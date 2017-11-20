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

package com.tle.web.language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class AddLanguageDialog extends AbstractOkayableDialog<AddLanguageDialog.AddLanguageModel>
{
	@PlugKey("addlanguage.dialog.title.title")
	private static Label LABEL_TITLE;
	@PlugKey("addlanguage.dialog.none")
	private static String defaultLangCountryNone;

	@Component(name = "lnge")
	private SingleSelectionList<NameValue> languageList;

	@Component(name = "cntr")
	private SingleSelectionList<NameValue> countryList;

	@Inject
	private LanguageService langService;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	private JSCallable reloadParent;
	private static final String DIALOG_HEIGHT = "360px";

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "aldl";
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showItem");
	}

	@EventHandlerMethod
	public void showItem(SectionInfo info, String highlightedLang, String highlightedCntry)
	{
		AddLanguageModel model = getModel(info);
		if( !Check.isEmpty(highlightedLang) )
		{
			model.setHighlightedLanguage(highlightedLang);
		}

		if( !Check.isEmpty(highlightedCntry) )
		{
			model.setHighlightedCountry(highlightedCntry);
		}

		super.showDialog(info);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
		reloadParent = addParentCallable(new ReloadFunction(false));

		String[] isoLanguageCodes = Locale.getISOLanguages();
		List<NameValue> isoLangArray = new ArrayList<NameValue>(isoLanguageCodes.length);
		for( String isoLanguageCode : isoLanguageCodes )
		{
			Locale localeForLang = new Locale(isoLanguageCode);
			String displayName = localeForLang.getDisplayLanguage() + " [" + isoLanguageCode + ']';
			isoLangArray.add(new NameValue(displayName, isoLanguageCode));
		}
		isoLangArray.add(0, new NameValue(CurrentLocale.get(defaultLangCountryNone), null));
		languageList.setListModel(new SimpleHtmlListModel<NameValue>(isoLangArray));

		String[] isoCountryCodes = Locale.getISOCountries();
		List<NameValue> isoCntryArray = new ArrayList<NameValue>(isoCountryCodes.length);
		for( String isoCountryCode : isoCountryCodes )
		{
			// Can't pass null, but an empty string OK for language param
			Locale localeForCountry = new Locale("", isoCountryCode);
			String displayName = localeForCountry.getDisplayCountry() + " [" + isoCountryCode + ']';
			isoCntryArray.add(new NameValue(displayName, isoCountryCode));
		}
		isoCntryArray.add(0, new NameValue(CurrentLocale.get(defaultLangCountryNone), null));
		countryList.setListModel(new SimpleHtmlListModel<NameValue>(isoCntryArray));
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		AddLanguageModel model = getModel(context);
		if( !Check.isEmpty(model.getHighlightedLanguage()) )
		{
			languageList.setSelectedStringValue(context, model.getHighlightedLanguage());
		}

		if( !Check.isEmpty(model.getHighlightedCountry()) )
		{
			countryList.setSelectedStringValue(context, model.getHighlightedCountry());
		}

		return viewFactory.createResult("addlanguage-dialog.ftl", this);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		JSExpression aGetLanguageExpression = languageList.createGetExpression();
		JSExpression aGetCountryExpression = countryList.createGetExpression();
		return events.getNamedHandler("saveContribLanguages", aGetLanguageExpression, aGetCountryExpression);
	}

	/**
	 * Do nothing if both inputs are empty, but otherwise if either is null,
	 * create a non-null empty string to get a valid Language object (and
	 * comparison)
	 */
	@EventHandlerMethod
	public void saveContribLanguages(SectionInfo info, @Nullable String langStr, @Nullable String cntrStr)
	{
		List<Language> persistedLangs = langService.getLanguages();
		String noneSelected = CurrentLocale.get(defaultLangCountryNone);
		if( langStr == null || langStr.equals(noneSelected) )
		{
			langStr = "";
		}

		if( cntrStr == null || cntrStr.equals(noneSelected) )
		{
			cntrStr = "";
		}

		if( !(Check.isEmpty(langStr) && Check.isEmpty(cntrStr)) )
		{
			Language newLang = new Language();
			newLang.setLanguage(langStr);
			newLang.setCountry(cntrStr);
			boolean pairExists = false;
			for( Language alang : persistedLangs )
			{
				String alangLang = alang.getLanguage() != null ? alang.getLanguage() : "";
				String alangCntry = alang.getCountry() != null ? alang.getCountry() : "";
				if( alangLang.equals(langStr) && alangCntry.equals(cntrStr) )
				{
					pairExists = true;
					break;
				}
			}

			if( !pairExists )
			{
				// Inflate the existing lang set and save it
				persistedLangs.add(newLang);
				langService.setLanguages(persistedLangs);
			}
		}
		closeDialog(info, reloadParent);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public String getHeight()
	{
		return DIALOG_HEIGHT;
	}

	public SingleSelectionList<NameValue> getLanguageList()
	{
		return languageList;
	}

	public SingleSelectionList<NameValue> getCountryList()
	{
		return countryList;
	}

	@Override
	public AddLanguageModel instantiateDialogModel(SectionInfo info)
	{
		return new AddLanguageModel();
	}

	public static class AddLanguageModel extends DialogModel
	{
		@Bookmarked
		private String highlightedLanguage;
		@Bookmarked
		private String highlightedCountry;

		public String getHighlightedLanguage()
		{
			return highlightedLanguage;
		}

		public void setHighlightedLanguage(String highlightedLanguage)
		{
			this.highlightedLanguage = highlightedLanguage;
		}

		public String getHighlightedCountry()
		{
			return highlightedCountry;
		}

		public void setHighlightedCountry(String highlightedCountry)
		{
			this.highlightedCountry = highlightedCountry;
		}
	}
}
