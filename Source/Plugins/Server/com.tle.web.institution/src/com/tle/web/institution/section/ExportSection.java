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

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.ExportFile;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.FileSystemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.section.ProgressSection.ProgressRunnable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;

@Bind
@SuppressWarnings("nls")
public class ExportSection extends AbstractEditSection<ExportSection.ExportModel>
{
	@PlugKey("institutions.admin.confirmdump")
	private static String KEY_CONFIRMEXPORT;
	@PlugKey("institutions.export.warning.notdisabled")
	private static Label LABEL_NOTDISABLED;
	@PlugKey("institutions.export.exporting")
	private static String KEY_EXPORTING;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ContentStreamWriter contentStreamWriter;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private InstitutionImportService instImportService;

	@TreeLookup
	private ProgressSection progressSection;

	@Override
	public String getDefaultPropertyName()
	{
		return "exp"; //$NON-NLS-1$
	}

	@Override
	public Class<ExportModel> getModelClass()
	{
		return ExportModel.class;
	}

	public static class ExportModel extends EditInstitutionModel
	{
		private String name;
		private String url;
		private Label warning;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public Label getWarning()
		{
			return warning;
		}

		public void setWarning(Label warning)
		{
			this.warning = warning;
		}
	}

	public void setupExport(SectionInfo info, long institutionId)
	{
		getModel(info).setId(institutionId);
		getModel(info).setNavigateAway(false);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ExportModel model = getModel(context);
		long instId = model.getId();

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

		if( instId != 0 && !getModel(context).isNavigateAway() )
		{
			Institution i = institutionService.getInstitution(instId);
			if( i.isEnabled() )
			{
				model.setWarning(LABEL_NOTDISABLED);
			}
			model.setName(i.getName());
			model.setUrl(i.getUrl());
			SubmitValuesHandler handler = events.getNamedHandler("doAction");
			handler.addValidator(new Confirm(new KeyLabel(KEY_CONFIRMEXPORT, model.getName())));
			getActionButton().setClickHandler(context, handler);

			getModel(context).setNavigateAway(true);
			return viewFactory.createResult("export.ftl", context);
		}
		return null;
	}

	@EventHandlerMethod
	public void download(final SectionInfo info, final String stagingId) throws Exception
	{
		if( !CurrentUser.getUserState().isSystem() )
		{
			info.forwardToUrl("institutions.do");
			return;
		}

		final StringBuilder exportedFilename = new StringBuilder();
		exportedFilename.append("institution-");
		exportedFilename.append(ApplicationVersion.get().getFull());
		exportedFilename.append('-');
		exportedFilename.append(new LocalDate(CurrentTimeZone.get()).format(Dates.ISO_DATE_ONLY).replace("-", ""));
		exportedFilename.append(".tgz");

		info.setRendered();
		FileContentStream stream = fileSystemService.getContentStream(new ExportFile(stagingId + ".tgz"), null,
			"application/x-gzip");

		stream.setFilenameWithoutPath(exportedFilename.toString());
		stream.setContentDisposition("attachment");
		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(), stream);
	}

	@Override
	public void doAction(final SectionInfo info)
	{
		final ExportModel model = getModel(info);
		final Institution i = institutionService.getInstitution(model.getId());
		final Set<String> flags = getFlags(info);
		final InstitutionInfo instImp = instImportService.getInstitutionInfo(i);
		instImp.setFlags(flags);
		progressSection.setupProgress(info, instImportService.getConverterTasks(ConvertType.EXPORT, instImp),
			KEY_EXPORTING, i, new ProgressRunnable()
			{
				@Override
				public void run(ListProgressCallback cb)
				{
					String stagingId = instImportService.exportInstitution(i, cb, flags);
					cb.setForwardUrl(
						new BookmarkAndModify(info, events.getNamedModifier("download", stagingId)).getHref());
				}

				@Override
				public String getTaskName()
				{
					return "export";
				}
			});
	}
}
