package com.tle.core.activation.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.util.UtcDate;

public interface ActivationService
{
	List<ActivateRequest> getAllRequestsForDateRange(String activationType, Item item, Date start, Date end);

	List<ActivateRequest> getAllRequests(String activationType, Item item);

	ActivateRequest getRequest(long id);

	List<ActivateRequest> activate(String activationType, Item key, List<ActivateRequest> requests,
		boolean skipPercentage);

	void activateAll(String activationType, Map<Long, List<ActivateRequest>> requestMap, boolean ignorePercentage);

	void deactivate(String activationType, ActivateRequest request);

	void delete(String activationType, ActivateRequest request);

	void delete(String activationType, long id);

	List<ActivateRequest> getAllCurrentActivations(String activationType, Item item);

	List<ActivateRequest> filterDeletableRequest(List<ActivateRequest> requests);

	List<ActivateRequest> filterEditableRequest(List<ActivateRequest> requests);

	List<ActivateRequest> getByCourse(CourseInfo course);

	/**
	 * Returns conceptual dates
	 * 
	 * @param course
	 * @return
	 */
	UtcDate[] getDefaultCourseDates(CourseInfo course);

	boolean isActive(String activationType, Item item, String attachmentUuid);

	boolean isActiveOrPending(String activationType, String attachmentUuid);

	String getStatusKey(int status);

	Collection<ItemId> getAllActivatedItemsForInstitution();

	void ensureStatesForItem(Item item);

	void ensureStates(List<ActivateRequest> requests);

	ActivateRequest getRequest(String requestUuid);

	Set<Item> filterActivatableItems(Set<Item> portionItems);

	List<String> getImplementationTypes();

	List<ActivateRequest> getAllRequests(Item item);

	List<ActivateRequest> getAllByStatus(int status);

	List<ActivateRequest> listAll();

	String getActivationDescription(ActivateRequest request);

	void updateActivation(ActivateRequest request);

	void validateItem(String activationType, Item item, boolean ignoreOverrides, boolean skipPercentage);

	void deactivateByUuid(String activationUuid);

	List<ActivateRequest> getAllCurrentAndPendingActivations(String activationType, String attachmentUuid);

	boolean attachmentIsSelectableForCourse(String activationType, String attachmentUuid, String courseCode);

}
