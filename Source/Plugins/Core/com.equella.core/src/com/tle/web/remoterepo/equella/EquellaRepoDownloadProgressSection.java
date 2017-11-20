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

package com.tle.web.remoterepo.equella;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.TLESettings;
import com.tle.common.Check;
import com.tle.common.beans.progress.PercentageProgressCallback;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.equella.service.EquellaRepoService;
import com.tle.core.remoterepo.equella.service.EquellaRepoService.AttachmentDownloadSession;
import com.tle.web.core.servlet.ProgressServlet;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.section.CourseListVetoSection;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class EquellaRepoDownloadProgressSection
	extends
		AbstractPrototypeSection<EquellaRepoDownloadProgressSection.EquellaRepoDownloadProgressModel>
	implements
		HtmlRenderer,
		CourseListVetoSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(EquellaRepoDownloadProgressSection.class);

	private static final ExternallyDefinedFunction SHOW_PROGRESS = new ExternallyDefinedFunction("showProgress",
		new IncludeFile(resources.url("scripts/download.js"), JQueryProgression.PRERENDER));

	@Inject
	private EquellaRepoService equellaRepoService;
	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private ProgressServlet progressServlet;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Component
	private Div progress;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final EquellaRepoDownloadProgressModel model = getModel(context);
		progress.addReadyStatements(context,
			new FunctionCallStatement(SHOW_PROGRESS, resources.instUrl("progress/?id=" + model.getSessionId())));

		return view.createResult("download.ftl", this);
	}

	@DirectEvent
	public void startDownload(SectionInfo info)
	{
		final EquellaRepoDownloadProgressModel model = getModel(info);
		if( model.getUuid() != null )
		{
			final PercentageProgressCallback callback = new PercentageProgressCallback();
			String sessionId = null;
			if( model.isAttachments() && model.getSessionId() == null )
			{
				final FederatedSearch search = repoWebService.getRemoteRepository(info);
				final TLESettings settings = new TLESettings();
				settings.load(search);

				sessionId = equellaRepoService.downloadAttachments(settings, model.getUuid(), model.getVersion(),
					callback);
				model.setSessionId(sessionId);

				callback.setForwardUrl(new BookmarkAndModify(info, events.getNamedModifier("finished")).getHref());
				progressServlet.addCallback(sessionId, callback);
			}
			else
			{
				// No attachments to download = instant finish!
				finished(info);
			}
		}
	}

	@EventHandlerMethod
	public void finished(SectionInfo info)
	{
		final EquellaRepoDownloadProgressModel model = getModel(info);
		final String sessionId = model.getSessionId();

		final AttachmentDownloadSession session = equellaRepoService.downloadProgress(sessionId);
		if( session.isFinished() )
		{
			final StagingFile files = equellaRepoService.getDownloadedFiles(session);
			final FederatedSearch search = repoWebService.getRemoteRepository(info);
			try
			{
				final TLESettings settings = new TLESettings();
				settings.load(search);
				final PropBagEx xml = equellaRepoService.getItemXml(settings, model.getUuid(), model.getVersion(),
					!model.isAttachments());
				repoWebService.forwardToWizard(info, files, xml, search);
			}
			catch( Exception e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
	}

	public void setDownloadOptions(SectionInfo info, String uuid, int version, boolean attachments)
	{
		final EquellaRepoDownloadProgressModel model = getModel(info);
		model.setAttachments(attachments);
		model.setUuid(uuid);
		model.setVersion(version);
	}

	@Override
	public Class<EquellaRepoDownloadProgressModel> getModelClass()
	{
		return EquellaRepoDownloadProgressModel.class;
	}

	public Div getProgress()
	{
		return progress;
	}

	public static class EquellaRepoDownloadProgressModel
	{
		@Bookmarked
		private boolean attachments;
		@Bookmarked
		private String uuid;
		@Bookmarked
		private int version;
		@Bookmarked
		private String sessionId;

		public boolean isAttachments()
		{
			return attachments;
		}

		public void setAttachments(boolean attachments)
		{
			this.attachments = attachments;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public int getVersion()
		{
			return version;
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}
	}

	public boolean isShowing(SectionInfo info)
	{
		return !Check.isEmpty(getModel(info).getUuid());
	}
}
