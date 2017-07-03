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

import javax.inject.Inject;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.ImportFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.section.ProgressSection.ProgressRunnable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
@Bind
public class ImportSection extends AbstractEditSection<ImportSection.ImportModel>
{
	@PlugKey("institution.import.error.schema")
	private static Label LABEL_ERROR_SCHEMA;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private InstitutionImportService instImportService;

	@TreeLookup
	private ProgressSection progressSection;

	@Override
	public Class<ImportModel> getModelClass()
	{
		return ImportModel.class;
	}

	private InstitutionInfo getInstitutionInfo(SectionInfo info)
	{
		final ImportModel model = getModel(info);
		return model.getInstInfo().get(info, new CacheFiller<InstitutionInfo>()
		{
			@Override
			public InstitutionInfo get(SectionInfo info)
			{
				return instImportService.getInstitutionInfo(new ImportFile(model.getStagingId()));
			}
		});
	}

	public void setupImport(SectionInfo info, String stagingId)
	{
		getModel(info).setStagingId(stagingId);
		Institution institution = getInstitutionInfo(info).getInstitution();
		setupFieldsFromInstitution(info, institution);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{

		ImportModel model = getModel(context);
		if( model.getStagingId() != null )
		{
			prepareSelectedDatabase(context);
			return viewFactory.createResult("import.ftl", context);
		}
		return null;
	}

	@Override
	@EventHandlerMethod
	public void doAction(SectionInfo info)
	{
		InstitutionInfo impInstInfo = getInstitutionInfo(info);
		ImportModel model = getModel(info);

		Institution i = getInstitutionDetails(info);
		ensureForCloneOrImport(i);

		if( validate(info, i) )
		{
			final InstitutionInfo newInstInfo = new InstitutionInfo();
			newInstInfo.setBuildVersion(impInstInfo.getBuildVersion());
			newInstInfo.setServerURL(impInstInfo.getServerURL());
			newInstInfo.setInstitution(i);
			final ImportFile copyStaging = new ImportFile(model.getStagingId());
			final long targetSchemaId = getSelectedDatabase(info);

			progressSection.setupProgress(info, instImportService.getConverterTasks(ConvertType.IMPORT, impInstInfo),
				"institutions.import.importing", i, new ProgressRunnable()
				{
					@Override
					public void run(ListProgressCallback callback)
					{
						instImportService.importInstitution(copyStaging, targetSchemaId, newInstInfo, callback);
					}

					@Override
					public String getTaskName()
					{
						return "import";
					}
				});
		}
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
	public void cancel(final SectionInfo info)
	{
		final String stagingId = getModel(info).getStagingId();
		new Thread()
		{
			@Override
			public void run()
			{
				instImportService.cancelImport(new ImportFile(stagingId));
			}
		}.start();

		super.cancel(info);
	}

	public void cancelSectionOnly(final SectionInfo info)
	{
		super.cancel(info);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "imp";
	}

	public static class ImportModel extends EditInstitutionModel
	{
		@Bookmarked
		private String stagingId;
		private final CachedData<InstitutionInfo> instInfo = new CachedData<InstitutionInfo>();

		public String getStagingId()
		{
			return stagingId;
		}

		public void setStagingId(String stagingId)
		{
			this.stagingId = stagingId;
		}

		public CachedData<InstitutionInfo> getInstInfo()
		{
			return instInfo;
		}
	}
}
