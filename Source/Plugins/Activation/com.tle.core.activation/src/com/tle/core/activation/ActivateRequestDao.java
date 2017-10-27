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

package com.tle.core.activation;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.core.hibernate.dao.GenericDao;
import com.tle.core.item.dao.ItemDaoExtension;

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
