package com.tle.web.lti;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
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
