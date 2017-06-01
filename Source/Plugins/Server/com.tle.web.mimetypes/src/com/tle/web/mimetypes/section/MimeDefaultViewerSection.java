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

package com.tle.web.mimetypes.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import net.sf.json.JSONObject;

import com.google.common.collect.Lists;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.AfterTreeLookup;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.ListOption;
import com.tle.web.sections.standard.MappedStrings;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;
import com.tle.web.sections.standard.renderers.list.BooleanListRenderer;
import com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer;
import com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer;
import com.tle.web.template.DialogTemplate;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewItemService;

@SuppressWarnings("nls")
@Bind
public class MimeDefaultViewerSection extends AbstractPrototypeSection<MimeDefaultViewerSection.DefaultViewerModel>
	implements
		HtmlRenderer,
		MimeEditExtension,
		AfterTreeLookup
{
	private static final JSCallable OPEN_FUNC = new ExternallyDefinedFunction("openConfig", 7, new IncludeFile(
		ResourcesService.getResourceHelper(MimeDefaultViewerSection.class).url("scripts/viewers.js")),
		JQueryCore.PRERENDER);

	private static final String VIEWERS = "viewers"; //$NON-NLS-1$

	private final Map<String, ResourceViewerConfigDialog> configDialogs = new HashMap<String, ResourceViewerConfigDialog>();

	@PlugKey("viewers.head.default")
	private static Label LABEL_HEADER_DEFAULT;
	@PlugKey("viewers.head.enabled")
	private static Label LABEL_HEADER_ENABLED;
	@PlugKey("viewers.head.name")
	private static Label LABEL_HEADER_NAME;
	@PlugKey("viewers.configure")
	private static Label LABEL_CONFIG_BUTTON;

	@Inject
	private ViewItemService viewItemService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private DialogTemplate dialogTemplate;

	@TreeLookup
	private MimeDetailsSection detailsSection;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "dv")
	private SingleSelectionList<NameValue> defaultViewer;
	@Component(name = "ev")
	private MultiSelectionList<NameValue> enabledViewers;
	@Component(name = "vc")
	private MappedStrings viewerConfigs;
	@Component(name = "dvt")
	private Table defaultViewersTable;

	public static class DefaultViewerModel
	{
		private Collection<ResourceViewerConfigDialog> dialogs;

		public Collection<ResourceViewerConfigDialog> getDialogs()
		{
			return dialogs;
		}

		public void setDialogs(Collection<ResourceViewerConfigDialog> dialogs)
		{
			this.dialogs = dialogs;
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		DefaultViewerConfigDialog defaultDialog = componentFactory.createComponent(id,
			"config", tree, DefaultViewerConfigDialog.class, true); //$NON-NLS-1$
		defaultDialog.setTemplate(dialogTemplate);
		List<NameValue> viewers = viewItemService.getViewerNames();
		for( NameValue nameValue : viewers )
		{
			String viewerId = nameValue.getValue();
			ResourceViewer viewer = viewItemService.getViewer(viewerId);
			configDialogs.put(viewerId, viewer.createConfigDialog(id, tree, defaultDialog));
		}

		defaultViewersTable.setColumnHeadings(LABEL_HEADER_DEFAULT, LABEL_HEADER_ENABLED, LABEL_HEADER_NAME, null);
		defaultViewersTable.setColumnSorts(Sort.NONE, Sort.NONE, Sort.PRIMARY_ASC, Sort.NONE);

		ViewerListModel viewerListModel = new ViewerListModel();
		defaultViewer.setListModel(viewerListModel);
		enabledViewers.setListModel(viewerListModel);
		defaultViewer.setAlwaysSelect(true);
		defaultViewer.setDefaultRenderer(RendererConstants.BOOLEANLIST);
		enabledViewers.setDefaultRenderer(RendererConstants.BOOLEANLIST);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new DefaultViewerModel();
	}

	public class ViewerListModel extends DynamicHtmlListModel<NameValue>
	{
		@Override
		protected Iterable<NameValue> populateModel(SectionInfo info)
		{
			List<NameValue> viewers = Lists.newArrayList();
			TextField mimeTypeField = detailsSection.getType();
			List<NameValue> nvs = viewItemService.getViewerNames();
			FakeMimeTypeResource mimeResource = new FakeMimeTypeResource(mimeTypeField.getValue(info));
			for( NameValue viewer : nvs )
			{
				String viewerId = viewer.getValue();
				ResourceViewer resViewer = viewItemService.getViewer(viewerId);
				if( resViewer == null || resViewer.supports(info, mimeResource) )
				{
					viewers.add(viewer);
				}
			}
			return viewers;
		}
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		if( entry != null )
		{
			Map<String, String> attr = entry.getAttributes();
			String defaultId = attr.get(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
			if( defaultId == null )
			{
				defaultId = MimeTypeConstants.VAL_DEFAULT_VIEWERID;
			}
			defaultViewer.setSelectedStringValue(info, defaultId);
			List<String> enabledList = new ArrayList<String>(mimeTypeService.getListFromAttribute(entry,
				MimeTypeConstants.KEY_ENABLED_VIEWERS, String.class));
			if( !attr.containsKey(MimeTypeConstants.KEY_DISABLE_FILEVIEWER) )
			{
				enabledList.add(MimeTypeConstants.VAL_DEFAULT_VIEWERID);
			}
			enabledViewers.setSelectedStringValues(info, enabledList);
			Map<String, String> configMap = new HashMap<String, String>();
			for( String viewer : enabledList )
			{
				configMap.put(viewer, attr.get(MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX + viewer));
			}
			viewerConfigs.setValuesMap(info, configMap);
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		String viewerId = defaultViewer.getSelectedValueAsString(info);
		Map<String, String> attr = entry.getAttributes();
		if( !Check.isEmpty(viewerId) && !viewerId.equals(MimeTypeConstants.VAL_DEFAULT_VIEWERID) )
		{
			attr.put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, viewerId);
		}
		else
		{
			attr.remove(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
		}

		Set<String> enabledSet = new HashSet<String>(enabledViewers.getSelectedValuesAsStrings(info));
		if( !enabledSet.contains(MimeTypeConstants.VAL_DEFAULT_VIEWERID) )
		{
			attr.put(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true"); //$NON-NLS-1$
		}
		else
		{
			attr.remove(MimeTypeConstants.KEY_DISABLE_FILEVIEWER);
		}
		mimeTypeService.clearAllForPrefix(entry, MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX);

		Map<String, String> configMap = viewerConfigs.getValuesMap(info);
		for( String viewer : enabledSet )
		{
			String configJSON = configMap.get(viewer);
			if( !Check.isEmpty(configJSON) )
			{
				attr.put(MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX + viewer, configJSON);
			}
		}
		enabledSet.remove(MimeTypeConstants.VAL_DEFAULT_VIEWERID);
		mimeTypeService.setListAttribute(entry, MimeTypeConstants.KEY_ENABLED_VIEWERS, enabledSet);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "dv"; //$NON-NLS-1$
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Map<ResourceViewerConfigDialog, ResourceViewerConfigDialog> dialogs = new IdentityHashMap<ResourceViewerConfigDialog, ResourceViewerConfigDialog>();
		BooleanListRenderer defaultViewerRenderer = (BooleanListRenderer) renderSection(context, defaultViewer);
		BooleanListRenderer enabledViewerRenderer = (BooleanListRenderer) renderSection(context, enabledViewers);

		TableState tableState = defaultViewersTable.getState(context);
		int i = 0;
		List<ListOption<NameValue>> defaultViewers = defaultViewerRenderer.renderOptionList(context);
		List<ListOption<NameValue>> enabledViewerList = enabledViewerRenderer.renderOptionList(context);
		for( ListOption<NameValue> defaultOption : defaultViewers )
		{
			ListOption<NameValue> enabledOption = enabledViewerList.get(i);
			NameValue viewer = defaultOption.getOption().getObject();
			String viewerId = viewer.getValue();
			String defaultConfig = JSONObject.fromObject(new ResourceViewerConfig()).toString();
			HtmlComponentState buttonState = new HtmlComponentState(RendererConstants.BUTTON);
			buttonState.addClass("button");
			buttonState.setLabel(LABEL_CONFIG_BUTTON);
			buttonState.setId(getSectionId() + "cb" + viewerId);

			HtmlBooleanState enabledState = enabledOption.getBooleanState();
			CheckboxRenderer enabledCheck = new CheckboxRenderer(enabledState);

			TextFieldRenderer configField = new TextFieldRenderer(viewerConfigs.getValueState(context, viewerId));
			configField.setHidden(true);

			ResourceViewerConfigDialog dialog = configDialogs.get(viewerId);
			ButtonRenderer configButton = null;
			if( dialog != null )
			{
				dialogs.put(dialog, dialog);
				configButton = new ButtonRenderer(buttonState);
				buttonState.setDisabled(!enabledState.isChecked());
				enabledState.setEventHandler(
					JSHandler.EVENT_CHANGE,
					new StatementHandler(configButton.createDisableFunction(), new NotExpression(enabledCheck
						.createGetExpression())));
				AnonymousFunction updateFunc = new AnonymousFunction(new FunctionCallStatement(
					configField.createSetFunction(), new FunctionCallExpression(dialog.getCollectFunction())));

				buttonState.setClickHandler(new OverrideHandler(OPEN_FUNC, configField.createGetExpression(),
					updateFunc, defaultConfig, dialog.getOpenFunction(), dialog.getCloseFunction(), new JQuerySelector(
						dialog.getOkButton()), dialog.getPopulateFunction()));
			}

			//@formatter:off
			tableState.addRow(
					new RadioButtonRenderer(defaultOption.getBooleanState()), 
					enabledCheck, 
					new TextLabel(viewer.getName()), 
					new TableCell(configButton, configField)
				);
			//@formatter:on
			i++;
		}

		DefaultViewerModel model = getModel(context);

		model.setDialogs(dialogs.keySet());
		return viewFactory.createResult("defaultviewer.ftl", context); //$NON-NLS-1$
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return MimeTypesEditSection.TAB_VIEWERS;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	public Table getDefaultViewersTable()
	{
		return defaultViewersTable;
	}

	@Override
	public void afterTreeLookup(SectionTree tree)
	{
		detailsSection.addAjaxId(VIEWERS);
	}
}
