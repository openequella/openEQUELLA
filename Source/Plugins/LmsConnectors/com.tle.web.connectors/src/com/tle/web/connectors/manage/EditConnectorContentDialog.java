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

package com.tle.web.connectors.manage;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.connectors.manage.EditConnectorContentDialog.EditConnectorModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.CloseWindowResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class EditConnectorContentDialog extends AbstractOkayableDialog<EditConnectorModel>
{
	private static final Logger LOGGER = Logger.getLogger(EditConnectorContentDialog.class);

	@PlugKey("manage.edit.dialog.title")
	private static Label TITLE;
	@PlugKey("manage.edit.dialog.validation.entername")
	private static Label LABEL_ENTER_NAME;
	@PlugKey("manage.edit.dialog.genericerror")
	private static Label LABEL_GENERIC_EDIT_ERROR;
	@PlugKey("manage.edit.dialog.error")
	private static String KEY_EDIT_ERROR;

	@Inject
	private ConnectorRepositoryService repositoryService;

	@Component
	private TextField nameField;
	@Component
	private TextField descriptionField;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private ConnectorManagementQuerySection querySection;

	public EditConnectorContentDialog()
	{
		setAjax(true);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("edit/edit-dialog.ftl", this);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE;
	}

	@Override
	public EditConnectorModel instantiateDialogModel(SectionInfo info)
	{
		return new EditConnectorModel();
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("editContent");
	}

	@EventHandlerMethod
	public void editContent(SectionInfo info, ConnectorItemKey key, String title, String description)
	{
		final EditConnectorModel model = getModel(info);
		model.setConnectorItemKey(key);
		nameField.setValue(info, title);

		Connector connector = querySection.getConnector(info);
		boolean editDescription = repositoryService.supportsEditDescription(connector.getLmsType());
		model.setEditDescription(editDescription);
		if( editDescription )
		{
			descriptionField.setValue(info, description);
		}

		showDialog(info);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		final JSHandler handler = events.getNamedHandler("saveContent");
		handler.addValidator(Js.validator(Js.notEquals(nameField.createGetExpression(), Js.str("")))
			.setFailureStatements(Js.alert_s(LABEL_ENTER_NAME)));
		return handler;
	}

	@EventHandlerMethod
	public void saveContent(SectionInfo info)
	{
		EditConnectorModel model = getModel(info);
		String title = nameField.getValue(info);

		if( Check.isEmpty(title) )
		{
			model.setError(LABEL_ENTER_NAME);
			return;
		}

		Connector connector = querySection.getConnector(info);
		String description = (repositoryService.supportsEditDescription(connector.getLmsType())
			? descriptionField.getValue(info) : "");
		try
		{
			repositoryService.editContent(connector, CurrentUser.getUsername(),
				model.getConnectorItemKey().getContentId(), title, description);
			info.getRootRenderContext().setRenderedBody(new CloseWindowResult(jscall(getCloseFunction()),
				jscall(getOkCallback(), getModel(info).getConnectorItemKey())));
		}
		catch( LmsUserNotFoundException lms )
		{
			model.setError(new TextLabel(lms.getMessage()));
		}
		catch( Exception e )
		{
			LOGGER.error(LABEL_GENERIC_EDIT_ERROR.getText(), e);
			if( e.getMessage() != null )
			{
				model.setError(new KeyLabel(KEY_EDIT_ERROR, e.getMessage()));
			}
			else
			{
				model.setError(LABEL_GENERIC_EDIT_ERROR);
			}
		}
	}

	public TextField getNameField()
	{
		return nameField;
	}

	public TextField getDescriptionField()
	{
		return descriptionField;
	}

	public static class EditConnectorModel extends DialogModel
	{
		@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
		private ConnectorItemKey connectorItemKey;
		private boolean editDescription;
		private Label error;

		public boolean isEditDescription()
		{
			return editDescription;
		}

		public void setEditDescription(boolean editDescription)
		{
			this.editDescription = editDescription;
		}

		public ConnectorItemKey getConnectorItemKey()
		{
			return connectorItemKey;
		}

		public void setConnectorItemKey(ConnectorItemKey connectorItemKey)
		{
			this.connectorItemKey = connectorItemKey;
		}

		public Label getError()
		{
			return error;
		}

		public void setError(Label error)
		{
			this.error = error;
		}
	}
}
