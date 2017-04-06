package com.tle.core.workflow.notification;

import java.util.Set;

import com.google.common.collect.Multimap;

public interface WorkflowPreferencesService
{
	String WATCHED_ITEMDEFS = "watched.item.definitions"; //$NON-NLS-1$
	String NOTIFICATION_PERIOD = "notification.period"; //$NON-NLS-1$

	Multimap<String, String> getWatchedCollectionMap();

	Set<String> getWatchedCollections();

	void setWatchedCollections(Set<String> watches);
}
