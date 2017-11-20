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

package com.tle.core.hierarchy.impl;

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
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.hierarchy.SearchSetAdapter;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.search.searchset.SearchSet;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.collection.event.listener.ItemDefinitionDeletionListener;
import com.tle.core.dao.AbstractTreeDao.DeleteAction;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
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

/**
 * @author Nicholas Read
 */
@Singleton
@SuppressWarnings("nls")
@Bindings({@Bind(HierarchyService.class), @Bind(HierarchyServiceImpl.class)})
public class HierarchyServiceImpl
	implements
		HierarchyService,
		ItemDeletedListener,
		SchemaDeletionListener,
		ItemDefinitionDeletionListener,
		PowerSearchDeletionListener
{
	private static final String TOPIC_ORDERING = "listPosition";
	private static final Collection<String> EDIT_PRIV_LIST = Collections.singleton("EDIT_HIERARCHY_TOPIC");

	@Inject
	private HierarchyDao dao;
	@Inject
	private EntityRegistry registry;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private ItemService itemService;
	@Inject
	private SearchSetService searchSetService;
	@Inject
	private TaskService taskService;
	@Inject
	private XmlService xmlService;
	private XStream xstream;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SecureOnCall(priv = "MODIFY_KEY_RESOURCE")
	public void addKeyResource(HierarchyTreeNode node, ItemKey itemId)
	{
		final HierarchyTopic topic = getHierarchyTopic(node.getId());
		final Item item = itemService.get(itemId);

		List<Item> list = topic.getKeyResources();
		if( list == null )
		{
			list = new ArrayList<Item>();
			topic.setKeyResources(list);
		}

		if( !list.contains(item) )
		{
			list.add(item);
		}

		dao.saveOrUpdate(topic);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addKeyResource(String uuid, ItemKey itemId)
	{
		final String[] uv = uuid.split(":", 2);

		final HierarchyTopic topic = getHierarchyTopicByUuid(uv[0]);
		final Item item = itemService.get(itemId);

		// if the hierarchy is dynamic
		if( topic.getVirtualisationId() != null )
		{
			String itemUuid = item.getUuid();
			int itemVersion = item.getVersion();
			List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = getDynamicKeyResource(uuid, itemUuid,
				itemVersion);
			if( dynamicKeyResources == null )
			{
				saveDynamicKeyResource(uuid, itemUuid, itemVersion);
			}
			else
			{
				for( HierarchyTopicDynamicKeyResources k : dynamicKeyResources )
				{
					if( !k.getUuid().equals(itemUuid) || k.getVersion() != itemVersion )
					{
						saveDynamicKeyResource(uuid, itemUuid, itemVersion);
					}
				}
			}
		}
		else
		{
			List<Item> list = topic.getKeyResources();
			if( list == null )
			{
				list = new ArrayList<Item>();
				topic.setKeyResources(list);
			}

			if( !list.contains(item) )
			{
				list.add(item);
			}
			dao.saveOrUpdate(topic);
		}
	}

	@Transactional
	private void saveDynamicKeyResource(String dynamicHierarchyId, String itemUuid, int itemVersion)
	{
		HierarchyTopicDynamicKeyResources newDynamicKeyResources = new HierarchyTopicDynamicKeyResources();
		newDynamicKeyResources.setDynamicHierarchyId(dynamicHierarchyId);
		newDynamicKeyResources.setUuid(itemUuid);
		newDynamicKeyResources.setVersion(itemVersion);
		newDynamicKeyResources.setInstitution(CurrentInstitution.get());
		newDynamicKeyResources.setDateCreated(new Date());
		dao.saveDynamicKeyResources(newDynamicKeyResources);
	}

	@Override
	@Transactional
	public void deleteKeyResources(Item item)
	{
		dao.removeReferencesToItem(item);
		dao.removeDynamicKeyResource(item.getUuid(), item.getVersion(), CurrentInstitution.get());
	}

	@Override
	@Transactional
	public void deleteKeyResources(String uuid, ItemKey itemId)
	{
		final String[] uv = uuid.split(":", 2);

		final HierarchyTopic topic = getHierarchyTopicByUuid(uv[0]);
		final Item item = itemService.get(itemId);

		// if the hierarchy is dynamic
		if( topic.getVirtualisationId() != null )
		{
			dao.removeDynamicKeyResource(uuid, item.getUuid(), item.getVersion());
		}
		else
		{
			dao.removeReferencesToItem(item, topic.getId());
		}
	}

	@Override
	@Transactional
	public void removeDeletedItemReference(String uuid, int version)
	{
		dao.removeDynamicKeyResource(uuid, version, CurrentInstitution.get());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
	public long add(HierarchyTreeNode parentNode, String name, boolean inheritConstraints)
	{
		LanguageBundle nameBundle = new LanguageBundle();
		LangUtils.setString(nameBundle, CurrentLocale.getLocale(), name);

		HierarchyTopic topic = new HierarchyTopic();
		topic.setId(0l);
		topic.setName(nameBundle);
		topic.setUuid(UUID.randomUUID().toString());

		HierarchyTopic parent = parentNode == null ? null : getHierarchyTopic(parentNode.getId());
		if( parent != null && inheritConstraints )
		{
			List<ItemDefinitionScript> itemDefs = new ArrayList<ItemDefinitionScript>();
			for( ItemDefinition inheritable : getAllItemDefinitions(parent) )
			{
				itemDefs.add(new ItemDefinitionScript(inheritable, null));
			}
			topic.setInheritedItemDefs(itemDefs);

			List<SchemaScript> schemas = new ArrayList<SchemaScript>();
			for( Schema inheritable : getAllSchemas(parent) )
			{
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
	public long add(HierarchyTopic parent, HierarchyTopic newTopic, int position)
	{
		newTopic.setId(0l);
		insert(newTopic, parent, position);
		return newTopic.getId();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
	public long addRoot(HierarchyTopic newTopic, int position)
	{
		return add(null, newTopic, position);
	}

	@Override
	@Transactional
	@RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
	public long addToRoot(String name, boolean inheritConstraints)
	{
		return add(null, name, inheritConstraints);
	}

	/**
	 * Privilege checking is done in the method - not on an annotation
	 */
	@Override
	@Transactional
	public void edit(HierarchyPack pack)
	{
		HierarchyTopic topic = pack.getTopic();
		edit(topic);
		aclManager.setTargetList(Node.HIERARCHY_TOPIC, pack.getTopic(), pack.getTargetList());
	}

	@Override
	@Transactional
	public void edit(HierarchyTopic topic)
	{
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

	private void ensureEditRights(HierarchyTopic topic)
	{
		// We ensure that "null" is included in the following list because there
		// may be a grant at the institution level that would otherwise be
		// filtered out by revokes on topics
		final Collection<HierarchyTopic> objs = new CombinedCollection<HierarchyTopic>(topic.getAllParents(),
			Arrays.asList(topic, null));
		if( aclManager.filterNonGrantedObjects(EDIT_PRIV_LIST, objs).isEmpty() )
		{
			throw new AccessDeniedException(
				"You do not have the EDIT_HIERARCHY_TOPIC" + " privilege for this topic or any of it's parent topics.");
		}
	}

	@Override
	@Transactional
	@SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
	public void delete(HierarchyTreeNode node)
	{
		delete(getHierarchyTopic(node.getId()));
	}

	@Override
	@Transactional
	@SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
	public void delete(HierarchyTopic topic)
	{
		dao.delete(topic, new DeleteAction<HierarchyTopic>()
		{
			@Override
			public void beforeDelete(HierarchyTopic topic)
			{
				aclManager.setTargetList(Node.HIERARCHY_TOPIC, topic, null);
			}
		});
	}

	@Override
	public HierarchyTopic getHierarchyTopic(long id)
	{
		return dao.findById(id);
	}

	@Override
	public HierarchyTopic getHierarchyTopicByUuid(String uuid)
	{
		return dao.findByUuid(uuid, CurrentInstitution.get());
	}

	@Override
	@SecureOnReturn(priv = "VIEW_HIERARCHY_TOPIC")
	@Transactional
	public HierarchyTopic assertViewAccess(HierarchyTopic topic)
	{
		return topic;
	}

	@Override
	@SecureOnReturn(priv = "VIEW_HIERARCHY_TOPIC")
	@Transactional(propagation = Propagation.REQUIRED)
	public List<HierarchyTopic> getChildTopics(HierarchyTopic topic)
	{
		if( topic == null )
		{
			return dao.getRootNodes(TOPIC_ORDERING);
		}
		else
		{
			return dao.getChildrenForNode(topic, TOPIC_ORDERING);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public int countChildTopics(HierarchyTopic topic)
	{
		if( topic == null )
		{
			return dao.countRootNodes();
		}
		else
		{
			return dao.countSubnodesForNode(topic);
		}
	}

	private final VirtualisationHelper<HierarchyTopic> virtualisationHelper = new VirtualisationHelper<HierarchyTopic>()
	{
		@Override
		public SearchSet getSearchSet(HierarchyTopic topic)
		{
			return new SearchSetAdapter(topic);
		}

		@Override
		public HierarchyTopic newFromPrototypeForValue(HierarchyTopic obj, String value)
		{
			return obj;
		}
	};

	@Override
	public List<VirtualisableAndValue<HierarchyTopic>> expandVirtualisedTopics(List<HierarchyTopic> topics,
		Map<String, String> mappedValues, Collection<String> collectionUuids)
	{
		return searchSetService.expandSearchSets(topics, mappedValues, collectionUuids, virtualisationHelper);
	}

	@Override
	public LanguageBundle getHierarchyTopicName(long topicID)
	{
		return dao.getHierarchyTopicName(topicID);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.remoting.RemoteHierarchyService#getHierarchyPack(java.lang
	 * .String)
	 */
	@Override
	public HierarchyPack getHierarchyPack(long topicID)
	{
		HierarchyTopic topic = getHierarchyTopic(topicID);

		HierarchyPack pack = new HierarchyPack();
		pack.setTopic(topic);
		pack.setTargetList(aclManager.getTargetList(Node.HIERARCHY_TOPIC, topic));
		pack.setInheritedItemDefinitions(getAllItemDefinitions(topic.getParent()));
		pack.setInheritedSchemas(getAllSchemas(topic.getParent()));

		return pack;
	}

	private List<ItemDefinition> getAllItemDefinitions(HierarchyTopic topic)
	{
		List<ItemDefinition> results = new ArrayList<ItemDefinition>();
		if( topic != null )
		{
			if( topic.getAdditionalItemDefs() != null )
			{
				for( EntityScript<ItemDefinition> query : topic.getAdditionalItemDefs() )
				{
					results.add(new ItemDefinition(query.getEntity().getId()));
				}
			}

			if( topic.getInheritedItemDefs() != null )
			{
				for( EntityScript<ItemDefinition> query : topic.getInheritedItemDefs() )
				{
					results.add(new ItemDefinition(query.getEntity().getId()));
				}
			}
		}
		return results;
	}

	private List<Schema> getAllSchemas(HierarchyTopic topic)
	{
		List<Schema> results = new ArrayList<Schema>();
		if( topic != null )
		{
			if( topic.getAdditionalSchemas() != null )
			{
				for( EntityScript<Schema> query : topic.getAdditionalSchemas() )
				{
					results.add(new Schema(query.getEntity().getId()));
				}
			}

			if( topic.getInheritedSchemas() != null )
			{
				for( EntityScript<Schema> query : topic.getInheritedSchemas() )
				{
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
	public List<HierarchyTreeNode> listTreeNodes(long parentTopicID)
	{
		HierarchyTopic parent = parentTopicID <= 0 ? null : getHierarchyTopic(parentTopicID);

		List<HierarchyTopic> childTopics = getChildTopics(parent);

		Collection<HierarchyTopic> topicsGrantedEdit = aclManager.filterNonGrantedObjects(EDIT_PRIV_LIST, childTopics);

		List<HierarchyTreeNode> results = new ArrayList<HierarchyTreeNode>();
		for( HierarchyTopic childTopic : getChildTopics(parent) )
		{
			HierarchyTreeNode childNode = new HierarchyTreeNode();
			childNode.setId(childTopic.getId());
			childNode.setName(CurrentLocale.get(childTopic.getName()));
			childNode.setGrantedEditTopic(topicsGrantedEdit.contains(childTopic));
			results.add(childNode);
		}
		return results;
	}

	@Override
	public String getFullFreetextQuery(HierarchyTopic topic)
	{
		return searchSetService.getFreetextQuery(new SearchSetAdapter(topic));
	}

	@Override
	public FreeTextBooleanQuery getSearchClause(HierarchyTopic topic, Map<String, String> virtualisationValues)
	{
		return searchSetService.getSearchClauses(new SearchSetAdapter(topic), virtualisationValues);
	}

	@Override
	@Transactional
	public void move(long childID, long parentID, int position)
	{
		HierarchyTopic child = getHierarchyTopic(childID);
		HierarchyTopic parent = getHierarchyTopic(parentID);
		insert(child, parent, position);
	}

	/**
	 * parentUuid may be null
	 */
	@Override
	@Transactional
	public void move(String childUuid, String parentUuid, int index)
	{
		HierarchyTopic child = getHierarchyTopicByUuid(childUuid);
		HierarchyTopic parent = parentUuid != null ? getHierarchyTopicByUuid(parentUuid) : null;
		insert(child, parent, index);
	}

	/**
	 * Inserts a child into a parent at the given position.
	 */
	protected void insert(final HierarchyTopic child, final HierarchyTopic parent, final int position)
	{
		List<HierarchyTopic> children = getChildTopics(parent);
		child.setInstitution(CurrentInstitution.get());

		// zap the all parents collection as this is re-initialised during save
		child.setAllParents(null);

		// Remove the child if it is already in this parent
		if( children.contains(child) )
		{
			children.remove(child);
		}

		// Add the child back into the desired position
		if( position >= children.size() )
		{
			children.add(child); // Add to end
		}
		else if( position <= 0 )
		{
			children.add(0, child); // Add to start
		}
		else
		{
			children.add(position, child); // Add to position
		}

		// Update the positions of the children and save
		for( int i = 0; i < children.size(); i++ )
		{
			HierarchyTopic c = children.get(i);
			c.setListPosition(i);
			if( !c.equals(child) && Objects.equals(parent, c.getParent()) )
			{
				dao.updateWithNoParentChange(c);
			}
			else
			{
				c.setParent(parent);
				dao.saveOrUpdate(c);
			}
		}
	}

	private <T extends BaseEntity, U extends EntityScript<T>> void removeEntity(List<U> queries, T entity)
	{
		for( Iterator<U> iter = queries.iterator(); iter.hasNext(); )
		{
			EntityScript<T> script = iter.next();
			if( Objects.equals(script.getEntity(), entity) )
			{
				iter.remove();
			}
		}
	}

	@Override
	@RequiresPrivilegeWithNoTarget(priv = "EDIT_HIERARCHY_TOPIC")
	public String importRootTopic(String xml, boolean newids, boolean useSecurity)
	{
		String returnXml = removeCoursesNode(new PropBagEx(xml));
		return startImportTask(returnXml, null, newids, useSecurity);
	}

	@Override
	@RequiresPrivilege(priv = "EDIT_HIERARCHY_TOPIC")
	public String importTopic(String xml, HierarchyTreeNode topicInto, boolean newids, boolean useSecurity)
	{
		String returnXml = removeCoursesNode(new PropBagEx(xml));
		return startImportTask(returnXml, topicInto, newids, useSecurity);
	}

	// removing the "courses" nodes from the older version of exported topic XML
	// (Bug #6737)
	private String removeCoursesNode(PropBagEx xml)
	{
		xml.deleteNode("topic/courses");
		if( xml.nodeExists("children/com.tle.beans.hierarchy.ExportedHierarchyNode") )
		{
			PropBagEx childXml = xml.getSubtree("children");
			for( PropBagEx bag : childXml.iterator("com.tle.beans.hierarchy.ExportedHierarchyNode") )
			{
				removeCoursesNode(bag);
			}
		}
		return xml.toString();
	}

	private String startImportTask(String xml, HierarchyTreeNode topicInto, boolean newids, boolean useSecurity)
	{
		return taskService.startTask(new BeanClusteredTask(null, HierarchyTaskFactory.class, "createImportTask",
			CurrentUser.getUserState(), xml, topicInto == null ? -1l : topicInto.getId(), newids, useSecurity));
	}

	@Override
	public ImportStatus getImportStatus(String taskUuid)
	{
		TaskStatus ts = taskService.getTaskStatus(taskUuid);

		String error = ts.getErrorMessage();
		if( error != null )
		{
			throw new RuntimeException(error);
		}

		List<Serializable> taskLog = ts.getTaskLog();
		if( !Check.isEmpty(taskLog) )
		{
			return ImportStatus.error((ValidationError) taskLog.get(0));
		}

		if( ts.isFinished() )
		{
			return ImportStatus.finished();
		}

		return ImportStatus.progress(ts.getDoneWork(), ts.getMaxWork());
	}

	@Override
	@SecureOnCall(priv = "EDIT_HIERARCHY_TOPIC")
	public String exportTopic(HierarchyTreeNode node, boolean withSecurity)
	{
		return taskService.startTask(new BeanClusteredTask(null, HierarchyTaskFactory.class, "createExportTask",
			CurrentUser.getUserState(), node.getId(), withSecurity));
	}

	@Override
	public ExportStatus getExportStatus(String taskUuid)
	{
		TaskStatus ts = taskService.getTaskStatus(taskUuid);

		String error = ts.getErrorMessage();
		if( error != null )
		{
			throw new RuntimeException(error);
		}

		if( ts.isFinished() )
		{
			return ExportStatus.finished(((URL) ts.getTaskLog().get(0)).toString());
		}

		return ExportStatus.progress(ts.getDoneWork(), ts.getMaxWork());
	}

	@Override
	public XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = xmlService.createDefault(getClass().getClassLoader());
			xstream.registerConverter(new BaseEntityXmlConverter(registry));
			xstream.registerConverter(new ItemXmlConverter(itemService));
		}
		return xstream;
	}

	@Override
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		Item item = new Item();
		item.setId(event.getKey());
		dao.removeReferencesToItem(item);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(Schema schema)
	{
		for( HierarchyTopic topic : dao.getTopicsReferencingSchema(schema) )
		{
			removeEntity(topic.getAdditionalSchemas(), schema);
			removeEntity(topic.getInheritedSchemas(), schema);

			dao.updateWithNoParentChange(topic);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(ItemDefinition entity)
	{
		for( HierarchyTopic topic : dao.getTopicsReferencingItemDefinition(entity) )
		{
			removeEntity(topic.getAdditionalItemDefs(), entity);
			removeEntity(topic.getInheritedItemDefs(), entity);

			dao.updateWithNoParentChange(topic);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void removeReferences(PowerSearch powerSearch)
	{
		for( HierarchyTopic topic : dao.getTopicsReferencingPowerSearch(powerSearch) )
		{
			topic.setAdvancedSearch(null);
			dao.updateWithNoParentChange(topic);
		}
	}

	@Override
	public List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId)
	{
		return dao.getDynamicKeyResource(dynamicHierarchyId, CurrentInstitution.get());
	}

	@Override
	public List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId, String itemUuid,
		int itemVersion)
	{
		return dao.getDynamicKeyResource(dynamicHierarchyId, itemUuid, itemVersion, CurrentInstitution.get());
	}

	@Override
	public Collection<String> getTopicIdsWithKeyResource(Item item)
	{
		Collection<String> ids = new ArrayList<String>();

		String uuid = item.getUuid();
		int version = item.getVersion();
		List<HierarchyTopicDynamicKeyResources> dynamicKeyResources = dao.getDynamicKeyResource(uuid, version,
			CurrentInstitution.get());
		List<HierarchyTopic> keyResources = dao.findKeyResource(item);

		if( dynamicKeyResources != null )
		{
			for( HierarchyTopicDynamicKeyResources k : dynamicKeyResources )
			{
				ids.add(k.getDynamicHierarchyId());
			}
		}
		if( keyResources != null )
		{
			for( HierarchyTopic h : keyResources )
			{
				ids.add(h.getUuid());
			}
		}
		return ids;
	}
}
