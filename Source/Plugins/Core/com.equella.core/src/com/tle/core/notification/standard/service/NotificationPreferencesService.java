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

package com.tle.core.notification.standard.service;

import java.util.Collection;
import java.util.Map;
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

	Multimap<String, String> getOptedOutCollectionsForUsers(Collection<String> userUuid);

	void setWatchedCollections(Set<String> watches);

	void setOptedOutCollections(Set<String> defs);
}
