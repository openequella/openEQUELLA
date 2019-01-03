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

package com.tle.web.sections.jquery.libraries;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.DebugSettings;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
@NonNullByDefault
public class JQueryDatepicker implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable DATEPICKER = new JQueryLibraryInclude("jquery.ui.datepicker.js", JQueryUICore.PRERENDER).hasMin();
	public static final PreRenderable PRERENDER = new IncludeFile(JQueryLibraryInclude.urlHelper.url("js/datepicker.js"), DATEPICKER);

	private static final JSCallable SETUP_PICKER_FUNCTION = new ExternallyDefinedFunction("setupPicker", PRERENDER);

	public static final JSCallable DISABLE_PICKER_FUNCTION = new ExternallyDefinedFunction("disablePicker", PRERENDER);

	private static final String IMAGE_URL = ResourcesService.getResourceHelper(JQueryDatepicker.class)
		.url("images/calendar-icon.png");

	public static JSStatements setupPicker(JQuerySelector visibleElement, JQuerySelector invisibleElement,
		Integer timezoneOffset, JSExpression onChangeExpression, JQuerySelector secondDateInvisibleElement,
		boolean primary, String pickerFormat, Long initialDate, String buttonText)
	{
		return Js.call_s(SETUP_PICKER_FUNCTION, visibleElement, invisibleElement, timezoneOffset, onChangeExpression,
			secondDateInvisibleElement, primary, pickerFormat, initialDate, IMAGE_URL, buttonText);
	}

	@Nullable
	public static PreRenderable getLangPackInclude(Locale locale)
	{
		// lang include
		String langPack = findLangPack(CurrentLocale.getLocale());
		if( langPack != null )
		{
			return new JQueryLibraryInclude("i18n/jquery.ui.datepicker-" + langPack + ".js", PRERENDER);
		}
		return null;
	}

	private static final Set<String> LANG_PACKS = new HashSet<String>();

	static
	{
		// Sigh... get things working again
		final String[] packs = new String[]{"af", "ar-DZ", "ar", "be", "bq", "bs", "ca", "cs", "cy-GB", "da", "de",
				"el", "en-AU", "en-GB", "en-NZ", "eo", "es", "et", "eu", "fa", "fi", "fo", "fr-CA", "fr-CH", "fr", "gl",
				"he", "hi", "hr", "hu", "hy", "id", "is", "it", "ja", "ka", "kk", "km", "ko", "ky", "lb", "lt", "lv",
				"mk", "ml", "ms", "nb", "nl-BE", "nl", "nn", "no", "pl", "pt-BR", "pt", "rm", "ro", "ru", "sk", "sl",
				"sq", "sr-SR", "sr", "sv", "ta", "th", "tj", "tr", "uk", "vi", "zh-CN", "zh-HK", "zh-TW"};
		for( String pack : packs )
		{
			LANG_PACKS.add(pack);
		}
	}

	@Nullable
	private static String findLangPack(Locale locale)
	{
		final String lang = locale.getLanguage();
		String country = locale.getCountry();
		if( country != null )
		{
			country = country.toUpperCase();
		}
		final String composite = lang + '-' + country;
		// en-US is the default for datepicker, there is no pack for this
		if( composite.equals("en-US") )
		{
			return null;
		}

		if( LANG_PACKS.contains(composite) )
		{
			return composite;
		}
		if( LANG_PACKS.contains(lang) )
		{
			return lang;
		}
		// look for same lang with a different country
		for( String pack : LANG_PACKS )
		{
			if( pack.startsWith(lang + '-') )
			{
				return pack;
			}
		}

		return null;
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.datepicker.name");
	}

	@Override
	public String getId()
	{
		return "datepicker";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
