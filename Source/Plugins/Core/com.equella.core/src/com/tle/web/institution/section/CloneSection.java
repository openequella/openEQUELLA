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

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.section.ProgressSection.ProgressRunnable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
@Bind
public class CloneSection extends AbstractEditSection<CloneSection.CloneModel>
{
	@PlugKey("institution.import.error.schema")
	private static Label LABEL_ERROR_SCHEMA;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private InstitutionImportService instImportService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private ProgressSection progressSection;

	@Override
	public Class<CloneModel> getModelClass()
	{
		return CloneModel.class;
	}

	public void setupClone(SectionInfo info, long instId)
	{
		CloneModel model = getModel(info);
		model.setId(instId);
		Institution i = institutionService.getInstitution(instId);
		setupFieldsFromInstitution(info, i);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		CloneModel model = getModel(context);
		if( model.getId() != 0 && !getModel(context).isNavigateAway() )
		{

			if( !model.hasLoaded() )
			{
				getItemsCheck().setChecked(context, true);
				getAttachmentsCheck().setChecked(context, true);
				getAuditlogsCheck().setChecked(context, true);
				model.setLoaded(true);
			}

			if( !getItemsCheck().isChecked(context) )
			{
				getAttachmentsCheck().setDisabled(context, true);
			}

			prepareSelectedDatabase(context);
			getModel(context).setNavigateAway(true);
			return viewFactory.createResult("clone.ftl", context);
		}
		return null;
	}

	@Override
	protected void extraValidate(SectionInfo info, Institution institution, Map<String, String> errors)
	{
		if( getSelectedDatabase(info) == -1 )
		{
			errors.put("schema", LABEL_ERROR_SCHEMA.getText());
		}
	}

	@Override
	@EventHandlerMethod
	public void doAction(SectionInfo info)
	{
		getModel(info).setNavigateAway(false);
		final Institution i = getInstitutionDetails(info);
		final long origInstitutionId = i.getUniqueId();
		ensureForCloneOrImport(i);

		if( validate(info, i) )
		{
			final long targetSchemaId = getSelectedDatabase(info);
			final Set<String> flags = getFlags(info);
			final InstitutionInfo cloned = instImportService.getInstitutionInfo(i);
			cloned.setFlags(flags);

			progressSection.setupProgress(info, instImportService.getConverterTasks(ConvertType.CLONE, cloned),
				"institutions.clone.cloning", i, new ProgressRunnable()
				{
					@Override
					public void run(ListProgressCallback callback)
					{
						instImportService.clone(targetSchemaId, i, origInstitutionId, callback, flags);
					}

					@Override
					public String getTaskName()
					{
						return "clone";
					}
				});
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "clo";
	}

	public static class CloneModel extends EditInstitutionModel
	{
		// nothing
	}
}
