package com.tle.core.notification.standard.service;

import java.util.Set;

import com.google.common.collect.Multimap;

public interface NotificationPreferencesService
{
	String WATCHED_ITEMDEFS = "watched.item.definitions"; //$NON-NLS-1$
	String OPTEDOUT_ITEMDEFS = "optedout.item.definitions"; //$NON-NLS-1$
	String NOTIFICATION_PERIOD = "notification.period"; //$NON-NLS-1$

	Multimap<String, String> getWatchedCollectionMap();

	Set<String> getWatchedCollections();

	Set<String> getOptedOutCollections();

	Set<String> getOptedOutCollectionsForUser(String userUuid);

	void setWatchedCollections(Set<String> watches);

	void setOptedOutCollections(Set<String> defs);
}
