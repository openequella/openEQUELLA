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

package com.tle.web.login;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import java.util.HashMap;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class AddIpAddressDialog extends AbstractOkayableDialog<AddIpAddressDialog.AddIpAddressDialogModel>
{
	private static final IncludeFile INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		AddIpAddressDialog.class).url("scripts/ipAddressRegex.js"));
	private static final ExternallyDefinedFunction CHECK_IP_REGEX = new ExternallyDefinedFunction("isWildIPRegex",
		INCLUDE);

	@PlugKey("ipaddress.dialog.title")
	private static Label LABEL_TITLE;

	@PlugKey("ipaddress.dialog.invalid")
	private static Label LABEL_IP_INVALID;

	@PlugKey("ipaddress.dialog.blank")
	private static Label LABEL_IP_BLANK;

	@Component(name = "ipat", stateful = false)
	private TextField ipAddressText;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "aip";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("addipaddress-dialog.ftl", this);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return new OverrideHandler(jscall(getOkCallback(), ipAddressText.createGetExpression()),
			jscall(getCloseFunction())).addValidator(
			ipAddressText.createNotBlankValidator().setFailureStatements(Js.alert_s(LABEL_IP_BLANK))).addValidator(
			new FunctionCallValidator(CHECK_IP_REGEX, Jq.$(ipAddressText)).setFailureStatements(Js
				.alert_s(LABEL_IP_INVALID)));
	}

	public TextField getIpAddressText()
	{
		return ipAddressText;
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}

	@Override
	public AddIpAddressDialogModel instantiateDialogModel(@Nullable SectionInfo info)
	{
		return new AddIpAddressDialogModel();
	}

	@Override
	public Class<AddIpAddressDialogModel> getModelClass()
	{
		return AddIpAddressDialogModel.class;
	}

	public class AddIpAddressDialogModel extends DialogModel
	{
		private final Map<String, String> errorList = new HashMap<String, String>();

		public Map<String, String> getErrorList()
		{
			return errorList;
		}
	}
}
