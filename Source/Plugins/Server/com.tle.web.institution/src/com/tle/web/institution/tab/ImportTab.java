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

package com.tle.web.institution.tab;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.common.Check;
import com.tle.common.beans.UploadCallbackInputStream;
import com.tle.common.beans.progress.PercentageProgressCallback;
import com.tle.common.filesystem.handle.ImportFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.AuthenticatedThread;
import com.tle.core.migration.MigrationService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.UrlService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.core.servlet.ProgressServlet;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.institution.section.ImportSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.FileUpload;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.ProgressRenderer;

@SuppressWarnings("nls")
public class ImportTab extends AbstractInstitutionTab<ImportTab.ImportTabModel>
{
	private static final Log LOGGER = LogFactory.getLog(ImportTab.class);
	private static final String ARCHIVE_FILE = "archive";

	@PlugKey("institution.import.empty.file")
	private static Label LABEL_EMPTY;

	@Inject
	private ImportSection importSection;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ProgressServlet progressServlet;
	@Inject
	private UrlService urlService;
	@Inject
	private MigrationService migrationService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component
	private FileUpload fileUpload;
	@Component
	private Button uploadButton;

	public static class ImportTabModel
	{
		@Bookmarked
		private String stagingId;
		private Map<String, String> errors = new HashMap<String, String>();
		@Bookmarked
		private boolean uploaded;

		public String getStagingId()
		{
			return stagingId;
		}

		public void setStagingId(String stagingId)
		{
			this.stagingId = stagingId;
		}

		public void setErrors(Map<String, String> errors)
		{
			this.errors = errors;
		}

		public Map<String, String> getErrors()
		{
			return errors;
		}

		public boolean isUploaded()
		{
			return uploaded;
		}

		public void setUploaded(boolean uploaded)
		{
			this.uploaded = uploaded;
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ii";
	}

	@Override
	public Class<ImportTabModel> getModelClass()
	{
		return ImportTabModel.class;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(importSection, id);
		uploadButton.setClickHandler(events.getNamedHandler("uploadedFile"));
	}

	public void startUnzip(SectionInfo info, final ImportFile stagingFile, final String filename, long length)
		throws IOException
	{
		ImportTabModel model = getModel(info);

		final String stagingUuid = stagingFile.getMyPathComponent();
		model.setStagingId(stagingUuid);

		final PercentageProgressCallback callback = new PercentageProgressCallback();
		callback.setForwardUrl(new BookmarkAndModify(info, events.getNamedModifier("finishedUnzip")).getHref());
		callback.setTotalSize(length);
		progressServlet.addCallback(stagingUuid, callback);

		final UploadCallbackInputStream inpStream = new UploadCallbackInputStream(
			fileSystemService.read(stagingFile, ARCHIVE_FILE), callback);

		new AuthenticatedThread()
		{
			@Override
			public void doRun()
			{
				try
				{
					fileSystemService.unzipFile(stagingFile, inpStream, ArchiveType.getForFilename(filename));
				}
				catch( Exception e )
				{
					LOGGER.error("Error unzipping institution file", e);
				}
				finally
				{
					callback.setFinished();
				}
			}
		}.start();
	}

	@EventHandlerMethod
	public void finishedUnzip(SectionInfo info)
	{
		ImportTabModel model = getModel(info);
		String stagingId = model.getStagingId();
		try
		{
			importSection.setupImport(info, stagingId);
		}
		catch( Exception e )
		{
			LOGGER.error(CurrentLocale.get("institutions.error.readingdetails"), e);
			model.getErrors().put("file", CurrentLocale.get("institutions.import.invalid.file"));
			info.preventGET();
		}
		model.setStagingId(null);
	}

	@EventHandlerMethod
	public void uploadedFile(SectionInfo info) throws IOException
	{
		final String filename = fileUpload.getFilename(info);
		ImportTabModel model = getModel(info);
		if( Check.isEmpty(filename) )
		{
			model.setUploaded(true);
			return;
		}

		// Add a uuid to the import to avoid conflicts if uploading an
		// institution with the same filename at the same time. See issue #3888
		// for more information.
		final ImportFile importFile = new ImportFile(filename + "-" + UUID.randomUUID().toString());

		try( InputStream in = fileUpload.getInputStream(info) )
		{
			fileSystemService.removeFile(importFile);
			fileSystemService.write(importFile, ARCHIVE_FILE, in, false);
			startUnzip(info, importFile, filename, fileSystemService.fileLength(importFile, ARCHIVE_FILE));
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( migrationService.getAvailableSchemaIds().isEmpty() )
		{
			return viewFactory.createResult("tab/nodatabases.ftl", this);
		}

		ImportTabModel model = getModel(context);
		if( model.isUploaded() && Check.isEmpty(fileUpload.getFilename(context)) )
		{
			model.getErrors().put("file", LABEL_EMPTY.getText());
		}

		if( Check.isEmpty(model.getErrors()) )
		{
			SectionResult result = SectionUtils.renderSectionResult(context, importSection);
			if( result != null )
			{
				return result;
			}
		}

		String stagingId = model.getStagingId();
		if( stagingId != null )
		{
			context.getBody().addReadyStatements(
				Js.call_s(ProgressRenderer.WEBKIT_PROGRESS_FRAME, JQueryCore.getJQueryCoreUrl()), Js.call_s(
					ProgressRenderer.SHOW_PROGRESS_FUNCTION, urlService.getAdminUrl() + "progress/?id=" + stagingId));
		}

		model.setUploaded(false);

		return viewFactory.createResult("tab/uploadimport.ftl", context);
	}

	@Override
	public boolean shouldDefault(SectionInfo info)
	{
		return !migrationService.getAvailableSchemaIds().isEmpty();
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	public FileUpload getFileUpload()
	{
		return fileUpload;
	}

	public Button getUploadButton()
	{
		return uploadButton;
	}
}
