package com.tle.core.pss.service;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.pss.entity.PssCallbackLog;

public interface PearsonScormServicesCallbackService
{
	void addCallbackLogEntry(PssCallbackLog logEntry);

	void deleteCallbackEntry(int trackingNumber);

	void deleteCallbackEntry(Item item);

	void editCallbackLogEntry(PssCallbackLog logEntry);

	PssCallbackLog getCallbackLogEntry(int trackingNumber);

	PssCallbackLog getCallbackLogEntry(Item item);

	PssCallbackLog getCallbackLogEntryWithFallback(int trackingNumber, ItemId itemId);
}
