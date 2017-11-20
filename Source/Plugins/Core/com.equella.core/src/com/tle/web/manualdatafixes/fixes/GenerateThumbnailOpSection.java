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

package com.tle.web.manualdatafixes.fixes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.operations.FilterResultListener;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.manualdatafixes.ManualDataFixModel;
import com.tle.web.manualdatafixes.UpdateTaskStatus;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author aholland
 */
@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class GenerateThumbnailOpSection
	extends
		AbstractPrototypeSection<GenerateThumbnailOpSection.GenerateThumbnailOpModel>
	implements
		HtmlRenderer,
		UpdateTaskStatus
{
	private static final String TASK_ID = "Generate-Thumbnails";

	@PlugKey("fix.generatethumb.task.key")
	private static String TASK_PROGRESS_KEY;
	@PlugKey("fix.generatethumb.task")
	private static String TASK_NAME_KEY;
	@PlugKey("fix.generatethumb.all")
	private static String ALL;
	@PlugKey("fix.generatethumb.missing")
	private static String MISSING;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private ItemService itemService;
	@Inject
	private TaskService taskService;
	@Inject
	private ThumbnailFilterFactory filterFactory;

	@Component
	@PlugKey("fix.generatethumb.execute")
	private Button execute;
	@Component(stateful = false)
	private SingleSelectionList<VoidKeyOption> forceUpdate;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		List<VoidKeyOption> opts = new ArrayList<VoidKeyOption>();
		opts.add(new VoidKeyOption(MISSING, "false"));
		opts.add(new VoidKeyOption(ALL, "true"));
		forceUpdate.setListModel(new SimpleHtmlListModel<VoidKeyOption>(opts));
		forceUpdate.setAlwaysSelect(true);

		execute.setClickHandler(events.getNamedHandler("startGeneratingThumbs"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		GenerateThumbnailOpModel model = getModel(context);
		TaskStatus status = model.getTaskStatus();
		if( status != null && !status.isFinished() )
		{
			model.setTaskLabel(new KeyLabel(TASK_PROGRESS_KEY, status.getDoneWork(), status.getMaxWork()));
			model.setInProgress(true);
		}

		return viewFactory.createResult("generatethumb.ftl", this);
	}

	@EventHandlerMethod
	public void startGeneratingThumbs(SectionInfo info)
	{
		boolean force = Boolean.valueOf(forceUpdate.getSelectedValueAsString(info));
		long instId = CurrentInstitution.get().getUniqueId();
		taskService.getGlobalTask(
			new BeanClusteredTask(TASK_ID + instId, GenerateThumbnailOpSection.class, "createTask", instId, force),
			TimeUnit.SECONDS.toMillis(20));
	}

	public Task createTask(final long currentInstitution, final boolean force)
	{
		return new SingleShotTask()
		{
			@Override
			public void runTask() throws Exception
			{
				Institution inst = institutionService.getInstitution(currentInstitution);
				runAs.executeAsSystem(inst, new Runnable()
				{
					@Override
					public void run()
					{
						itemService.operateAll(filterFactory.generateThumbnails(force), new FilterResultListener()
						{
							@Override
							public void total(int total)
							{
								setupStatus(TASK_PROGRESS_KEY, total);
							}

							@Override
							public void succeeded(ItemKey itemId, ItemPack pack)
							{
								incrementWork();
							}

							@Override
							public void failed(ItemKey itemId, Item item, ItemPack pack, Throwable e)
							{
								incrementWork();
							}
						});
					}
				});
			}

			@Override
			protected String getTitleKey()
			{
				return TASK_NAME_KEY;
			}
		};
	}

	@BindFactory
	public interface ThumbnailFilterFactory
	{
		GenerateThumbnailFilter generateThumbnails(boolean forceUpdate);
	}

	public static class GenerateThumbnailFilter extends BaseFilter
	{
		private final boolean forceUpdate;

		@Inject
		private GenerateThumbnailOpFactory thumbOpFactory;
		@Inject
		private ItemOperationFactory workflowFactory;

		@AssistedInject
		protected GenerateThumbnailFilter(@Assisted boolean forceUpdate)
		{
			this.forceUpdate = forceUpdate;
		}

		@Override
		protected WorkflowOperation[] createOperations()
		{
			return new WorkflowOperation[]{thumbOpFactory.generateThumbnail(forceUpdate),
					workflowFactory.reindexOnly(false)};
		}

		@Override
		public String getWhereClause()
		{
			return "a.class = FileAttachment";
		}

		@Override
		public String getJoinClause()
		{
			return "JOIN i.attachments a";
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new GenerateThumbnailOpModel();
	}

	@NonNullByDefault(false)
	public class GenerateThumbnailOpModel extends ManualDataFixModel
	{
		@Override
		public TaskStatus getTaskStatus()
		{
			if( checkedStatus )
			{
				return taskStatus;
			}

			String taskId = taskService.getRunningGlobalTask(TASK_ID + CurrentInstitution.get().getUniqueId());
			taskService.askTaskChanges(Collections.singleton(taskId));

			if( taskId != null )
			{
				taskStatus = taskService.waitForTaskStatus(taskId, 2000);
			}
			else
			{
				taskStatus = null;
			}

			checkedStatus = true;

			return taskStatus;
		}
	}

	public Button getExecute()
	{
		return execute;
	}

	public SingleSelectionList<VoidKeyOption> getForceUpdate()
	{
		return forceUpdate;
	}

	@Override
	public String getAjaxId()
	{
		return "thumb_status";
	}

	@Override
	public boolean isFinished(SectionInfo info)
	{
		TaskStatus taskStatus = getModel(info).getTaskStatus();
		return taskStatus != null ? taskStatus.isFinished() : true;
	}
}
