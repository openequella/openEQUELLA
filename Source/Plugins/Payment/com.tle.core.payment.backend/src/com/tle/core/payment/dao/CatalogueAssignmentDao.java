package com.tle.core.payment.dao;

import java.util.List;

import com.tle.beans.item.Item;
import com.tle.common.payment.entity.Catalogue;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author aholland
 */
public interface CatalogueAssignmentDao extends GenericDao<CatalogueAssignment, Long>
{
	List<CatalogueAssignment> enumerateForItem(Item item);

	List<CatalogueAssignment> enumerateForItem(Item item, boolean blacklist);

	List<CatalogueAssignment> enumerateForCatalogue(Catalogue catalogue);

	boolean addToList(Catalogue catalogue, Item item, boolean blacklist);

	boolean removeFromList(Catalogue catalogue, Item item);
}
