package com.tle.core.activation;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.core.dao.ItemDaoExtension;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Charles O'Farrell
 */
public interface ActivateRequestDao extends GenericDao<ActivateRequest, Long>, ItemDaoExtension
{
	List<ActivateRequest> getAllRequestsForDateRange(String type, Item item, Date start, Date end);

	List<ActivateRequest> getAllRequests(Item item);

	List<ActivateRequest> getAllRequests(String type, Item item);

	List<ActivateRequest> getAllRequestsForItems(String type, Collection<Item> items);

	List<ActivateRequest> getAllActiveRequests(String type, Item item);

	List<ActivateRequest> getAllActiveAndPendingRequests(String type, String attachmentUuid);

	List<ActivateRequest> getAllActiveRequestsForItems(String type, Collection<Item> items);

	List<ActivateRequest> getAllActiveOrPendingRequestsForItems(String type, Collection<Item> items);

	List<ActivateRequest> getAllRequestsForCourse(CourseInfo course);

	List<ActivateRequest> getAllRequestsByStatus(int status);

	void removeRequests(String type, long itemId);

	ActivateRequest getLastActive(String type, Item item, String attachment);

	Collection<ItemId> getAllActivatedItemsForInstitution();

	void deleteAllForItem(Item item);

	List<ActivateRequest> listAll();

	ActivateRequest getByUuid(String requestUuid);

	List<ItemIdKey> getItemKeysForUserActivations(String userId);
}
