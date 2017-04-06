package com.tle.core.services.item.relation;

import java.util.Collection;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
public interface RelationDao extends GenericInstitutionalDao<Relation, Long>
{
	Collection<Relation> getAllByFromItem(Item from);

	Collection<Relation> getAllByToItem(Item to);

	Collection<Relation> getAllByFromItemAndType(Item from, String type);

	Collection<Relation> getAllByType(String type);

	Collection<Long> getAllIdsForInstitution();

	Collection<Relation> getAllMentioningItem(Item item);

	Collection<Relation> getAllByToItemAndType(Item to, String type);

	List<Relation> getAllMentioningItem(ItemKey itemId);
}
