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

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.Institution;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.common.usermanagement.user.AuthenticatedThread;
import com.tle.core.services.UrlService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.core.servlet.ProgressServlet;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;

public class ProgressSection extends AbstractPrototypeSection<ProgressSection.ProgressModel> implements HtmlRenderer
{
	private static final Log LOGGER = LogFactory.getLog(ProgressSection.class);

	@Inject
	private ProgressServlet progressServlet;
	@Inject
	private UrlService urlService;
	@Inject
	private UserSessionService userSessionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private TabsSection tabsSection;

	@Component
	private Button returnButton;

	public static class ProgressModel
	{
		@Bookmarked
		private String progressId;
		private ProgressData data;

		public String getProgressId()
		{
			return progressId;
		}

		public void setProgressId(String progressId)
		{
			this.progressId = progressId;
		}

		public ProgressData getData()
		{
			return data;
		}

		public void setData(ProgressData data)
		{
			this.data = data;
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ProgressModel model = getModel(context);
		String progressId = model.getProgressId();
		if( progressId != null )
		{
			ProgressData data = userSessionService.getAttribute(progressId);
			if( data != null )
			{
				model.setData(data);
				Decorations.getDecorations(context).setMenuMode(MenuMode.HIDDEN);
				return viewFactory.createTemplateResult("progress.ftl", context); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public Class<ProgressModel> getModelClass()
	{
		return ProgressModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "p"; //$NON-NLS-1$
	}

	private String startProgress(final ProgressRunnable run)
	{
		String progressId = UUID.randomUUID().toString();
		final ListProgressCallback callback = new ListProgressCallback();
		progressServlet.addCallback(progressId, callback);
		run.callback = callback;

		new AuthenticatedThread()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void doRun()
			{
				try
				{
					run.run();
				}
				catch( Exception e )
				{
					LOGGER.error("Error running " + run.getTaskName() + " operation", e); //$NON-NLS-1$ //$NON-NLS-2$
					callback.addError(true, "", e); //$NON-NLS-1$
					callback.setFinished();
				}
			}
		}.start();

		return progressId;
	}

	public void setupProgress(SectionInfo info, List<String> tasks, String action, Institution i,
		ProgressRunnable runnable)
	{
		String progressId = startProgress(runnable);
		ProgressData data = new ProgressData();
		ProgressModel model = getModel(info);
		data.setAction(action);
		data.setInstitutionName(i.getName());
		data.setProgressId(progressId);
		data.setTasks(tasks);
		data.setAjaxUrl(urlService.getAdminUrl() + "progress/?id=" + progressId); //$NON-NLS-1$
		String sessId = userSessionService.createUniqueKey();
		userSessionService.setAttribute(sessId, data);
		model.setProgressId(sessId);
		tabsSection.changeTab(info, null);
	}

	@EventHandlerMethod
	public void finished(SectionInfo info)
	{
		ProgressModel model = getModel(info);
		userSessionService.removeAttribute(model.getProgressId());
		model.setProgressId(null);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		returnButton.setClickHandler(events.getNamedHandler("finished")); //$NON-NLS-1$
	}

	public static class ProgressData implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String action;
		private String institutionName;
		private String progressId;
		private String ajaxUrl;
		private List<String> tasks;

		public String getAction()
		{
			return action;
		}

		public void setAction(String action)
		{
			this.action = action;
		}

		public String getInstitutionName()
		{
			return institutionName;
		}

		public void setInstitutionName(String institutionName)
		{
			this.institutionName = institutionName;
		}

		public String getProgressId()
		{
			return progressId;
		}

		public void setProgressId(String progressId)
		{
			this.progressId = progressId;
		}

		public String getAjaxUrl()
		{
			return ajaxUrl;
		}

		public void setAjaxUrl(String ajaxUrl)
		{
			this.ajaxUrl = ajaxUrl;
		}

		public List<String> getTasks()
		{
			return tasks;
		}

		public void setTasks(List<String> tasks)
		{
			this.tasks = tasks;
		}
	}

	public abstract static class ProgressRunnable implements Runnable
	{
		protected ListProgressCallback callback;

		protected abstract void run(ListProgressCallback callback1);

		public abstract String getTaskName();

		@Override
		public void run()
		{
			run(callback);
			callback.setFinished();
		}
	}

	public Button getReturnButton()
	{
		return returnButton;
	}
}
