/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.usermanagement.standard.service.impl;

import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.dao.helpers.Pagination;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.services.ValidationHelper;
import com.tle.core.usermanagement.standard.dao.TLEGroupDao;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(TLEGroupService.class)
@Singleton
@SuppressWarnings("nls")
public class TLEGroupServiceImpl
    implements TLEGroupService, UserChangeListener, GroupChangedListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(TLEGroupServiceImpl.class);
  private static final String[] BLANKS = {"name"};

  @Inject private TLEGroupDao dao;
  @Inject private EventService eventService;

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public String add(String parentID, String name) {
    TLEGroup group = createGroup(null, name);

    if (parentID != null) {
      Optional.ofNullable(get(parentID))
          .ifPresentOrElse(
              group::setParent,
              () -> {
                throw new NotFoundException("No such parent with id of " + parentID + " found.");
              });
    }

    if (Check.isEmpty(group.getUuid())) {
      group.setUuid(UUID.randomUUID().toString());
    }

    return add(group);
  }

  @Override
  public TLEGroup createGroup(String groupId, String name) {
    // ensure groupId doesn't already exist, it didn't always have a unique
    // constraint
    if (get(groupId) != null) {
      throw new RuntimeException(
          CurrentLocale.get("com.tle.core.entity.services.groups.error.alreadyexists", groupId));
    }

    TLEGroup group = new TLEGroup();
    group.setId(0l);
    group.setName(name);
    group.setInstitution(CurrentInstitution.get());
    if (groupId != null) {
      group.setUuid(groupId);
    }
    return group;
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public String add(TLEGroup group) {
    if (group != null) {
      validate(group);
      checkInUse(group, true);

      dao.save(group);

      return group.getUuid();
    }
    return null;
  }

  @Transactional(readOnly = true)
  @Override
  public TLEGroup get(String id) {
    return dao.findByUuid(id);
  }

  @Override
  public TLEGroup getByName(String name) {
    return dao.findByCriteria(
        Restrictions.eq("name", name), Restrictions.eq("institution", CurrentInstitution.get()));
  }

  /**
   * Check if the group is in use, looking for any other group (different by uuid) in the same
   * institution and with the same name. Thereby attempting to ensure the name is unique.
   *
   * @param group The group to check
   * @param ignoreUuid Whether to ignore the UUID of the group when checking for uniqueness, useful
   *     when adding a new group which doesn't yet have a UUID.
   */
  private void checkInUse(TLEGroup group, boolean ignoreUuid) {
    Criterion hasDifferentUuid = ignoreUuid ? null : Restrictions.ne("uuid", group.getUuid());
    Criterion withSameName = Restrictions.eq("name", group.getName());
    boolean unique =
        dao.findAllByCriteria(hasDifferentUuid, withSameName, CurrentInstitution.equalityCriteria())
            .isEmpty();
    if (!unique) {
      ValidationError error = new ValidationError("name", "Name already exists");
      throw new InvalidDataException(Collections.singletonList(error));
    }
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public String edit(final TLEGroup group) {
    boolean parentSame;
    {
      TLEGroup original = get(group.getUuid());
      Optional<TLEGroup> oldParent = Optional.ofNullable(original.getParent());

      dao.unlinkFromSession(original);
      oldParent.ifPresent(dao::unlinkFromSession);

      parentSame = Objects.equals(oldParent.orElse(null), group.getParent());
    }

    group.setInstitution(CurrentInstitution.get());

    validate(group);
    checkInUse(group, false);

    dao.update(group);

    if (!parentSame) {
      LOGGER.info("Group parent modified - Rebuilding subgroup parents");
      updateGroup(group);
    }

    eventService.publishApplicationEvent(new GroupEditEvent(group.getUuid(), group.getUsers()));
    return group.getUuid();
  }

  private void updateGroup(TLEGroup group) {
    for (TLEGroup subgroup : getGroupsInGroup(group)) {
      dao.update(subgroup);

      updateGroup(subgroup);
    }
  }

  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(TLEGroup group, boolean deleteChildren) {
    TLEGroup parent = group.getParent();

    if (!deleteChildren) {
      // Move children up to the same level as the group we're deleting
      for (TLEGroup child : getGroupsInGroup(group)) {
        child.setParent(parent);
        updateGroup(child);
        dao.update(child);
      }
    }

    dao.delete(group);
    eventService.publishApplicationEvent(new GroupDeletedEvent(group.getUuid()));
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(String groupID, boolean deleteChildren) {
    Optional.ofNullable(get(groupID))
        .ifPresentOrElse(
            group -> delete(group, deleteChildren),
            () -> {
              throw new NotFoundException("No such group with id of " + groupID + " found.");
            });
  }

  @Override
  public List<String> getUsersInGroup(String parentGroupID, boolean recurse) {
    return dao.getUsersInGroup(parentGroupID, recurse);
  }

  @Override
  public List<String> getUsersInGroup(
      String parentGroupID, boolean recurse, Integer limit, Integer offset) {
    return dao.getUsersInGroup(parentGroupID, recurse, Pagination.of(offset, limit));
  }

  @Override
  public List<TLEGroup> getGroupsInGroup(TLEGroup group) {
    return getGroupsInGroup(group, null, null);
  }

  public List<TLEGroup> getGroupsInGroup(TLEGroup group, Integer limit, Integer offset) {
    return dao.findAllByCriteria(
        orderByName(),
        Pagination.of(offset, limit),
        withParent(group),
        CurrentInstitution.equalityCriteria());
  }

  @Override
  public List<TLEGroup> search(String query) {
    return search(query, null, null);
  }

  @Override
  public List<TLEGroup> search(String query, Integer limit, Integer offset) {
    return dao.findAllByCriteria(
        orderByName(),
        Pagination.of(offset, limit),
        withNameLike(query),
        CurrentInstitution.equalityCriteria());
  }

  @Override
  public List<TLEGroup> search(String query, String parentGroup) {
    return dao.searchGroups(query, parentGroup);
  }

  /**
   * We are searching on a query string (on group name), or by user as member, or both. If the user
   * search is invoked, the boolean flag indicates if all parents of the group are to be included in
   * the result set
   */
  @Override
  public List<TLEGroup> search(String query, String userId, boolean allParents) {
    if (Check.isEmpty(userId)) {
      return search(query);
    } else {
      List<TLEGroup> userGroups = getGroupsContainingUser(userId, allParents);
      if (Check.isEmpty(query)) {
        return userGroups;
      } else {
        List<TLEGroup> queryGroups = search(query);
        queryGroups.retainAll(userGroups);
        return queryGroups;
      }
    }
  }

  private void validate(TLEGroup group) {
    List<ValidationError> errors = new ArrayList<ValidationError>();
    ValidationHelper.checkBlankFields(group, BLANKS, errors);

    if (!errors.isEmpty()) {
      throw new InvalidDataException(errors);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<TLEGroup> getGroupsContainingUser(String userID, boolean recursive) {
    List<TLEGroup> results = dao.getGroupsContainingUser(userID);

    if (recursive) {
      Set<TLEGroup> realResults = new HashSet<TLEGroup>();
      for (TLEGroup group : results) {
        realResults.add(group);
        realResults.addAll(group.getAllParents());
      }
      results = new ArrayList<TLEGroup>(realResults);
    }

    return results;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void userDeletedEvent(UserDeletedEvent event) {
    String userID = event.getUserID();

    for (TLEGroup group : dao.getGroupsContainingUser(userID)) {
      group.getUsers().remove(userID);

      dao.update(group);
      eventService.publishApplicationEvent(
          new GroupEditEvent(group.getUuid(), Collections.singleton(userID)));
    }
  }

  @Override
  public void userEditedEvent(UserEditEvent event) {
    // Nothing to do here
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void userIdChangedEvent(UserIdChangedEvent event) {
    String fromUserId = event.getFromUserId();

    for (TLEGroup group : dao.getGroupsContainingUser(fromUserId)) {
      Set<String> users = group.getUsers();
      users.remove(fromUserId);
      users.add(event.getToUserId());

      dao.update(group);
      eventService.publishApplicationEvent(
          new GroupEditEvent(group.getUuid(), Collections.singleton(fromUserId)));
    }
  }

  @Override
  public List<TLEGroup> getInformationForGroups(Collection<String> groups) {
    return dao.getInformationForGroups(groups);
  }

  @Override
  public List<TLEGroup> getInformationForGroups(
      Collection<String> groupIds, Integer limit, Integer offset) {
    return dao.findAllByCriteria(
        orderByName(),
        Pagination.of(offset, limit),
        Restrictions.in("uuid", groupIds),
        CurrentInstitution.equalityCriteria());
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public void addUserToGroup(String groupUuid, String userUuid) {
    if (dao.addUserToGroup(groupUuid, userUuid)) {
      eventService.publishApplicationEvent(
          new GroupEditEvent(groupUuid, Collections.singleton(userUuid)));
    }
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public void removeUserFromGroup(String groupUuid, String userUuid) {
    if (dao.removeUserFromGroup(groupUuid, userUuid)) {
      eventService.publishApplicationEvent(
          new GroupEditEvent(groupUuid, Collections.singleton(userUuid)));
    }
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
  @Transactional(propagation = Propagation.REQUIRED)
  public void removeAllUsersFromGroup(String groupUuid) {
    TLEGroup group = get(groupUuid);
    if (group != null) {
      group.setUsers(null);
      edit(group);
    }
  }

  @Override
  public void groupDeletedEvent(GroupDeletedEvent event) {
    // Don't delete our groups! Either we kicked it off by deleting one of
    // our users, or it's an event from another UMP which isn't going to
    // match our IDs anyway.
  }

  @Override
  public void groupEditedEvent(GroupEditEvent groupEditEvent) {
    // We don't care - either we've kicked off the edit event or it's for
    // another UMP.
  }

  @Override
  @Transactional
  public void groupIdChangedEvent(GroupIdChangedEvent event) {
    TLEGroup group = dao.findByUuid(event.getFromGroupId());
    if (group != null) {
      group.setUuid(event.getToGroupId());
      dao.update(group);
    }
  }

  @Override
  public String prepareQuery(String searchString) {
    if (!searchString.startsWith("*")) {
      searchString = "*" + searchString;
    }

    if (!searchString.endsWith("*")) {
      searchString += "*";
    }

    return searchString;
  }

  @Override
  public long countUsersInGroup(String groupId) {
    return dao.countUsersInGroup(groupId);
  }

  @Override
  public long countGroupsInGroupById(String groupId) {
    return countGroupsInGroup(get(groupId));
  }

  @Override
  public long countGroupsInGroup(TLEGroup parent) {
    return dao.countByCriteria(withParent(parent), CurrentInstitution.equalityCriteria());
  }

  @Override
  public long countGroupsForQuery(String query) {
    return dao.countByCriteria(withNameLike(query), CurrentInstitution.equalityCriteria());
  }

  @Override
  public long countValidGroups(Set<String> groupIds) {
    return dao.countByCriteria(
        Restrictions.in("uuid", groupIds), CurrentInstitution.equalityCriteria());
  }

  private Order orderByName() {
    return Order.asc("name");
  }

  private Criterion withParent(TLEGroup parent) {
    return parent == null ? Restrictions.isNull("parent") : Restrictions.eq("parent", parent);
  }

  private Criterion withNameLike(String name) {
    return Restrictions.ilike("name", name.replace('*', '%'));
  }
}
