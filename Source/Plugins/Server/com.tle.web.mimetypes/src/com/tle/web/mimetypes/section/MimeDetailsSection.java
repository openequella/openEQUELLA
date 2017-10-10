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
import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.mimetypes.MimeEditExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MutableList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.StringListModel;

@Bind
public class MimeDetailsSection extends AbstractPrototypeSection<MimeDetailsSection.MimeDetailsModel>
	implements
		MimeEditExtension,
		HtmlRenderer
{
	@PlugKey("error.mimetype.empty")
	private static String ERROR_MIME_TYPE_EMPTY;
	@PlugKey("error.extensions.length")
	private static String ERROR_EXTENSIONS_LENGTH;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private TextField description;
	@Component
	private TextField type;
	@Component
	private TextField newExtension;
	@Component
	private MutableList<String> extensions;
	@Component
	private Button addExtensionButton;
	@Component
	private Button removeExtensionButton;
	@AjaxFactory
	private AjaxGenerator ajax;

	private List<String> ajaxIds;

	public static class MimeDetailsModel
	{
		// nothing
	}

	@Override
	public Class<MimeDetailsModel> getModelClass()
	{
		return MimeDetailsModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "det";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		extensions.setListModel(new StringListModel());
		addExtensionButton.addClickStatements(
			new FunctionCallStatement(extensions.createAddFunction(), newExtension.createGetExpression(),
				newExtension.createGetExpression()),
			new ScriptStatement(JQuerySelector.valueSetExpression(newExtension, "")));
		removeExtensionButton.addClickStatements(new FunctionCallStatement(extensions.createRemoveFunction()));
		ajaxIds = new ArrayList<String>();
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		String[] ids = ajaxIds.toArray(new String[ajaxIds.size()]);
		type.setEventHandler(JSHandler.EVENT_BLUR,
			new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, null, null, ids)));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("mimedetails.ftl", context);
	}

	@Override
	public void loadEntry(SectionInfo info, MimeEntry entry)
	{
		if( entry != null )
		{
			description.setValue(info, entry.getDescription());
			type.setValue(info, entry.getType());
			newExtension.setValue(info, "");
			extensions.getListModel().setValues(info, new ArrayList<String>(entry.getExtensions()));
		}
	}

	@Override
	public void saveEntry(SectionInfo info, MimeEntry entry)
	{
		if( Check.isEmpty(type.getValue(info)) )
		{
			throw new InvalidDataException(new ValidationError("type", null, ERROR_MIME_TYPE_EMPTY));
		}
		entry.setDescription(description.getValue(info));
		entry.setType(type.getValue(info));

		List<String> extensionsList = extensions.getValues(info);
		for( String ext : extensionsList )
		{
			if( ext.length() >= 20 )
			{
				throw new InvalidDataException(new ValidationError("extensions", null, ERROR_EXTENSIONS_LENGTH));
			}
		}
		entry.setExtensions(extensions.getValues(info));
	}

	@Override
	public NameValue getTabToAppearOn()
	{
		return MimeTypesEditSection.TAB_DETAILS;
	}

	@Override
	public boolean isVisible(SectionInfo info)
	{
		return true;
	}

	public TextField getDescription()
	{
		return description;
	}

	public TextField getType()
	{
		return type;
	}

	public TextField getNewExtension()
	{
		return newExtension;
	}

	public MutableList<String> getExtensions()
	{
		return extensions;
	}

	public Button getAddExtensionButton()
	{
		return addExtensionButton;
	}

	public Button getRemoveExtensionButton()
	{
		return removeExtensionButton;
	}

	public void addAjaxId(String ajaxId)
	{
		ajaxIds.add(ajaxId);
	}
}
