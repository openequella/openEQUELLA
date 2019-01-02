/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionTree;

@Bind
@Singleton
public class UserLinkService
{
	@Inject
	private Provider<UserLinkSection> linkProvider;

	public UserLinkSection register(SectionTree tree, String parentId)
	{
		UserLinkSection uls = tree.lookupSection(UserLinkSection.class, null);
		if( uls == null )
		{
			uls = linkProvider.get();
			tree.registerInnerSection(uls, parentId);
		}
		return uls;
	}
}
