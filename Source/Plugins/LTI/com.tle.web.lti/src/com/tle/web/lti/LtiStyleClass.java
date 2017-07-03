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

package com.tle.web.lti;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.template.section.HtmlStyleClass;

@Bind
@Singleton
public class LtiStyleClass implements HtmlStyleClass
{

	@Override
	public String getStyleClass(SectionInfo info)
	{
		String styleClass = null;
		if( CurrentUser.getUserState() instanceof LtiUserState )
		{
			LtiUserState userState = ((LtiUserState) CurrentUser.getUserState());
			LtiData ltiData = userState.getData();
			styleClass = ltiData.getOAuthData() != null ? ltiData.getOAuthData().getConsumerKey() : "";
			styleClass += ltiData.getToolConsumerInfoProductFamilyCode() != null ? " "
				+ ltiData.getToolConsumerInfoProductFamilyCode() : "";
		}
		return styleClass;
	}

}
