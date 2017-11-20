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

package com.tle.core.events;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public abstract class ApplicationEvent<T extends ApplicationListener> implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum PostTo
	{
		POST_ONLY_TO_SELF, POST_TO_ALL_CLUSTER_NODES, POST_TO_OTHER_CLUSTER_NODES, POST_TO_SELF_SYNCHRONOUSLY
	}

	private final PostTo postTo;

	public ApplicationEvent(PostTo postTo)
	{
		this.postTo = postTo;
	}

	public PostTo getPostTo()
	{
		return postTo;
	}

	public boolean requiresInstitution()
	{
		return false;
	}

	public abstract Class<T> getListener();

	public abstract void postEvent(T listener);
}
