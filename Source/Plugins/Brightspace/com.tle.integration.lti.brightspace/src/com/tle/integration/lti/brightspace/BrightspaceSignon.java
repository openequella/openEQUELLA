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

package com.tle.integration.lti.brightspace;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * Not sign-on as such, but an extension of signon.do which doesn't do sign-on either,
 * but sets up an integration selection session
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class BrightspaceSignon extends AbstractPrototypeSection<SingleSignonForm> implements AfterParametersListener
{
	public static final String KEY_SESSION_TYPE = "BrightspaceSignon.SessionType";

	public static final String SESSION_TYPE_NAVBAR = "navbar";
	public static final String SESSION_TYPE_QUICKLINK = "quicklink";
	public static final String SESSION_TYPE_COURSEBUILDER = "coursebuilder";
	public static final String SESSION_TYPE_INSERTSTUFF = "insertstuff";

	//Not really nullable...
	@Nullable
	private String type;
	@Nullable
	private String defaultAction;
	@Inject
	private BrightspaceIntegration brightspaceIntegration;

	@Override
	public void afterParameters(SectionInfo info, ParametersEvent event)
	{
		final SingleSignonForm model = getModel(info);

		String formDataAction = model.getAction();
		if( formDataAction == null )
		{
			model.setAction(defaultAction);
		}
		info.setAttribute(KEY_SESSION_TYPE, type);

		brightspaceIntegration.setupSingleSignOn(info, model);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public SingleSignonForm instantiateModel(SectionInfo info)
	{
		return new SingleSignonForm();
	}

	public void setType(String type)
	{
		this.type = type;

		if( type.equals(SESSION_TYPE_NAVBAR) )
		{
			defaultAction = "structured";
		}
		else if( type.equals(SESSION_TYPE_QUICKLINK) )
		{
			defaultAction = "selectOrAdd";
		}
		else if( type.equals(SESSION_TYPE_COURSEBUILDER) )
		{
			defaultAction = "structured";
		}
		else if( type.equals(SESSION_TYPE_INSERTSTUFF) )
		{
			defaultAction = "searchThin";
		}
	}
}
