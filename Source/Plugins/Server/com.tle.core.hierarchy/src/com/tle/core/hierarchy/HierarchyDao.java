package com.tle.core.hierarchy;

import java.util.Collection;
import java.util.List;

import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources;
import com.tle.beans.item.Item;
import com.tle.core.dao.AbstractTreeDao;

/**
 * @author Nicholas Read
 */
public interface HierarchyDao extends AbstractTreeDao<HierarchyTopic>
{
	List<HierarchyTopic> enumerateAll();

	LanguageBundle getHierarchyTopicName(long topicID);

	Collection<HierarchyTopic> getTopicsReferencingItemDefinition(ItemDefinition itemDefinition);

	Collection<HierarchyTopic> getTopicsReferencingSchema(Schema schema);

	List<HierarchyTopic> getTopicsReferencingPowerSearch(PowerSearch powerSearch);

	HierarchyTopic findByUuid(String uuid, Institution institution);

	void removeReferencesToItem(Item item);

	void removeReferencesToItem(Item item, long id);

	void saveDynamicKeyResources(HierarchyTopicDynamicKeyResources entity);

	List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId, Institution institution);

	List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String itemUuid, int itemVersion,
		Institution institution);

	List<HierarchyTopicDynamicKeyResources> getDynamicKeyResource(String dynamicHierarchyId, String itemUuid,
		int itemVersion, Institution institution);

	List<HierarchyTopicDynamicKeyResources> getAllDynamicKeyResources(Institution institution);

	List<HierarchyTopic> findKeyResource(Item item);

	void removeDynamicKeyResource(String topicId, String itemUuid, int itemVersion);

	void deleteAllDynamicKeyResources(Institution institution);

	void removeDynamicKeyResource(String itemUuid, int itemVersion, Institution institution);

}
