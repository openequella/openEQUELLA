package com.tle.core.activation.service;

import java.util.List;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;

/**
 * @author Aaron
 */
public interface ActivationImplementation
{
	List<ActivateRequest> getAllRequests(Item item);

	String getActivationDescription(ActivateRequest request);

	void validateItem(Item item, boolean ignoreOverrides, boolean skipPercentage);

}
