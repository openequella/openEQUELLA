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

package com.tle.core.i18n;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.web.WebConstants;
import com.google.inject.Singleton;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale.AbstractCurrentLocale;
import com.tle.common.i18n.LocaleData;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserSessionService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;

@Bind
@Singleton
public class ServerSideLocaleImplementation extends AbstractCurrentLocale
{
	@Inject
	private LanguageService languageService;
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private UserSessionService userSessionService;

	private static final InheritableThreadLocal<CurrentData> local = new InheritableThreadLocal<CurrentData>();

	@Override
	protected Pair<Locale, String> resolveKey(String key)
	{
		CurrentData cd = getCurrentData();
		return new Pair<Locale, String>(cd.getLocale(), cd.getResourceBundle().getString(key));
	}

	@SuppressWarnings("nls")
	private CurrentData getCurrentData()
	{
		UserState userState = CurrentUser.getUserState();
		CurrentData currentData = local.get();
		if( currentData != null && currentData.getUserState() == userState )
		{
			return currentData;
		}
		LocaleData locale = null;
		if( userState == null )
		{
			locale = new LocaleData(Locale.getDefault(), false);
		}
		else
		{
			locale = LocaleLookup.selectLocale();
		}

		currentData = new CurrentData(userState, locale.getLocale(), locale.isRightToLeft(),
			languageService.getResourceBundle(locale.getLocale(), "resource-centre"));
		local.set(currentData);
		return currentData;
	}

	@Override
	public Locale getLocale()
	{
		return getCurrentData().getLocale();
	}

	@Override
	public boolean isRightToLeft()
	{
		return getCurrentData().isRightToLeft();
	}

	@Override
	public ResourceBundle getResourceBundle()
	{
		return getCurrentData().getResourceBundle();
	}

	@Override
	public String toString(double num)
	{
		return getCurrentData().getNumberFormatter().format(num);
	}

	@Override
	public String toString(long num)
	{
		return getCurrentData().getNumberFormatter().format(num);
	}

	private static class CurrentData
	{
		private final UserState userState;
		private final Locale locale;
		private final boolean rightToLeft;
		private final ResourceBundle resourceBundle;

		private NumberFormat numberFormatter;

		public CurrentData(UserState userState, Locale locale, boolean rightToLeft, ResourceBundle resourceBundle)
		{
			this.userState = userState;
			this.locale = locale;
			this.rightToLeft = rightToLeft;
			this.resourceBundle = resourceBundle;
		}

		public Locale getLocale()
		{
			return locale;
		}

		public boolean isRightToLeft()
		{
			return rightToLeft;
		}

		public ResourceBundle getResourceBundle()
		{
			return resourceBundle;
		}

		public NumberFormat getNumberFormatter()
		{
			if( numberFormatter == null )
			{
				numberFormatter = NumberFormat.getInstance(locale);
			}
			return numberFormatter;
		}

		public UserState getUserState()
		{
			return userState;
		}
	}

	public void clearThreadLocals()
	{
		local.remove();
	}
}