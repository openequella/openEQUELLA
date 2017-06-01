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

package com.tle.core.taxonomy.impl;

import com.tle.core.events.ApplicationEvent;

public class TaxonomyModifiedEvent extends ApplicationEvent<TaxonomyModifiedListener>
{
	private static final long serialVersionUID = 1L;

	private final String taxonomyUuid;

	public TaxonomyModifiedEvent(String taxonomyUuid)
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
		this.taxonomyUuid = taxonomyUuid;
	}

	public String getTaxonomyUuid()
	{
		return taxonomyUuid;
	}

	@Override
	public Class<TaxonomyModifiedListener> getListener()
	{
		return TaxonomyModifiedListener.class;
	}

	@Override
	public void postEvent(TaxonomyModifiedListener listener)
	{
		listener.taxonomyModifiedEvent(this);
	}
}
