/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.core.services.ValidationHelper;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.beans.user.GroupTreeNode;
import com.tle.beans.user.TLEGroup;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.events.services.EventService;
import com.tle.core.usermanagement.standard.dao.TLEGroupDao;
import com.tle.core.usermanagement.standard.service.TLEGroupService;
import com.tle.common.institution.CurrentInstitution;

/**
 * @author Nicholas Read
 */
@Bind(TLEGroupService.class)
@Singleton
@SuppressWarnings("nls")
public class TLEGroupServiceImpl implements TLEGroupService, UserChangeListener, GroupChangedListener
{
	private static final Logger LOGGER = Logger.getLogger(TLEGroupServiceImpl.class);
	private static final String[] BLANKS = {"name"};

	@Inject
	private TLEGroupDao dao;
	@Inject
	private EventService eventService;

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public String add(String parentID, String name)
	{
		TLEGroup group = createGroup(null, name);

		if( parentID != null )
		{
			group.setParent(get(parentID));
		}

		if( Check.isEmpty(group.getUuid()) )
		{
			group.setUuid(UUID.randomUUID().toString());
		}

		return add(group);
	}

	@Override
	public TLEGroup createGroup(String groupId, String name)
	{
		// ensure groupId doesn't already exist, it didn't always have a unique
		// constraint
		if( get(groupId) != null )
		{
			throw new RuntimeException(CurrentLocale.get("com.tle.core.entity.services.groups.error.alreadyexists",
				groupId));
		}

		TLEGroup group = new TLEGroup();
		group.setId(0l);
		group.setName(name);
		group.setInstitution(CurrentInstitution.get());
		if( groupId != null )
		{
			group.setUuid(groupId);
		}
		return group;
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public String add(TLEGroup group)
	{
		if( group != null )
		{
			validate(group);
			checkInUse(group, true);

			dao.save(group);

			return group.getUuid();
		}
		return null;
	}

	@Override
	public TLEGroup get(String id)
	{
		return dao.findByUuid(id);
	}

	@Override
	public TLEGroup getByName(String name)
	{
		return dao.findByCriteria(Restrictions.eq("name", name),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	private void checkInUse(TLEGroup group, boolean forAdd)
	{
		Criterion c1 = forAdd ? null : Restrictions.ne("uuid", group.getUuid());
		Criterion c2 = Restrictions.eq("name", group.getName());
		Criterion c3 = Restrictions.eq("institution", CurrentInstitution.get());
		if( !dao.findAllByCriteria(c1, c2, c3).isEmpty() )
		{
			ValidationError error = new ValidationError("name", "Name already exists");
			throw new InvalidDataException(Collections.singletonList(error));
		}
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public String edit(final TLEGroup group)
	{
		boolean parentSame;
		{
			TLEGroup original = get(group.getUuid());
			TLEGroup oldParent = original.getParent();
			dao.unlinkFromSession(original);
			dao.unlinkFromSession(oldParent);
			parentSame = Objects.equals(oldParent, group.getParent());
		}

		group.setInstitution(CurrentInstitution.get());

		validate(group);
		checkInUse(group, false);

		dao.update(group);

		if( !parentSame )
		{
			LOGGER.info("Group parent modified - Rebuilding subgroup parents");
			updateGroup(group);
		}

		eventService.publishApplicationEvent(new GroupEditEvent(group.getUuid(), group.getUsers()));
		return group.getUuid();
	}

	private void updateGroup(TLEGroup group)
	{
		for( TLEGroup subgroup : getGroupsInGroup(group) )
		{
			dao.update(subgroup);

			updateGroup(subgroup);
		}
	}

	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(TLEGroup group, boolean deleteChildren)
	{
		TLEGroup parent = group.getParent();

		if( !deleteChildren )
		{
			// Move children up to same level as group we're deleting
			for( TLEGroup child : getGroupsInGroup(group) )
			{
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
	public void delete(String groupID, boolean deleteChildren)
	{
		delete(get(groupID), deleteChildren);
	}

	@Override
	public List<String> getUsersInGroup(String parentGroupID, boolean recurse)
	{
		return dao.getUsersInGroup(parentGroupID, recurse);
	}

	private List<TLEGroup> getGroupsInGroup(TLEGroup group)
	{
		Criterion c1 = group == null ? Restrictions.isNull("parent") : Restrictions.eq("parent", group);
		return dao.findAllByCriteria(c1);
	}

	@Override
	public List<TLEGroup> search(String query)
	{
		Criterion c1 = Restrictions.ilike("name", query.replace('*', '%'));
		Criterion c2 = Restrictions.eq("institution", CurrentInstitution.get());

		return dao.findAllByCriteria(Order.asc("name"), -1, c1, c2);
	}

	@Override
	public List<TLEGroup> search(String query, String parentGroup)
	{
		return dao.searchGroups(query, parentGroup);
	}

	/**
	 * We are searching on a query string (on group name), or by user as member, or both.
	 * If the user search is invoked, the boolean flag indicates if all parents of the group
	 * are to be included in the result set
	 */
	@Override
	public List<TLEGroup> search(String query, String userId, boolean allParents)
	{
		if( Check.isEmpty(userId) )
		{
			return search(query);
		}
		else
		{
			List<TLEGroup> userGroups = getGroupsContainingUser(userId, allParents);
			if( Check.isEmpty(query) )
			{
				return userGroups;
			}
			else
			{
				List<TLEGroup> queryGroups = search(query);
				queryGroups.retainAll(userGroups);
				return queryGroups;
			}
		}
	}

	private void validate(TLEGroup group)
	{
		List<ValidationError> errors = new ArrayList<ValidationError>();
		ValidationHelper.checkBlankFields(group, BLANKS, errors);

		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<TLEGroup> getGroupsContainingUser(String userID, boolean recursive)
	{
		List<TLEGroup> results = dao.getGroupsContainingUser(userID);

		if( recursive )
		{
			Set<TLEGroup> realResults = new HashSet<TLEGroup>();
			for( TLEGroup group : results )
			{
				realResults.add(group);
				realResults.addAll(group.getAllParents());
			}
			results = new ArrayList<TLEGroup>(realResults);
		}

		return results;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void userDeletedEvent(UserDeletedEvent event)
	{
		String userID = event.getUserID();

		for( TLEGroup group : dao.getGroupsContainingUser(userID) )
		{
			group.getUsers().remove(userID);

			dao.update(group);
			eventService.publishApplicationEvent(new GroupEditEvent(group.getUuid(), Collections.singleton(userID)));
		}
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		String fromUserId = event.getFromUserId();

		for( TLEGroup group : dao.getGroupsContainingUser(fromUserId) )
		{
			Set<String> users = group.getUsers();
			users.remove(fromUserId);
			users.add(event.getToUserId());

			dao.update(group);
			eventService
				.publishApplicationEvent(new GroupEditEvent(group.getUuid(), Collections.singleton(fromUserId)));
		}
	}

	@Override
	public GroupTreeNode searchTree(String query)
	{
		query = prepareQuery(query);

		Map<String, GroupTreeNode> cachedNodes = new HashMap<String, GroupTreeNode>();
		GroupTreeNode root = new GroupTreeNode();

		for( TLEGroup gb : search(query) )
		{
			setupParents(gb, cachedNodes, root);
			for( TLEGroup sub : dao.getAllSubnodeForNode(gb) )
			{
				setupParents(sub, cachedNodes, root);
			}
		}

		return root;
	}

	private GroupTreeNode setupParents(TLEGroup group, Map<String, GroupTreeNode> cachedNodes, GroupTreeNode root)
	{
		final String groupID = group.getUuid();

		if( !cachedNodes.containsKey(groupID) )
		{
			GroupTreeNode node = new GroupTreeNode();
			node.setId(groupID);
			node.setName(group.getName());

			TLEGroup parent = group.getParent();
			if( parent == null )
			{
				root.add(node);
			}
			else
			{
				GroupTreeNode pnode = setupParents(parent, cachedNodes, root);
				pnode.add(node);
			}
			cachedNodes.put(node.getId(), node);
		}

		return cachedNodes.get(groupID);
	}

	@Override
	public List<TLEGroup> getInformationForGroups(Collection<String> groups)
	{
		return dao.getInformationForGroups(groups);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public void addUserToGroup(String groupUuid, String userUuid)
	{
		if( dao.addUserToGroup(groupUuid, userUuid) )
		{
			eventService.publishApplicationEvent(new GroupEditEvent(groupUuid, Collections.singleton(userUuid)));
		}
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeUserFromGroup(String groupUuid, String userUuid)
	{
		if( dao.removeUserFromGroup(groupUuid, userUuid) )
		{
			eventService.publishApplicationEvent(new GroupEditEvent(groupUuid, Collections.singleton(userUuid)));
		}
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_USER_MANAGEMENT")
	@Transactional(propagation = Propagation.REQUIRED)
	public void removeAllUsersFromGroup(String groupUuid)
	{
		TLEGroup group = get(groupUuid);
		if( group != null )
		{
			group.setUsers(null);
			edit(group);
		}
	}

	@Override
	public void groupDeletedEvent(GroupDeletedEvent event)
	{
		// Don't delete our groups! Either we kicked it off by deleting one of
		// our users, or it's an event from another UMP which isn't going to
		// match our IDs anyway.
	}

	@Override
	public void groupEditedEvent(GroupEditEvent groupEditEvent)
	{
		// We don't care - either we've kicked off the edit event or it's for
		// another UMP.
	}

	@Override
	@Transactional
	public void groupIdChangedEvent(GroupIdChangedEvent event)
	{
		TLEGroup group = dao.findByUuid(event.getFromGroupId());
		if( group != null )
		{
			group.setUuid(event.getToGroupId());
			dao.update(group);
		}
	}

	@Override
	public String prepareQuery(String searchString)
	{
		if( !searchString.startsWith("*") )
		{
			searchString = "*" + searchString;
		}

		if( !searchString.endsWith("*") )
		{
			searchString += "*";
		}

		return searchString;
	}
}
