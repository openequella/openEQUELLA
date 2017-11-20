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

package com.tle.web.notification.portal;

import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.web.notification.portal.NotificationFilter.NotificationFilterFactory;
import com.tle.web.workflow.portal.TaskListExtension;
import com.tle.web.workflow.portal.TaskListSubsearch;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@Bind
@Singleton
public class NotificationPortalExtension implements TaskListExtension // NOSONAR
{
	@Inject
	private NotificationFilterFactory factory;

	@SuppressWarnings("nls")
	@Override
	public List<TaskListSubsearch> getTaskFilters()
	{
		Builder<TaskListSubsearch> notificationFilters = ImmutableList.builder();
		notificationFilters.add(factory.create(NotifcationPortalConstants.ID_ALL, "", false));
		notificationFilters.add(factory.create("notewentlive", Notification.REASON_WENTLIVE, true));
		notificationFilters.add(factory.create("notewentlive2", Notification.REASON_WENTLIVE2, true));
		notificationFilters.add(factory.create("notemylive", Notification.REASON_MYLIVE, true));
		notificationFilters.add(factory.create("noterejected", Notification.REASON_REJECTED, true));
		notificationFilters.add(factory.create("notebadurl", Notification.REASON_BADURL, true));
		notificationFilters.add(factory.create("noteoverdue", Notification.REASON_OVERDUE, true));
		notificationFilters.add(factory.create("noteerror", Notification.REASON_SCRIPT_ERROR, true));
		notificationFilters.add(factory.create("noteexecuted", Notification.REASON_SCRIPT_EXECUTED, true));
		return notificationFilters.build();
	}
}
