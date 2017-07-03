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

package com.tle.web.htmleditor.tinymce.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.common.ScriptContext;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.htmleditor.HtmlEditorConfiguration;
import com.tle.common.htmleditor.HtmlEditorToolbarConfig;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.scripting.ScriptContextFactory;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.jackson.mapper.LenientMapperExtension;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.HtmlEditorControl;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.TinyMceAddonProvider;
import com.tle.web.htmleditor.tinymce.TinyMceControl;
import com.tle.web.htmleditor.tinymce.TinyMceEditorSection;
import com.tle.web.htmleditor.tinymce.TinyMceModel;
import com.tle.web.htmleditor.tinymce.actions.TinyMceActionSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.js.ObjectExpressionDeserialiser;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.TableRenderer;
import com.tle.web.spellcheck.dictionary.DictionaryService;
import com.tle.web.spellcheck.dictionary.TLEDictionary;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(TinyMceService.class)
@Singleton
public class TinyMceServiceImpl implements TinyMceService
{
	private static final Logger LOGGER = Logger.getLogger(TinyMceService.class);
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(TinyMceServiceImpl.class);

	@PlugKey("dropdown.format")
	private static Label LABEL_FORMAT_SELECT;
	@PlugKey("dropdown.fontfamily")
	private static Label LABEL_FONT_FAMILY_SELECT;
	@PlugKey("dropdown.fontsize")
	private static Label LABEL_FONT_SIZE_SELECT;
	@PlugKey("dropdown.styles")
	private static Label LABEL_STYLE_SELECT;
	@PlugKey("button.")
	private static String KEY_BUTTON_PREFIX;

	static
	{
		PluginResourceHandler.init(TinyMceServiceImpl.class);
	}

	// ! Needs to be up to date with scripts/tinymce/langs
	private static final Map<String, String> LANG_PACKS;

	static
	{
		// Attempts at getting this dynamically failed, feel free to try
		final Map<String, String> tempMap = Maps.newHashMap();
		tempMap.put("en", "en");
		tempMap.put("ar", "ar");
		tempMap.put("az", "az");
		tempMap.put("be", "be");
		tempMap.put("bg", "bg");
		tempMap.put("bn", "bn");
		tempMap.put("br", "br");
		tempMap.put("bs", "bs");
		tempMap.put("ca", "ca");
		tempMap.put("ch", "ch");
		tempMap.put("cn", "cn");
		tempMap.put("cs", "cs");
		tempMap.put("cy", "cy");
		tempMap.put("da", "da");
		tempMap.put("de", "de");
		tempMap.put("dv", "dv");
		tempMap.put("el", "el");
		tempMap.put("eo", "eo");
		tempMap.put("es", "es");
		tempMap.put("et", "et");
		tempMap.put("eu", "eu");
		tempMap.put("fa", "fa");
		tempMap.put("fi", "fi");
		tempMap.put("fr", "fr");
		tempMap.put("gl", "gl");
		tempMap.put("gu", "gu");
		tempMap.put("he", "he");
		tempMap.put("hi", "hi");
		tempMap.put("hr", "hr");
		tempMap.put("hu", "hu");
		tempMap.put("hy", "hy");
		tempMap.put("ia", "ia");
		tempMap.put("id", "id");
		tempMap.put("is", "is");
		tempMap.put("it", "it");
		tempMap.put("ja", "ja");
		tempMap.put("ka", "ka");
		tempMap.put("kk", "kk");
		tempMap.put("kl", "kl");
		tempMap.put("km", "km");
		tempMap.put("ko", "ko");
		tempMap.put("lb", "lb");
		tempMap.put("lt", "lt");
		tempMap.put("lv", "lv");
		tempMap.put("mk", "mk");
		tempMap.put("ml", "ml");
		tempMap.put("mn", "mn");
		tempMap.put("ms", "ms");
		tempMap.put("my", "my");
		tempMap.put("nb", "nb");
		tempMap.put("nl", "nl");
		tempMap.put("nn", "nn");
		tempMap.put("no", "no");
		tempMap.put("pl", "pl");
		tempMap.put("ps", "ps");
		tempMap.put("pt", "pt");
		tempMap.put("ro", "ro");
		tempMap.put("sc", "sc");
		tempMap.put("se", "se");
		tempMap.put("si", "si");
		tempMap.put("sk", "sk");
		tempMap.put("sl", "sl");
		tempMap.put("sq", "sq");
		tempMap.put("sr", "sr");
		tempMap.put("sv", "sv");
		tempMap.put("sy", "sy");
		tempMap.put("ta", "ta");
		tempMap.put("te", "te");
		tempMap.put("th", "th");
		tempMap.put("tn", "tn");
		tempMap.put("tr", "tr");
		tempMap.put("tt", "tt");
		tempMap.put("tw", "tw");
		tempMap.put("uk", "uk");
		tempMap.put("ur", "ur");
		tempMap.put("vi", "vi");
		tempMap.put("zh-cn", "zh-cn");
		tempMap.put("zh-tw", "zh-tw");
		tempMap.put("zh", "zh");
		tempMap.put("zu", "zu");
		LANG_PACKS = Collections.unmodifiableMap(tempMap);
	}

	private static final CssInclude TLE_SKIN = CssInclude
		.include(RESOURCES.url("scripts/tinymce/themes/advanced/skins/tle/ui.css")).make();
	private static final CssInclude TLE_SILVER_SKIN = CssInclude
		.include(RESOURCES.url("scripts/tinymce/themes/advanced/skins/tle/ui_silver.css")).prerender(TLE_SKIN).make();

	private static final IncludeFile TINY_MCE_JS = new IncludeFile(RESOURCES.url("scripts/tinymce/tiny_mce.js"),
		JQueryCore.PRERENDER);
	private static final IncludeFile TINY_MCE_DEV_JS = new IncludeFile(RESOURCES.url("scripts/tinymce/tiny_mce_src.js"),
		JQueryCore.PRERENDER);

	private static final String DEFAULT_PLUGINS = "inlinepopups,paste,wordcount,fullscreen";

	private final IncludeFile htmlEditorJs = new IncludeFile(RESOURCES.url("scripts/htmleditor.js"), new PreRenderable()
	{
		@Override
		public void preRender(PreRenderContext info)
		{
			if( configService.isDebuggingMode() )
			{
				info.preRender(TINY_MCE_DEV_JS);
			}
			else
			{
				info.preRender(TINY_MCE_JS);
			}
		}
	});

	private static final List<List<String>> DEFAULT_BUTTON_LAYOUT;

	static
	{
		final List<List<String>> def = Lists.newArrayList();
		final List<String> row1 = Collections.unmodifiableList(Lists.newArrayList("bold", "italic", "underline",
			"strikethrough", "|", "justifyleft", "justifycenter", "justifyright", "justifyfull", "|", "formatselect",
			"hr", "removeformat", "visualaid", "|", "sub", "sup", "|", "charmap", "link", "unlink", "anchor", "image"));
		final List<String> row2 = Collections.unmodifiableList(
			Lists.newArrayList("code", "|", "undo", "redo", "cut", "copy", "paste", "selectall", "|", "table", "|",
				"row_props", "cell_props", "|", "row_before", "row_after", "delete_row", "|", "col_before", "col_after",
				"delete_col", "|", "split_cells", "merge_cells", "|", "forecolorpicker", "backcolorpicker"));
		// Not ideal, it 'knows' about tle_fileuploader, tle_reslinker and
		// tle_scrapbookpicker when it shouldn't
		final List<String> row3 = Collections.unmodifiableList(
			Lists.newArrayList("bullist", "numlist", "|", "outdent", "indent", "|", "tle_fileuploader", "tle_reslinker",
				"tle_scrapbookpicker", "|", "fontselect", "fontsizeselect", "|", "spellchecker"));
		def.add(row1);
		def.add(row2);
		def.add(row3);
		DEFAULT_BUTTON_LAYOUT = Collections.unmodifiableList(def);
	}

	// Can't be static due to reliance on configService
	private final JSCallAndReference initialiseMce = new ExternallyDefinedFunction("initialiseMce", 4, htmlEditorJs);
	private final JSCallAndReference registerEditor = new ExternallyDefinedFunction("registerEditor", 1, htmlEditorJs);
	private final JSCallAndReference saveContents = new ExternallyDefinedFunction("saveContents", 1, htmlEditorJs);
	private final JSCallAndReference setDisabled = new ExternallyDefinedFunction("setTinyMceDisabled", 3, htmlEditorJs);
	private final JSCallAndReference toggleTinyMceFullscreen = new ExternallyDefinedFunction("toggleTinyMceFullscreen",
		2, htmlEditorJs);

	private static Map<String, HtmlEditorButtonDefinition> STANDARD_BUTTONS = null;
	private static final Object BUTTON_LOCK = new Object();

	@Inject
	private DictionaryService dictionaryService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private Provider<TinyMceControl> controlProvider;
	@Inject
	private Provider<TinyMceEditorSection> editorProvider;
	@Inject
	private RendererFactory rendererFactory;
	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private ObjectMapperService objectMapperService;

	private PluginTracker<TinyMceAddonProvider> addOnTracker;

	@Override
	public void preRender(PreRenderContext context, AbstractRenderedComponent<?> textAreaComponent, TinyMceModel model)
	{
		if( !context.getBooleanAttribute("TinyMceService.initialiseMce") )
		{
			final List<TinyMceAddOn> addOns = model.getAddOns();
			final String userlang = model.getLang();
			final String directionality = (model.getDirectionality() == null ? "ltr" : model.getDirectionality());

			final ObjectExpression options = new ObjectExpression();

			// TODO: cache it? it can only change when a plugin is
			// created/deleted/disabled
			final Map<String, HtmlEditorButtonDefinition> buttonDefs = Maps.newHashMap(getStandardButtons());
			for( TinyMceAddOn addOn : addOns )
			{
				for( HtmlEditorButtonDefinition button : addOn.getButtons(context) )
				{
					buttonDefs.put(button.getId(), button);
				}
			}

			final Set<String> usedPluginIds = Sets.newHashSet();
			final HtmlEditorConfiguration editorConfig = htmlEditorService.getEditorConfig();
			final List<HtmlEditorToolbarConfig> rows = editorConfig.getRows();
			int rowIndex = 0;
			// -----------TOOLBAR---------------
			options.put("theme_advanced_toolbar_location", "top");
			options.put("theme_advanced_toolbar_align", directionality.equals("ltr") ? "left" : "right");
			options.put("theme_advanced_statusbar_location", "bottom");
			options.put("theme_advanced_resizing", true);
			for( HtmlEditorToolbarConfig row : rows )
			{
				rowIndex++;
				final String buttons = Utils.join(row.getButtons().toArray(), ",");
				options.put("theme_advanced_buttons" + rowIndex, buttons);
				for( String buttonId : row.getButtons() )
				{
					HtmlEditorButtonDefinition buttonDef = buttonDefs.get(buttonId);
					if( buttonDef != null && buttonDef.getPluginId() != null )
					{
						usedPluginIds.add(buttonDef.getPluginId());
					}
				}
			}

			options.put("plugins", getPlugins(usedPluginIds));
			options.put("theme", "advanced");
			options.put("skin", "tle");
			options.put("skin_variant", "silver");
			options.put("mode", "none");
			options.put("language", LANG_PACKS.containsKey(userlang) ? userlang : "en");

			options.put("directionality", directionality);
			options.put("gecko_spellcheck", false);
			options.put("convert_urls", false);
			options.put("relative_urls", false);
			options.put("submit_patch", false);
			options.put("valid_elements", "*[*]");
			options.put("invalid_elements", "head,body,title,meta");

			if( usedPluginIds.contains("table") )
			{
				options.put("table_styles", "Header 1=header1;Header 2=header2;Header 3=header3");
				options.put("table_cell_styles",
					"Header 1=header1;Header 2=header2;Header 3=header3;Table Cell=tableCel1");
				options.put("table_row_styles",
					"Header 1=header1;Header 2=header2;Header 3=header3;Table Row=tableRow1");
				options.put("table_cell_limit", 400);
				options.put("table_row_limit", 20);
				options.put("table_col_limit", 20);
			}

			if( usedPluginIds.contains("paste") )
			{
				options.put("paste_block_drop", true);
				options.put("paste_convert_middot_lists", true);
				options.put("paste_auto_cleanup_on_paste", true);
				options.put("paste_remove_styles_if_webkit", true);
				options.put("paste_retain_style_properties", "all");
				options.put("paste_strip_class_attributes", "mso");
				options.put("paste_remove_spans", false);
			}

			if( usedPluginIds.contains("spellchecker") )
			{
				options.put("spellchecker_languages", "+" + model.getLanguagesList());
				options.put("spellchecker_rpc_url", institutionService.institutionalise("spellcheck/"));
			}

			options.put("body_class", "htmleditor");
			final String css = htmlEditorService.getStylesheetRelativeUrl();
			if( css != null )
			{
				options.put("content_css", institutionService.institutionalise(css));
			}

			final ScriptContextFactory scriptContextFactory = model.getScriptContextFactory();
			for( TinyMceAddOn addOn : addOns )
			{
				// Add-ons without buttons should be added regardless. If you
				// don't want them then disable them.
				if( usedPluginIds.contains(addOn.getId()) || addOn.getButtons(context).isEmpty() )
				{
					final ScriptContext scriptContext = scriptContextFactory.createScriptContext();
					scriptContext.addScriptObject("pluginUrl", addOn.getBaseUrl());
					scriptContext.addScriptObject("resourcesUrl", addOn.getResourcesUrl());
					scriptContext.addScriptObject("institutionUrl", institutionService.getInstitutionUrl().toString());

					addOn.setScriptContext(context, scriptContext);
					context.preRender(addOn);
					final ObjectExpression init = addOn.getInitialisation(context);
					if( init != null )
					{
						options.merge(init);
					}
				}
			}

			// Override options with editoroptions.txt (now 'advanced html
			// options')
			options.merge(getEditorOptions(editorConfig));

			String actionUrl = model.getActionUrl();
			actionUrl = actionUrl + (actionUrl.contains("?") ? "&" : "?") + "action=";
			final JSStatements setupMce = Js.call_s(initialiseMce, model.getBaseUrl(), actionUrl, options,
				getAddOnsArray(addOns, usedPluginIds));

			context.addReadyStatements(setupMce);
			context.setAttribute("TinyMceService.initialiseMce", true);
		}

		textAreaComponent.addReadyStatements(context, Js.call_s(registerEditor, Jq.$(textAreaComponent)));
	}

	@Override
	public JSHandler getPreSubmitHandler(ElementId textAreaComponent)
	{
		return new StatementHandler(Js.call_s(saveContents, Jq.$(textAreaComponent)));
	}

	@Override
	public JSHandler getToggleFullscreeenHandler(ElementId textAreaComponent, ElementId link)
	{
		return new OverrideHandler(Js.call_s(toggleTinyMceFullscreen, Jq.$(textAreaComponent), Jq.$(link)));
	}

	private String getPlugins(Set<String> usedPlugins)
	{
		final StringBuilder plugins = new StringBuilder(DEFAULT_PLUGINS);
		// include all plugins required by buttons
		String used = Utils.join(usedPlugins.toArray(), ",");
		if( !Check.isEmpty(used) )
		{
			plugins.append(",").append(used);
		}
		return plugins.toString();
	}

	private ArrayExpression getAddOnsArray(List<TinyMceAddOn> addOns, Set<String> usedPluginIds)
	{
		final List<ObjectExpression> addOnExpressionList = Lists.newArrayList();
		final List<TinyMceAddOn> addOnCopy = Lists.newArrayList(addOns);
		final Iterator<TinyMceAddOn> addOnIterator = addOnCopy.iterator();
		while( addOnIterator.hasNext() )
		{
			final TinyMceAddOn addOn = addOnIterator.next();
			if( usedPluginIds.contains(addOn.getId()) )
			{
				final ObjectExpression addOnObj = new ObjectExpression();
				addOnObj.put("uid", addOn.getId());
				addOnObj.put("jsUrl", addOn.getJsUrl());
				addOnExpressionList.add(addOnObj);
			}
		}
		return new ArrayExpression(addOnExpressionList.toArray());
	}

	private ObjectExpression getEditorOptions(HtmlEditorConfiguration config)
	{
		final String options = config.getEditorOptions();
		if( options == null || options.trim().length() == 0 )
		{
			return null;
		}

		try
		{
			final ObjectMapper jsonMapper = objectMapperService.createObjectMapper(LenientMapperExtension.NAME);
			final SimpleModule module = new SimpleModule("equella", new Version(1, 0, 0, null));
			module.addDeserializer(ObjectExpression.class, new ObjectExpressionDeserialiser());
			jsonMapper.registerModule(module);
			final ObjectExpression obj = jsonMapper.readValue(options, ObjectExpression.class);
			return obj; // NOSONAR (kept local variable for readability)
		}
		catch( IOException io )
		{
			// Invalid editor options. Should never have happened.
			LOGGER.error("Invalid HTML editor options", io);
			return null;
		}
	}

	@Override
	public List<TinyMceAddOn> getAddOns()
	{
		List<TinyMceAddOn> addons = Lists.newArrayList();
		List<TinyMceAddonProvider> providers = addOnTracker.getBeanList();
		for( TinyMceAddonProvider provider : providers )
		{
			addons.addAll(provider.getAddons());
		}
		return addons;
	}

	@Override
	public HtmlEditorInterface createEditor()
	{
		return editorProvider.get();
	}

	@Override
	public HtmlEditorControl createControl()
	{
		return controlProvider.get();
	}

	@Override
	public void populateModel(SectionInfo info, TinyMceModel model, Map<String, String> properties,
		boolean restrictedCollections, boolean restrictedDynacolls, boolean restrictedSearches,
		boolean restrictedContributables, Map<Class<?>, Set<String>> collectionUuids, Set<String> contributableUuids)
	{
		model.setProperties(properties);
		model.setWidth(properties.get("width"));
		model.setHeight(properties.get("height"));

		final String excludedAddOns = properties.get(HtmlEditorInterface.EXCLUDED_ADDONS);
		final List<TinyMceAddOn> addOns = new ArrayList<TinyMceAddOn>(getAddOns());
		final List<String> exclusions = !Check.isEmpty(excludedAddOns) ? Arrays.asList(excludedAddOns.split(","))
			: null;
		for( Iterator<TinyMceAddOn> iter = addOns.iterator(); iter.hasNext(); )
		{
			TinyMceAddOn addOn = iter.next();
			if( (exclusions != null && exclusions.contains(addOn.getId())) || !addOn.isEnabled() )
			{
				iter.remove();
			}
		}
		model.setAddOns(addOns);

		// set up the languages for spellchecking
		StringBuilder languagesList = new StringBuilder();
		boolean first = true;
		for( TLEDictionary dict : dictionaryService.getTLEDictionaryList() )
		{
			if( !first )
			{
				languagesList.append(",");
			}
			languagesList.append(dict.getLanguage()).append("=").append(dict.getCode());
			first = false;
		}
		model.setLanguagesList(languagesList.toString());

		// Setup the action URL
		SectionInfo actionInfo = info.createForward("/access/mceaction.do");
		TinyMceActionSection actionSection = actionInfo.lookupSection(TinyMceActionSection.class);
		actionSection.setSessionId(actionInfo, properties.get("sessionId"));
		actionSection.setPageId(actionInfo, properties.get("pageId"));
		actionSection.setSearchableUuids(actionInfo, restrictedCollections, restrictedDynacolls, restrictedSearches,
			restrictedContributables, collectionUuids, contributableUuids);

		model.setActionUrl(actionInfo.getPublicBookmark().getHref());
		model.setBaseUrl(institutionService.institutionalise(RESOURCES.url("scripts/tinymce")));
		model.setLang(getClosestLangPack(LANG_PACKS));
		model.setDirectionality(CurrentLocale.isRightToLeft() ? "rtl" : "ltr");

		try
		{
			model.setRows(Integer.valueOf(properties.get("rows")));
		}
		catch( Exception e )
		{
			// ignore
		}
	}

	private String getClosestLangPack(Map<String, String> available)
	{
		if( available == null || available.isEmpty() )
		{
			// Something has gone badly wrong, let's just run it in English
			return "en";
		}
		Locale locale = CurrentLocale.getLocale();
		String lang = LangUtils.getClosestObjectForLocale(available, locale);
		return lang == null ? "en" : lang;
		// Don't think it can ever be null, but just in case
	}

	@Override
	public JSCallable getDisableFunction(ElementId element, ElementId fullscreenLinkElement)
	{
		final ScriptVariable dis = new ScriptVariable("dis");
		final JSAssignable setDisabledThis = Js
			.function(Js.call_s(setDisabled, Jq.$(element), Jq.$(fullscreenLinkElement), dis), dis);
		return CallAndReferenceFunction.get(setDisabledThis, element);
	}

	@Override
	public LinkedHashMap<String, HtmlEditorButtonDefinition> getButtons(SectionInfo info)
	{
		final LinkedHashMap<String, HtmlEditorButtonDefinition> buttons = Maps.newLinkedHashMap(getStandardButtons());
		// customs
		for( TinyMceAddOn addOn : getAddOns() )
		{
			for( HtmlEditorButtonDefinition button : addOn.getButtons(info) )
			{
				buttons.put(button.getId(), button);
			}
		}
		return buttons;
	}

	@Override
	public List<List<String>> getDefaultButtonConfiguration()
	{
		return DEFAULT_BUTTON_LAYOUT;
	}

	private Map<String, HtmlEditorButtonDefinition> getStandardButtons()
	{
		if( STANDARD_BUTTONS == null )
		{
			synchronized( BUTTON_LOCK )
			{
				if( STANDARD_BUTTONS == null )
				{
					final Map<String, HtmlEditorButtonDefinition> buttons = Maps.newLinkedHashMap();
					addButton(buttons, "bold", null, 0);
					addButton(buttons, "italic", null, 0);
					addButton(buttons, "underline", null, 0);
					addButton(buttons, "strikethrough", null, 0);
					addButton(buttons, "justifyleft", null, 0);
					addButton(buttons, "justifycenter", null, 0);
					addButton(buttons, "justifyright", null, 0);
					addButton(buttons, "justifyfull", null, 0);
					addDdButton(buttons, "formatselect", null, "mce_formatselect", LABEL_FORMAT_SELECT, 0, true);
					addButton(buttons, "hr", null, 0);
					addButton(buttons, "removeformat", null, 0);
					addButton(buttons, "visualaid", null, 0);
					addButton(buttons, "sub", null, 0);
					addButton(buttons, "sup", null, 0);
					addButton(buttons, "charmap", null, 0);
					addButton(buttons, "link", "advlink", 0);
					addButton(buttons, "unlink", "advlink", 0);
					addButton(buttons, "anchor", "advlink", 0);
					addButton(buttons, "image", "advimage", 0);

					addButton(buttons, "code", null, 1);
					addButton(buttons, "undo", null, 1);
					addButton(buttons, "redo", null, 1);
					addButton(buttons, "cut", null, 1);
					addButton(buttons, "copy", null, 1);
					addButton(buttons, "paste", null, 1);
					addButton(buttons, "selectall", "paste", 1);
					addButton(buttons, "table", "table", 1);
					addButton(buttons, "row_props", "table", 1);
					addButton(buttons, "cell_props", "table", 1);
					addButton(buttons, "row_before", "table", 1);
					addButton(buttons, "row_after", "table", 1);
					addButton(buttons, "delete_row", "table", 1);
					addButton(buttons, "col_before", "table", 1);
					addButton(buttons, "col_after", "table", 1);
					addButton(buttons, "delete_col", "table", 1);
					addButton(buttons, "split_cells", "table", 1);
					addButton(buttons, "merge_cells", "table", 1);
					addButton(buttons, "forecolorpicker", null, 1);
					addButton(buttons, "backcolorpicker", null, 1);

					addButton(buttons, "bullist", null, 2);
					addButton(buttons, "numlist", null, 2);
					addButton(buttons, "outdent", null, 2);
					addButton(buttons, "indent", null, 2);

					addDdButton(buttons, "fontselect", null, "mce_fontselect", LABEL_FONT_FAMILY_SELECT, 2, true);
					addDdButton(buttons, "fontsizeselect", null, "mce_fontsizeselect", LABEL_FONT_SIZE_SELECT, 2, true);
					addButton(buttons, "spellchecker", "spellchecker", 2);

					DivRenderer renderer = new DivRenderer("tleSkin",
						new SpanRenderer("mceSeparator", new LabelRenderer(new TextLabel("&nbsp;", true))));
					HtmlEditorButtonDefinition seppo = new HtmlEditorButtonDefinition("|", null, renderer,
						new KeyLabel(KEY_BUTTON_PREFIX + "separator"), 2, false);
					buttons.put("|", seppo);

					addButton(buttons, "advhr", "advhr", -1);
					addButton(buttons, "emotions", "emotions", -1);
					// addButton(buttons, "fullpage", -1);
					addButton(buttons, "fullscreen", "fullscreen", -1);
					// addButton(buttons, "iespell", -1);
					addButton(buttons, "media", "media", -1);
					addButton(buttons, "nonbreaking", "nonbreaking", -1);
					addButton(buttons, "pagebreak", "pagebreak", -1);
					addButton(buttons, "preview", "preview", -1);
					addButton(buttons, "print", "print", -1);
					addButton(buttons, "visualchars", "visualchars", -1);
					addButton(buttons, "blockquote", null, -1);
					addButton(buttons, "help", null, -1);
					addButton(buttons, "newdocument", null, -1);
					// searchreplace
					addButton(buttons, "search", "searchreplace", -1);
					addButton(buttons, "replace", "searchreplace", -1);
					// insertdatetime
					addButton(buttons, "insertdate", "insertdatetime", -1);
					addButton(buttons, "inserttime", "insertdatetime", -1);
					// directionality
					addButton(buttons, "ltr", "directionality", -1);
					addButton(buttons, "rtl", "directionality", -1);
					// layer
					addButton(buttons, "moveforward", "layer", -1);
					addButton(buttons, "movebackward", "layer", -1);
					addButton(buttons, "absolute", "layer", -1);
					addButton(buttons, "insertlayer", "layer", -1);
					// save
					addButton(buttons, "save", "save", -1);
					addButton(buttons, "cancel", "save", -1);
					// style
					addButton(buttons, "styleprops", "style", -1);
					// xhtmlxtras
					addButton(buttons, "cite", "xhtmlxtras", -1);
					addButton(buttons, "abbr", "xhtmlxtras", -1);
					addButton(buttons, "acronym", "xhtmlxtras", -1);
					addButton(buttons, "ins", "xhtmlxtras", -1);
					addButton(buttons, "del", "xhtmlxtras", -1);
					addButton(buttons, "attribs", "xhtmlxtras", -1);
					// template
					addButton(buttons, "template", "template", -1);

					addDdButton(buttons, "styleselect", null, "mce_styleselect", LABEL_STYLE_SELECT, -1, true);
					STANDARD_BUTTONS = Collections.unmodifiableMap(buttons);
				}
			}
		}
		return STANDARD_BUTTONS;
	}

	private void addButton(Map<String, HtmlEditorButtonDefinition> buttons, String id, String pluginId, int row)
	{
		addButton(buttons, id, pluginId, row, true);
	}

	private void addButton(Map<String, HtmlEditorButtonDefinition> buttons, String id, String pluginId, int row,
		boolean singleton)
	{
		HtmlEditorButtonDefinition bold = new HtmlEditorButtonDefinition(id, pluginId,
			new StandardButtonRenderer("mce_" + id), new KeyLabel(KEY_BUTTON_PREFIX + id), row, singleton);
		buttons.put(id, bold);
	}

	private void addDdButton(Map<String, HtmlEditorButtonDefinition> buttons, String id, String pluginId,
		String cssClass, Label text, int row, boolean singleton)
	{
		HtmlEditorButtonDefinition bold = new HtmlEditorButtonDefinition(id, pluginId,
			new DropdownButtonRenderer(cssClass, text), new KeyLabel(KEY_BUTTON_PREFIX + id), row, singleton);
		buttons.put(id, bold);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		addOnTracker = new PluginTracker<TinyMceAddonProvider>(pluginService, RESOURCES.pluginId(), "addons", "id");
		addOnTracker.setBeanKey("class");
	}

	protected static TableState selectButtonState(String buttonCss, Label buttonText)
	{
		TableState state = new TableState();
		state.setNoColGroup(true);
		state.makePresentation();
		state.addClass("mceListBox mceListBoxEnabled " + buttonCss);

		DivRenderer atag = new DivRenderer("a", "mceText", buttonText);
		TableCell ddtext = new TableCell(atag);
		ddtext.addClass("mceLeft");

		DivRenderer aopen = new DivRenderer("a", "mceOpen",
			new SpanRenderer(new SpanRenderer("mceIconOnly", new LabelRenderer(new TextLabel("&nbsp;", true)))));
		TableCell dddrop = new TableCell(aopen);
		dddrop.addClass("mceRight");
		state.addRow(ddtext, dddrop);
		return state;
	}

	public class DropdownButtonRenderer extends DivRenderer
	{
		public DropdownButtonRenderer(String buttonCss, Label buttonText)
		{
			super("tleSkin",
				new DivRenderer("span", new TableRenderer(selectButtonState(buttonCss, buttonText), rendererFactory)
				{
					// sonar fix - remove the useless overriding method
				}));
		}
	}

	public class StandardButtonRenderer extends DivRenderer
	{
		public StandardButtonRenderer(String buttonCss)
		{
			super("tleSkin tleSkinSilver",
				new DivRenderer("a", "mceButton", new DivRenderer("span", "mceIcon " + buttonCss, null)));
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			super.preRender(info);
			info.preRender(TLE_SILVER_SKIN);
		}
	}
}
