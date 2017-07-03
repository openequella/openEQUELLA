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
			boolean sessionAvailable = userSessionService.isSessionAvailable();
			if( sessionAvailable )
			{
				locale = userSessionService.getAttribute(WebConstants.KEY_LOCALE);
			}
			if( locale == null )
			{
				HttpServletRequest request = userSessionService.getAssociatedRequest();
				Locale preferredLocale = userPreferenceService.getPreferredLocale(request);
				locale = new LocaleData(preferredLocale, languageService.isRightToLeft(preferredLocale));
			}
			if( sessionAvailable )
			{
				userSessionService.setAttribute(WebConstants.KEY_LOCALE, locale);
			}
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