/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewable.servlet;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.NewViewableItemState;
import com.tle.web.viewable.ViewableItem;

@Bind
@Singleton
public class LegacyItemServlet extends ItemServlet
{
	@Override
	protected ItemUrlParser getItemUrlParser()
	{
		return new NewItemUrlParser()
		{
			@Override
			protected void setupContext()
			{
				String itemdef = partList.get(0);
				context = request.getServletPath().substring(1) + '/' + itemdef + '/';
				partList = partList.subList(1, partList.size());
			}

			@Override
			public ViewableItem createViewableItem()
			{
				NewDefaultViewableItem viewableItem = (NewDefaultViewableItem) super.createViewableItem();
				NewViewableItemState state = viewableItem.getState();
				state.setContext(context);
				return viewableItem;
			}

		};
	}
}
