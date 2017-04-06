package com.tle.core.hierarchy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.hierarchy.RemoteHierarchyService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.search.VirtualisableAndValue;

/**
 * @author Nicholas Read
 */
public interface HierarchyService extends RemoteHierarchyService
{
	LanguageBundle getHierarchyTopicName(long topicID);

	String getFullFreetextQuery(HierarchyTopic topic);

	FreeTextBooleanQuery getSearchClause(HierarchyTopic topic, Map<String, String> virtualisationValues);

	HierarchyTopic getHierarchyTopicByUuid(String uuid);

	HierarchyTopic assertViewAccess(HierarchyTopic topic);

	HierarchyTopic getHierarchyTopic(long id);

	List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId);

	List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId, String itemUuid,
		int itemVersion);

	List<HierarchyTopic> getChildTopics(HierarchyTopic topic);

	int countChildTopics(HierarchyTopic topic);

	List<VirtualisableAndValue<HierarchyTopic>> expandVirtualisedTopics(List<HierarchyTopic> topics,
		Map<String, String> mappedValues, Collection<String> collectionUuids);

	void addKeyResource(HierarchyTreeNode node, ItemKey item);

	void addKeyResource(String uuid, ItemKey item);

	void edit(HierarchyTopic topic);

	long addRoot(HierarchyTopic newTopic, int position);

	long add(HierarchyTopic parentTopic, HierarchyTopic newTopic, int position);

	void move(String childUuid, String parentUuid, int offset);

	void delete(HierarchyTopic topic);

	void deleteKeyResources(Item item);

	void deleteKeyResources(String uuid, ItemKey itemId);

	void removeDeletedItemReference(String uuid, int version);

	Collection<String> getTopicIdsWithKeyResource(Item item);

}
