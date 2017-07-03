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

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.services.FileSystemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NullExpression;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class RootLanguageSection extends OneColumnLayout<OneColumnLayout.OneColumnLayoutModel>
{
	@PlugKey("language.title")
	private static Label TITLE_LABEL;
	@PlugKey("language.importlangpack.receipt")
	private static Label IMPORT_LANGPACK_RECEIPT_LABEL;
	@PlugKey("language.removelangpack.confirm.delete")
	private static Confirm CONFIRM_REMOVE_LANGPACK;
	@PlugKey("language.removelangpack.receipt")
	private static Label REMOVE_LANGPACK_RECEIPT_LABEL;
	@PlugKey("language.export.defaultfilenameprefix")
	private static String defaultExportFilePrefix;
	@PlugKey("language.removecontriblang.confirm.delete")
	private static Confirm CONFIRM_REMOVE_CONTRIBLANG;
	@PlugKey("language.export.label")
	private static Label EXPORT_LABEL;
	@PlugKey("language.delete.label")
	private static Label DELETE_LABEL;
	@PlugKey("language.locale.default.name")
	private static Label LABEL_LOCALE_DEFAULT_NAME;

	@Component(name = "imprtlnk")
	@PlugKey("importlocale.link")
	private Link importLocaleLink;

	@Component(name = "fiup")
	private FileUpload fileUploader;

	@Component
	@PlugKey("addcontriblang.addlink")
	private Link addContribLangLink;

	@Component
	@Inject
	private AddLanguageDialog addLanguageDialog;

	@Component(stateful = false)
	private SelectionsTable languagePacksTbl;

	@Component(stateful = false)
	private SelectionsTable contributionLanguageTbl;

	@Inject
	private LanguageSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private LanguageService langService;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ReceiptService receiptService;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	private JSCallable deleteLocaleHandler;
	private JSCallable deleteContribHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		importLocaleLink.setDisablable(true);
		importLocaleLink.setClickHandler(events.getNamedHandler("importLocale"));
		importLocaleLink.setStyleClass("add");

		fileUploader.addEventStatements(JSHandler.EVENT_CHANGE,
			jscall(new FunctionCallExpression(importLocaleLink.createDisableFunction(), false)));
		addContribLangLink.setClickHandler(addLanguageDialog.getOpenFunction(), "", "");
		addContribLangLink.setStyleClass("add");
		addLanguageDialog.setOkCallback(new ReloadFunction());

		deleteLocaleHandler = events.getSubmitValuesFunction("removeLocale");
		languagePacksTbl.setSelectionsModel(new LocaleLanguageTableModel());

		deleteContribHandler = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeContribLangRow"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "contriblanglist");
		contributionLanguageTbl.setSelectionsModel(new ContributionLanguageTableModel());

	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		// enabled via change to fileUpload.
		importLocaleLink.setDisabled(info, true);

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "language.ftl", this));
	}

	/**
	 * We can't know the result of the file-save (or file-save-Cancel), and
	 * setting any receipt string here won't necessarily display in any case.
	 * Given that any browsers behaviour will be to present the user with a
	 * save-or-cancel dialog, that would seem to be sufficient feedback for this
	 * operation.
	 * 
	 * @param info
	 * @param key
	 */
	@EventHandlerMethod
	public void exportLocale(SectionInfo info, String key)
	{
		Locale matchingLocale = null;
		for( Locale locale : langService.listAvailableResourceBundles() )
		{
			String localAsStrng = locale.toString();
			if( localAsStrng.equals(key) )
			{
				matchingLocale = locale;
				break;
			}
		}
		if( matchingLocale == null )
		{
			throw new Error("Cannot find locale for " + key);
		}

		try
		{
			String exportedFilename = CurrentLocale.get(defaultExportFilePrefix);
			exportedFilename += matchingLocale.toString() + ".zip";

			HttpServletResponse response = info.getResponse();
			response.addHeader("Content-Disposition", "attachment; filename=\"" + exportedFilename + '"');

			langService.exportLanguagePack(matchingLocale, response.getOutputStream());
			info.setRendered();
		}
		catch( IOException ioe )
		{
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * Removing locale from the table is a complete action (ie, it is NOT a
	 * visual removal carried to completion with a Save button), hence we
	 * precede it with a confirmation alert box.
	 * 
	 * @param info
	 * @param key
	 */
	@EventHandlerMethod
	public void removeLocale(SectionInfo info, String key)
	{
		boolean removed = false;
		List<Locale> persistedLangaugePacks = langService.listAvailableResourceBundles();
		for( Iterator<Locale> iter = persistedLangaugePacks.iterator(); iter.hasNext(); )
		{
			Locale locale = iter.next();
			String localeAsString = locale.toString();
			if( localeAsString.equals(key) )
			{
				langService.deleteLanguagePack(locale);
				iter.remove();
				removed = true;
				break; // there can only be one remove at a time
			}
		}
		if( removed )
		{
			receiptService.setReceipt(REMOVE_LANGPACK_RECEIPT_LABEL);
		}
	}

	@EventHandlerMethod
	public void importLocale(SectionInfo info)
	{
		String uploadFilename = fileUploader.getFilename(info);
		if( !Check.isEmpty(uploadFilename) )
		{
			long filesize = fileUploader.getFileSize(info);

			if( filesize > 0 )
			{
				try
				{
					// upload the file into server's staging area ...
					StagingFile stagingDir = stagingService.createStagingArea();
					InputStream is = fileUploader.getInputStream(info);
					fileSystemService.write(stagingDir, uploadFilename, is, false);
					// ... from where the language service can do its thing.
					langService.importLanguagePack(stagingDir.getUuid(), uploadFilename);
					// gloat.
					receiptService.setReceipt(IMPORT_LANGPACK_RECEIPT_LABEL);
				}
				catch( IOException ioe )
				{
					throw new RuntimeException(ioe);
				}
			}
		}
	}

	@EventHandlerMethod
	public void removeContribLangRow(SectionInfo info, String key)
	{
		List<Language> persistedLanguages = langService.getLanguages();

		boolean altered = false;
		for( Iterator<Language> iter = persistedLanguages.iterator(); iter.hasNext(); )
		{
			Language lang = iter.next();
			if( lang.getLocale().getDisplayName().equals(key) )
			{
				iter.remove();
				altered = true;
				break; // there can only be one remove at a time
			}
		}
		if( altered )
		{
			langService.setLanguages(persistedLanguages);
		}
	}

	private class ContributionLanguageTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			List<Language> langList = langService.getLanguages();
			return Lists.transform(langList, new Function<Language, String>()
			{
				@Override
				public String apply(Language lang)
				{
					return lang.getLocale().getDisplayName();
				}
			});
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String lang,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LabelRenderer(new TextLabel(lang)));
			if( langService.getLanguages().size() > 1 )
			{
				actions.add(makeRemoveAction(null,
					new OverrideHandler(deleteContribHandler, lang).addValidator(CONFIRM_REMOVE_CONTRIBLANG)));
			}
		}
	}

	private class LocaleLanguageTableModel extends DynamicSelectionsTableModel<Locale>
	{
		@Override
		protected List<Locale> getSourceList(SectionInfo info)
		{
			return langService.listAvailableResourceBundles();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, Locale locale,
			List<SectionRenderable> actions, int index)
		{
			final Label name;
			// toString() gets the country, lang and variant and sticks them
			// together. If none of them are populated it returns a blank string
			if( Strings.isNullOrEmpty(locale.toString()) )
			{
				name = LABEL_LOCALE_DEFAULT_NAME;
			}
			else
			{
				name = new TextLabel(locale.getDisplayName());
			}
			selection.setViewAction(new LabelRenderer(name));
			/**
			 * Export handled as a GET rather than a POST, to avoid errors with
			 * 'page already being submitted', hence the BookmarkAndModify
			 * utilisation.
			 */
			Bookmark bookmarkForGET = new BookmarkAndModify(info,
				events.getNamedModifier("exportLocale", locale.toString()));
			actions.add(new LinkRenderer(new HtmlLinkState(EXPORT_LABEL, bookmarkForGET)));
			actions.add(makeAction(DELETE_LABEL,
				new OverrideHandler(deleteLocaleHandler, locale.toString()).addValidator(CONFIRM_REMOVE_LANGPACK)));
		}
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public boolean hasLanguagePacks(SectionInfo info)
	{
		return !Check.isEmpty(langService.listAvailableResourceBundles());
	}

	public SelectionsTable getLanguagePacksTbl()
	{
		return languagePacksTbl;
	}

	public boolean hasContributionLanguages(SectionInfo info)
	{
		return !Check.isEmpty(langService.getLanguages());
	}

	public FileUpload getFileUploader()
	{
		return fileUploader;
	}

	public Link getImportLocaleLink()
	{
		return importLocaleLink;
	}

	public SelectionsTable getContributionLanguageTbl()
	{
		return contributionLanguageTbl;
	}

	public Link getAddContribLangLink()
	{
		return addContribLangLink;
	}
}
