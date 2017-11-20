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

package com.tle.web.connectors.section;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.ConnectorTypeDescriptor;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author Aaron
 */

@SuppressWarnings("nls")
@Bind
public class ConnectorContributeSection
	extends
		AbstractEntityContributeSection<ConnectorEditingBean, Connector, ConnectorContributeSection.ConnectorContributeModel>
{
	private Map<String, EntityEditor<ConnectorEditingBean, Connector>> editorMap;

	@PlugKey("editor.label.pagetitle.new")
	private static Label LABEL_CREATE_PAGETITLE;
	@PlugKey("editor.label.pagetitle.edit")
	private static Label LABEL_EDIT_PAGETITLE;
	@PlugKey("editor.dropdown.option.choosetype")
	private static String CHOOSE_TYPE_KEY;

	@Inject
	private ConnectorService connectorService;

	private PluginTracker<EntityEditor<ConnectorEditingBean, Connector>> editorTracker;

	@Component(name = "ct")
	private SingleSelectionList<ConnectorTypeDescriptor> connectorTypes;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;

	private UpdateDomFunction updateFunction;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ConnectorContributeModel model = getModel(context);

		model.setPageTitle(getPageTitle(context));

		final EntityEditor<ConnectorEditingBean, Connector> ed = getEditor(context);
		if( ed != null )
		{
			// check edit priv
			final ConnectorEditingBean editedConnector = ed.getEditedEntity(context);
			if( editedConnector.getId() == 0 )
			{
				ensureCreatePriv(context);
			}
			else if( !canEdit(context, editedConnector) )
			{
				throw accessDenied(getEditPriv());
			}

			HelpAndScreenOptionsSection.addHelp(context, ed.renderHelp(context));
			model.setEditorRenderable(ed.renderEditor(context));
		}
		else
		{
			ensureCreatePriv(context);
		}

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", view.createResult("editconnector.ftl", context));
		return templateResult;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		connectorTypes.setListModel(new DynamicHtmlListModel<ConnectorTypeDescriptor>()
		{
			@Override
			protected Iterable<ConnectorTypeDescriptor> populateModel(SectionInfo info)
			{
				return connectorService.listAllAvailableTypes();
			}

			@Override
			protected Option<ConnectorTypeDescriptor> getTopOption()
			{
				return new KeyOption<ConnectorTypeDescriptor>(CHOOSE_TYPE_KEY, "", null);
			}

			@Override
			protected Option<ConnectorTypeDescriptor> convertToOption(SectionInfo info, ConnectorTypeDescriptor obj)
			{
				return new NameValueOption<ConnectorTypeDescriptor>(
					new BundleNameValue(obj.getNameKey(), obj.getType()), obj);
			}
		});

		updateFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("typeSelected"),
			"connectorEditor", "actions");
		connectorTypes.addChangeEventHandler(new OverrideHandler(updateFunction));
	}

	@Override
	protected AbstractEntityService<ConnectorEditingBean, Connector> getEntityService()
	{
		return connectorService;
	}

	@Override
	protected Collection<EntityEditor<ConnectorEditingBean, Connector>> getAllEditors()
	{
		editorMap = editorTracker.getNewBeanMap();
		return editorMap.values();
	}

	@Override
	protected EntityEditor<ConnectorEditingBean, Connector> getEditor(SectionInfo info)
	{
		final String type = connectorTypes.getSelectedValueAsString(info);
		final ConnectorContributeModel model = getModel(info);
		if( !Check.isEmpty(type) )
		{
			final EntityEditor<ConnectorEditingBean, Connector> ed = editorMap.get(type);
			model.setEditor(ed);
			return ed;
		}
		return null;
	}

	@EventHandlerMethod
	public void typeSelected(SectionInfo info)
	{
		final EntityEditor<ConnectorEditingBean, Connector> ed = getEditor(info);
		if( ed != null )
		{
			// start new session
			ed.create(info);
		}
	}

	/**
	 * @param info
	 * @param connectorUuid
	 * @param type
	 */
	@Override
	public void startEdit(SectionInfo info, String connectorUuid, boolean clone)
	{
		final Connector connector = connectorService.getForEdit(connectorUuid);
		final String type = connector.getLmsType();
		final ConnectorContributeModel model = getModel(info);
		final EntityEditor<ConnectorEditingBean, Connector> ed = editorMap.get(type);
		model.setEditor(ed);
		model.setEditing(true);
		ed.edit(info, connectorUuid, clone);
		connectorTypes.setSelectedStringValue(info, type);
	}

	@Override
	public void returnFromEdit(SectionInfo info, boolean cancelled)
	{
		super.returnFromEdit(info, cancelled);
		connectorTypes.setSelectedStringValue(info, null);
	}

	@Override
	protected String getCreatePriv()
	{
		return ConnectorConstants.PRIV_CREATE_CONNECTOR;
	}

	@Override
	protected String getEditPriv()
	{
		return ConnectorConstants.PRIV_EDIT_CONNECTOR;
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		return LABEL_CREATE_PAGETITLE;
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		return LABEL_EDIT_PAGETITLE;
	}

	@Override
	public Class<ConnectorContributeModel> getModelClass()
	{
		return ConnectorContributeModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "cc";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ConnectorContributeModel(info);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		editorTracker = new PluginTracker<EntityEditor<ConnectorEditingBean, Connector>>(pluginService,
			"com.tle.web.connectors", "connectorEditor", "id");
		editorTracker.setBeanKey("class");
	}

	public SingleSelectionList<ConnectorTypeDescriptor> getConnectorTypes()
	{
		return connectorTypes;
	}

	public UpdateDomFunction getUpdateFunction()
	{
		return updateFunction;
	}

	public class ConnectorContributeModel
		extends
			AbstractEntityContributeSection<ConnectorEditingBean, Connector, ConnectorContributeModel>.EntityContributeModel
	{
		private final SectionInfo info;
		private EntityEditor<ConnectorEditingBean, Connector> editor;

		public ConnectorContributeModel(SectionInfo info)
		{
			this.info = info;
		}

		@Override
		public EntityEditor<ConnectorEditingBean, Connector> getEditor()
		{
			final String type = connectorTypes.getSelectedValueAsString(info);
			if( editor == null && type != null )
			{
				editor = editorMap.get(type);
			}
			return editor;
		}
	}
}
