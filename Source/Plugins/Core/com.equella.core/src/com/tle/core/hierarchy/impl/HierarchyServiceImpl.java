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

package com.tle.core.hierarchy.impl;

import com.dytech.common.collections.CombinedCollection;
import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.EntityScript;
import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.hierarchy.SearchSetAdapter;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.search.PresetSearch;
import com.tle.common.search.searchset.SearchSet;
import com.tle.common.security.Privilege;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.collection.event.listener.ItemDefinitionDeletionListener;
import com.tle.core.dao.AbstractTreeDao.DeleteAction;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.hierarchy.xml.ItemXmlConverter;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.item.service.ItemService;
import com.tle.core.powersearch.event.listener.PowerSearchDeletionListener;
import com.tle.core.schema.event.listener.SchemaDeletionListener;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.search.searchset.SearchSetService;
import com.tle.core.search.searchset.virtualisation.VirtualisationHelper;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.security.impl.RequiresPrivilegeWithNoTarget;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.xml.service.XmlService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** @author Nicholas Read */
@Singleton
@SuppressWarnings("nls")
@Bindings({@Bind(HierarchyService.class), @Bind(HierarchyServiceImpl.class)})
public class HierarchyServiceImpl
    implements HierarchyService,
        ItemDeletedListener,
        SchemaDeletionListener,
        ItemDefinitionDeletionListener,
        PowerSearchDeletionListener {
  private static final String TOPIC_ORDERING = "listPosition";
  private static final Collection<String> EDIT_PRIV_LIST =
      Collections.singleton("EDIT_HIERARCHY_TOPIC");

  @Inject private HierarchyDao dao;
  @Inject private EntityRegistry registry;
  @Inject private TLEAclManager aclManager;
  @Inject private FreeTextService freeTextService;
  @Inject private ItemService itemService;
  @Inject private SearchSetService searchSetService;
  @Inject private TaskService taskService;
  @Inject private XmlService xmlService;
  private XStream xstream;

  @Override
  @SecureOnReturn(priv = "VIEW_HIERARCHY_TOPIC")
  @Transactional(propagation = Propagation.REQUIRED)
  public List<HierarchyTopic> getRootTopics() {
    return dao.getRootNodes(TOPIC_ORDERING);
  }

  @Override
  @SecureOnReturn(priv = "VIEW_HIERARCHY_TOPIC")
  public List<HierarchyTopic> getSubTopics(HierarchyTopic topic) {
    return topic.getSubTopics();
  }

  @Override
  public List<HierarchyTopic> getChildTopics(HierarchyTopic topic) {
    return Optional.ofNullable(topic).map(this::getSubTopics).orElseGet(this::getRootTopics);
  }

  @Override
  public Optional<List<String>> getCollectionUuids(HierarchyTopic hierarchy) {
    List<ItemDefinitionScript> additionalItemDefs =
        new ArrayList<>(hierarchy.getAdditionalItemDefs());
    List<ItemDefinitionScript> inheritedItemDefs =
        new ArrayList<>(hierarchy.getInheritedItemDefs());

    List<String> uuids =
        Stream.concat(inheritedItemDefs.stream(), additionalItemDefs.stream())
            .map(itemDef -> itemDef.getEntity().getUuid())
            .collect(Collectors.toList());
    return Optional.of(uuids).filter(set -> !set.isEmpty());
  }

  /** Build a search for counting the items matching this topic. */
  @Override
  public PresetSearch buildSearch(HierarchyTopic topic, Map<String, String> compoundUuidMap) {
    FreeTextBooleanQuery searchClause = getSearchClause(topic, compoundUuidMap);
    String freetextQuery = getFullFreetextQuery(topic);

    PresetSearch search = new PresetSearch(freetextQuery, searchClause, true);
    getCollectionUuids(topic).ifPresent(search::setCollectionUuids);
    getAllSchema(topic).ifPresent(search::setSchemas);

    return search;
  }

  @Override
  public int getMatchingItemCount(HierarchyTopic topic, Map<String, String> compoundUuidMap) {
    PresetSearch search = buildSearch(topic, compoundUuidMap);
    return Arrays.stream(freeTextService.countsFromFilters(Collections.singletonList(search)))
        .findFirst()
        .orElse(0);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void addKeyResource(HierarchyCompoundUuid compoundUuid, ItemKey itemId) {
    HierarchyTopicKeyResource newKeyResources = new HierarchyTopicKeyResource();
    newKeyResources.setHierarchyCompoundUuid(compoundUuid.buildString(true));
    newKeyResources.setItemUuid(itemId.getUuid());
    newKeyResources.setItemVersion(itemId.getVersion());
    newKeyResources.setInstitution(CurrentInstitution.get());
    newKeyResources.setDateCreated(new Date());
    dao.saveKeyResources(newKeyResources);
  }

  /** Check whether the given item is a key resource of the given hierarchy topic. */
  @Override
  public boolean hasKeyResource(HierarchyCompoundUuid compoundUuid, ItemKey itemId) {
    String itemUuid = itemId.getUuid();
    int itemVersion = itemId.getVersion();

    Optional<HierarchyTopicKeyResource> keyResource =
        getKeyResource(compoundUuid, itemUuid, itemVersion);
    return keyResource.isPresent();
  }

  @Override
  @Transactional
  public void deleteKeyResources(Item item) {
    dao.deleteKeyResources(item.getUuid(), item.getVersion(), CurrentInstitution.get());
  }

  @Override
  @Transactional
  public void deleteKeyResources(HierarchyCompoundUuid compoundUuid, ItemKey itemId) {
    dao.deleteKeyResource(compoundUuid.buildString(true), itemId.getUuid(), itemId.getVersion());
  }

  @Override
  @Transactional
  public void removeDeletedItemReference(String legacyHierarchyCompoundUuid, int version) {
    dao.deleteKeyResources(legacyHierarchyCompoundUuid, version, CurrentInstitution.get());
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  @SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
  public long add(HierarchyTreeNode parentNode, String name, boolean inheritConstraints) {
    LanguageBundle nameBundle = new LanguageBundle();
    LangUtils.setString(nameBundle, CurrentLocale.getLocale(), name);

    HierarchyTopic topic = new HierarchyTopic();
    topic.setId(0L);
    topic.setName(nameBundle);
    topic.setUuid(UUID.randomUUID().toString());

    HierarchyTopic parent = parentNode == null ? null : getHierarchyTopic(parentNode.getId());
    if (parent != null && inheritConstraints) {
      List<ItemDefinitionScript> itemDefs = new ArrayList<ItemDefinitionScript>();
      for (ItemDefinition inheritable : getAllItemDefinitions(parent)) {
        itemDefs.add(new ItemDefinitionScript(inheritable, null));
      }
      topic.setInheritedItemDefs(itemDefs);

      List<SchemaScript> schemas = new ArrayList<SchemaScript>();
      for (Schema inheritable : getAllSchemas(parent)) {
        schemas.add(new SchemaScript(inheritable, null));
      }
      topic.setInheritedSchemas(schemas);

      topic.setInheritFreetext(true);
    }

    return add(parent, topic, Integer.MAX_VALUE);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  @SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
  public long add(HierarchyTopic parent, HierarchyTopic newTopic, int position) {
    newTopic.setId(0L);
    insert(newTopic, parent, position);
    return newTopic.getId();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  @RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
  public long addRoot(HierarchyTopic newTopic, int position) {
    return add(null, newTopic, position);
  }

  @Override
  @Transactional
  @RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
  public long addToRoot(String name, boolean inheritConstraints) {
    return add(null, name, inheritConstraints);
  }

  /** Privilege checking is done in the method - not on an annotation */
  @Override
  @Transactional
  public void edit(HierarchyPack pack) {
    HierarchyTopic topic = pack.getTopic();
    edit(topic);
    aclManager.setTargetList(Node.HIERARCHY_TOPIC, pack.getTopic(), pack.getTargetList());
  }

  @Override
  @Transactional
  public void edit(HierarchyTopic topic) {
    HierarchyTopic oldTopic = dao.findById(topic.getId());
    ensureEditRights(oldTopic);
    dao.unlinkFromSession(oldTopic);

    topic.setListPosition(oldTopic.getListPosition());

    dao.deleteAny(oldTopic.getName());
    dao.deleteAny(oldTopic.getShortDescription());
    dao.deleteAny(oldTopic.getLongDescription());
    dao.deleteAny(oldTopic.getResultsSectionName());
    dao.deleteAny(oldTopic.getSubtopicsSectionName());

    dao.saveOrUpdate(topic);
  }

  @Override
  public void updateKeyResources(
      HierarchyCompoundUuid compoundUuid, List<ItemId> providedKeyResourcesItems) {
    List<ItemId> existingResources =
        getKeyResourceItems(compoundUuid).stream().map(Item::getItemId).toList();

    // New key resources not in the original list that need to be added.
    List<ItemId> newResources =
        providedKeyResourcesItems.stream()
            .filter(itemId -> !existingResources.contains(itemId))
            .toList();

    // Key resource not included in the new provided list which need to be deleted.
    List<ItemId> deleteResources =
        existingResources.stream()
            .filter(itemId -> !providedKeyResourcesItems.contains(itemId))
            .toList();

    deleteResources.forEach(itemId -> deleteKeyResources(compoundUuid, itemId));
    newResources.forEach(
        item -> addKeyResource(compoundUuid, new ItemId(item.getUuid(), item.getVersion())));
  }

  private void ensureEditRights(HierarchyTopic topic) {
    // We ensure that "null" is included in the following list because there
    // may be a grant at the institution level that would otherwise be
    // filtered out by revokes on topics
    final Collection<HierarchyTopic> objs =
        new CombinedCollection<HierarchyTopic>(topic.getAllParents(), Arrays.asList(topic, null));
    if (aclManager.filterNonGrantedObjects(EDIT_PRIV_LIST, objs).isEmpty()) {
      throw new AccessDeniedException(
          "You do not have the EDIT_HIERARCHY_TOPIC"
              + " privilege for this topic or any of it's parent topics.");
    }
  }

  @Override
  @Transactional
  @SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
  public void delete(HierarchyTreeNode node) {
    delete(getHierarchyTopic(node.getId()));
  }

  @Override
  @Transactional
  @SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
  public void delete(HierarchyTopic topic) {
    dao.delete(
        topic,
        new DeleteAction<HierarchyTopic>() {
          @Override
          public void beforeDelete(HierarchyTopic topic) {
            aclManager.setTargetList(Node.HIERARCHY_TOPIC, topic, null);
          }
        });
  }

  @Override
  public HierarchyTopic getHierarchyTopic(long id) {
    return dao.findById(id);
  }

  @Override
  public HierarchyTopic getHierarchyTopicByUuid(String compoundUuid) {
    String uuid = compoundUuid.split(":", 2)[0];
    return dao.findByUuid(uuid, CurrentInstitution.get());
  }

  @Override
  @SecureOnReturn(priv = "VIEW_HIERARCHY_TOPIC")
  @Transactional
  public HierarchyTopic assertViewAccess(HierarchyTopic topic) {
    return topic;
  }

  @Override
  public Boolean hasViewAccess(HierarchyTopic topic) {
    return aclManager.hasPrivilege(topic, Privilege.VIEW_HIERARCHY_TOPIC);
  }

  @Override
  public Boolean hasEditAccess(HierarchyTopic topic) {
    return aclManager.hasPrivilege(topic, Privilege.EDIT_HIERARCHY_TOPIC);
  }

  @Override
  public Boolean hasModifyKeyResourceAccess(HierarchyTopic topic) {
    return aclManager.hasPrivilege(topic, Privilege.MODIFY_KEY_RESOURCE);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public int countChildTopics(HierarchyTopic topic) {
    if (topic == null) {
      return dao.countRootNodes();
    } else {
      return dao.countSubnodesForNode(topic);
    }
  }

  private final VirtualisationHelper<HierarchyTopic> virtualisationHelper =
      new VirtualisationHelper<HierarchyTopic>() {
        @Override
        public SearchSet getSearchSet(HierarchyTopic topic) {
          return new SearchSetAdapter(topic);
        }

        @Override
        public HierarchyTopic newFromPrototypeForValue(HierarchyTopic obj, String value) {
          return obj;
        }
      };

  @Override
  public List<VirtualisableAndValue<HierarchyTopic>> expandVirtualisedTopics(
      List<HierarchyTopic> topics,
      Map<String, String> mappedValues,
      Collection<String> collectionUuids) {
    return searchSetService.expandSearchSets(
        topics, mappedValues, collectionUuids, virtualisationHelper);
  }

  @Override
  public LanguageBundle getHierarchyTopicName(long topicID) {
    return dao.getHierarchyTopicName(topicID);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.remoting.RemoteHierarchyService#getHierarchyPack(java.lang
   * .String)
   */
  @Override
  public HierarchyPack getHierarchyPack(long topicID) {
    HierarchyTopic topic = getHierarchyTopic(topicID);

    HierarchyPack pack = new HierarchyPack();
    pack.setTopic(topic);
    pack.setTargetList(aclManager.getTargetList(Node.HIERARCHY_TOPIC, topic));
    pack.setInheritedItemDefinitions(getAllItemDefinitions(topic.getParent()));
    pack.setInheritedSchemas(getAllSchemas(topic.getParent()));

    return pack;
  }

  private List<ItemDefinition> getAllItemDefinitions(HierarchyTopic topic) {
    List<ItemDefinition> results = new ArrayList<ItemDefinition>();
    if (topic != null) {
      if (topic.getAdditionalItemDefs() != null) {
        for (EntityScript<ItemDefinition> query : topic.getAdditionalItemDefs()) {
          results.add(new ItemDefinition(query.getEntity().getId()));
        }
      }

      if (topic.getInheritedItemDefs() != null) {
        for (EntityScript<ItemDefinition> query : topic.getInheritedItemDefs()) {
          results.add(new ItemDefinition(query.getEntity().getId()));
        }
      }
    }
    return results;
  }

  private List<Schema> getAllSchemas(HierarchyTopic topic) {
    List<Schema> results = new ArrayList<Schema>();
    if (topic != null) {
      if (topic.getAdditionalSchemas() != null) {
        for (EntityScript<Schema> query : topic.getAdditionalSchemas()) {
          results.add(new Schema(query.getEntity().getId()));
        }
      }

      if (topic.getInheritedSchemas() != null) {
        for (EntityScript<Schema> query : topic.getInheritedSchemas()) {
          results.add(new Schema(query.getEntity().getId()));
        }
      }
    }
    return results;
  }

  /*
   * (non-Javadoc)
   * @see com.tle.core.remoting.RemoteHierarchyService#listTreeNodes(long)
   */
  @Override
  public List<HierarchyTreeNode> listTreeNodes(long parentTopicID) {
    HierarchyTopic parent = parentTopicID <= 0 ? null : getHierarchyTopic(parentTopicID);

    List<HierarchyTopic> subTopics = getChildTopics(parent);

    Collection<HierarchyTopic> topicsGrantedEdit =
        aclManager.filterNonGrantedObjects(EDIT_PRIV_LIST, subTopics);

    List<HierarchyTreeNode> results = new ArrayList<HierarchyTreeNode>();
    for (HierarchyTopic childTopic : subTopics) {
      HierarchyTreeNode childNode = new HierarchyTreeNode();
      childNode.setId(childTopic.getId());
      childNode.setName(CurrentLocale.get(childTopic.getName()));
      childNode.setGrantedEditTopic(topicsGrantedEdit.contains(childTopic));
      results.add(childNode);
    }
    return results;
  }

  @Override
  public String getFullFreetextQuery(HierarchyTopic topic) {
    return searchSetService.getFreetextQuery(new SearchSetAdapter(topic));
  }

  @Override
  public FreeTextBooleanQuery getSearchClause(
      HierarchyTopic topic, Map<String, String> virtualisationValues) {
    return searchSetService.getSearchClauses(new SearchSetAdapter(topic), virtualisationValues);
  }

  @Override
  @Transactional
  public void move(long childID, long parentID, int position) {
    HierarchyTopic child = getHierarchyTopic(childID);
    HierarchyTopic parent = getHierarchyTopic(parentID);
    insert(child, parent, position);
  }

  /** parentUuid may be null */
  @Override
  @Transactional
  public void move(String childUuid, String parentUuid, int index) {
    HierarchyTopic child = getHierarchyTopicByUuid(childUuid);
    HierarchyTopic parent = parentUuid != null ? getHierarchyTopicByUuid(parentUuid) : null;
    insert(child, parent, index);
  }

  /** Inserts a child into a parent at the given position. */
  protected void insert(
      final HierarchyTopic child, final HierarchyTopic parent, final int position) {
    List<HierarchyTopic> children = getChildTopics(parent);
    child.setInstitution(CurrentInstitution.get());
    // zap the all parents collection as this is re-initialised during save
    child.setAllParents(null);

    // Remove the child if it is already in this parent
    if (children.contains(child)) {
      children.remove(child);
    }

    // Add the child back into the desired position
    if (position >= children.size()) {
      children.add(child); // Add to end
    } else if (position <= 0) {
      children.addFirst(child); // Add to start
    } else {
      children.add(position, child); // Add to position
    }

    // Update the positions of the children and save
    for (int i = 0; i < children.size(); i++) {
      HierarchyTopic c = children.get(i);
      c.setListPosition(i);
      if (!c.equals(child) && Objects.equals(parent, c.getParent())) {
        dao.updateWithNoParentChange(c);
      } else {
        c.setParent(parent);
        dao.saveOrUpdate(c);
      }
    }
  }

  private <T extends BaseEntity, U extends EntityScript<T>> void removeEntity(
      List<U> queries, T entity) {
    for (Iterator<U> iter = queries.iterator(); iter.hasNext(); ) {
      EntityScript<T> script = iter.next();
      if (Objects.equals(script.getEntity(), entity)) {
        iter.remove();
      }
    }
  }

  @Override
  @RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
  public String importRootTopic(String xml, boolean newids, boolean useSecurity) {
    String returnXml = removeCoursesNode(new PropBagEx(xml));
    return startImportTask(returnXml, null, newids, useSecurity);
  }

  @Override
  @RequiresPrivilege(priv = "EDIT_HIERARCHY_TOPIC")
  public String importTopic(
      String xml, HierarchyTreeNode topicInto, boolean newids, boolean useSecurity) {
    String returnXml = removeCoursesNode(new PropBagEx(xml));
    return startImportTask(returnXml, topicInto, newids, useSecurity);
  }

  // removing the "courses" nodes from the older version of exported topic XML
  // (Bug #6737)
  private String removeCoursesNode(PropBagEx xml) {
    xml.deleteNode("topic/courses");
    if (xml.nodeExists("children/com.tle.beans.hierarchy.ExportedHierarchyNode")) {
      PropBagEx childXml = xml.getSubtree("children");
      for (PropBagEx bag : childXml.iterator("com.tle.beans.hierarchy.ExportedHierarchyNode")) {
        removeCoursesNode(bag);
      }
    }
    return xml.toString();
  }

  private String startImportTask(
      String xml, HierarchyTreeNode topicInto, boolean newids, boolean useSecurity) {
    return taskService.startTask(
        new BeanClusteredTask(
            null,
            HierarchyTaskFactory.class,
            "createImportTask",
            CurrentUser.getUserState(),
            xml,
            topicInto == null ? -1L : topicInto.getId(),
            newids,
            useSecurity));
  }

  @Override
  public ImportStatus getImportStatus(String taskUuid) {
    TaskStatus ts = taskService.getTaskStatus(taskUuid);

    String error = ts.getErrorMessage();
    if (error != null) {
      throw new RuntimeException(error);
    }

    List<Serializable> taskLog = ts.getTaskLog();
    if (!Check.isEmpty(taskLog)) {
      return ImportStatus.error((ValidationError) taskLog.getFirst());
    }

    if (ts.isFinished()) {
      return ImportStatus.finished();
    }

    return ImportStatus.progress(ts.getDoneWork(), ts.getMaxWork());
  }

  @Override
  @SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
  public String exportTopic(HierarchyTreeNode node, boolean withSecurity) {
    return taskService.startTask(
        new BeanClusteredTask(
            null,
            HierarchyTaskFactory.class,
            "createExportTask",
            CurrentUser.getUserState(),
            node.getId(),
            withSecurity));
  }

  @Override
  public ExportStatus getExportStatus(String taskUuid) {
    TaskStatus ts = taskService.getTaskStatus(taskUuid);

    String error = ts.getErrorMessage();
    if (error != null) {
      throw new RuntimeException(error);
    }

    if (ts.isFinished()) {
      return ExportStatus.finished(((URL) ts.getTaskLog().getFirst()).toString());
    }

    return ExportStatus.progress(ts.getDoneWork(), ts.getMaxWork());
  }

  @Override
  public XStream getXStream() {
    if (xstream == null) {
      xstream = xmlService.createDefault(getClass().getClassLoader());
      xstream.registerConverter(new BaseEntityXmlConverter(registry));
      xstream.registerConverter(new ItemXmlConverter(itemService));
    }
    return xstream;
  }

  @Override
  public void itemDeletedEvent(ItemDeletedEvent event) {
    Item item = new Item();
    item.setId(event.getKey());
    // TODO: OEQ-2051 Delete related key resources
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void removeReferences(Schema schema) {
    for (HierarchyTopic topic : dao.getTopicsReferencingSchema(schema)) {
      removeEntity(topic.getAdditionalSchemas(), schema);
      removeEntity(topic.getInheritedSchemas(), schema);

      dao.updateWithNoParentChange(topic);
    }
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void removeReferences(ItemDefinition entity) {
    for (HierarchyTopic topic : dao.getTopicsReferencingItemDefinition(entity)) {
      removeEntity(topic.getAdditionalItemDefs(), entity);
      removeEntity(topic.getInheritedItemDefs(), entity);

      dao.updateWithNoParentChange(topic);
    }
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void removeReferences(PowerSearch powerSearch) {
    for (HierarchyTopic topic : dao.getTopicsReferencingPowerSearch(powerSearch)) {
      topic.setAdvancedSearch(null);
      dao.updateWithNoParentChange(topic);
    }
  }

  @Override
  public List<HierarchyTopicKeyResource> getKeyResources(HierarchyCompoundUuid compoundUuid) {
    return dao.getKeyResources(compoundUuid.buildString(true), CurrentInstitution.get());
  }

  @Override
  public List<Item> getKeyResourceItems(HierarchyCompoundUuid compoundUuid) {
    // HierarchyTopicKeyResource.
    return getKeyResources(compoundUuid).stream()
        // ItemId.
        .map(resources -> new ItemId(resources.getItemUuid(), resources.getItemVersion()))
        // Item.
        .flatMap(id -> Optional.ofNullable(itemService.getUnsecureIfExists(id)).stream())
        .collect(Collectors.toList());
  }

  @Override
  public Optional<HierarchyTopicKeyResource> getKeyResource(
      HierarchyCompoundUuid compoundUuid, String itemUuid, int itemVersion) {
    return dao.getKeyResource(
        compoundUuid.buildString(true), itemUuid, itemVersion, CurrentInstitution.get());
  }

  @Override
  public Collection<String> getTopicIdsWithKeyResource(Item item) {
    Collection<String> ids = new ArrayList<String>();

    String uuid = item.getUuid();
    int version = item.getVersion();
    List<HierarchyTopicKeyResource> keyResources =
        dao.getKeyResources(uuid, version, CurrentInstitution.get());

    keyResources.forEach(k -> ids.add(k.getHierarchyCompoundUuid()));
    return ids;
  }

  /**
   * Fetch all schema (includes inherited schema and additional schema) associated with a given
   * hierarchy topic.
   */
  private Optional<Set<Schema>> getAllSchema(HierarchyTopic hierarchy) {
    List<SchemaScript> additionalSchema = hierarchy.getAdditionalSchemas();
    List<SchemaScript> inheritedSchemas = hierarchy.getInheritedSchemas();

    Set<Schema> allSchema =
        Stream.concat(additionalSchema.stream(), inheritedSchemas.stream())
            .map(SchemaScript::getEntity)
            .collect(Collectors.toSet());

    return Optional.of(allSchema).filter(set -> !set.isEmpty());
  }
}
