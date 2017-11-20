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

package com.tle.web.institution.section;

import javax.inject.Inject;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.quota.service.QuotaService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class EditSection extends AbstractEditSection<EditInstitutionModel>
{
	@PlugKey("institutions.edit.warning")
	private static Label LABEL_URLWARNING;
	@PlugKey("institutions.edit.filewarning")
	private static Label LABEL_FILEWARNING;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private InstitutionImportService instImportService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private QuotaService quotaService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;

	@Component
	@PlugKey("institutions.edit.unlock")
	private Button unlockUrlButton;
	@Component
	@PlugKey("institutions.edit.unlock")
	private Button unlockFilestoreButton;

	private JSHandler usageRefreshHandler;

	@Override
	public String getDefaultPropertyName()
	{
		return "edi";
	}

	@Override
	public Class<EditInstitutionModel> getModelClass()
	{
		return EditInstitutionModel.class;
	}

	public void setupEdit(SectionInfo info, long institutionId)
	{
		final Institution insti = institutionService.getInstitution(institutionId);
		setupFieldsFromInstitution(info, insti);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		EditInstitutionModel model = getModel(context);
		if( model.getId() != 0 && (!model.isNavigateAway() || model.getFileSystemUsage() != null) )
		{
			model.setNavigateAway(true);
			context.getBody().addReadyStatements(usageRefreshHandler);
			return viewFactory.createResult("edit.ftl", context);
		}
		return null;
	}

	@EventHandlerMethod
	public void calculateUsage(SectionInfo info)
	{
		final EditInstitutionModel model = getModel(info);
		Institution insti = institutionService.getInstitution(model.getId());

		runAs.executeAsSystem(insti, new Runnable()
		{
			@Override
			public void run()
			{
				model.setFileSystemUsage(quotaService.getInstitutionalConsumption(insti));
			}
		});
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		TextField urlField = getUrl();
		urlField.setDisabled(true);
		TextField filestore = getFilestore();
		filestore.setDisabled(true);

		unlockUrlButton.setClickHandler(new StatementHandler(Js.call_s(unlockUrlButton.createDisableFunction(), true),
			Js.call_s(urlField.createDisableFunction(), false)).addValidator(new Confirm(LABEL_URLWARNING)));

		unlockFilestoreButton
			.setClickHandler(new StatementHandler(Js.call_s(unlockFilestoreButton.createDisableFunction(), true),
				Js.call_s(filestore.createDisableFunction(), false)).addValidator(new Confirm(LABEL_FILEWARNING)));

		usageRefreshHandler = new OverrideHandler(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("calculateUsage"),
				ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), "usage-ajax"));
	}

	@Override
	public void doAction(SectionInfo info)
	{
		getModel(info).setNavigateAway(true);
		EditInstitutionModel model = getModel(info);
		Institution oldInst = institutionService.getInstitution(model.getId());
		TextField urlField = getUrl();
		if( Check.isEmpty(urlField.getValue(info)) )
		{
			urlField.setValue(info, oldInst.getUrl());
		}
		TextField filestoreField = getFilestore();
		if( Check.isEmpty(filestoreField.getValue(info)) )
		{
			filestoreField.setValue(info, oldInst.getFilestoreId());
		}

		Institution newInst = getInstitutionDetails(info);
		if( validate(info, newInst) )
		{
			instImportService.update(newInst);
			cancel(info);
		}
		else
		{
			// Stay on page, because there's an error to show, and also obliges
			// user to either correct the error of to specifically cancel &
			// return
			getModel(info).setNavigateAway(false);
		}

	}

	public Button getUnlockUrlButton()
	{
		return unlockUrlButton;
	}

	public Button getUnlockFilestoreButton()
	{
		return unlockFilestoreButton;
	}

}
