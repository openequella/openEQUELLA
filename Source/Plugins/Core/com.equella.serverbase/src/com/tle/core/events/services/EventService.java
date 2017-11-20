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

package com.tle.core.events.services;

import java.util.Collection;

import com.tle.beans.Institution;
import com.tle.core.events.ApplicationEvent;

public interface EventService
{
	void publishApplicationEvent(ApplicationEvent<?> event);

	/**
	 * This will publish the event once for each institution. This only makes
	 * sense from "server" tasks, etc.. that then need to notify all
	 * institutions of a change. It will also fail if you attempt to post a
	 * synchronous event.
	 */
	void publishApplicationEvent(Collection<Institution> institutions, ApplicationEvent<?> event);
}
