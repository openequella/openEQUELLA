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

package com.tle.web.viewitem.moderation;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.workflow.tasks.CurrentTaskSection;
import com.tle.web.workflow.tasks.ModerationService;
import com.tle.web.workflow.tasks.dialog.ApproveDialog;
import com.tle.web.workflow.tasks.dialog.RejectDialog;

@Bind
public class ViewMetadataAction extends AbstractParentViewItemSection<Object>
{
	@Component
	@PlugKey("action.name")
	private Button metadataButton;

	@Component
	@PlugKey("approve.name")
	private Button approveButton;

	@Component
	@PlugKey("reject.name")
	private Button rejectButton;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private ModerationService moderationService;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		metadataButton.setClickHandler(events.getNamedHandler("execute"));
		metadataButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		approveButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		rejectButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return moderationService.isModerating(info);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}
		approveButton.setClickHandler(context, context.lookupSection(ApproveDialog.class).getOpenFunction());
		rejectButton.setClickHandler(context, context.lookupSection(RejectDialog.class).getOpenFunction());
		return viewFactory.createResult("modbuttons.ftl", this);
	}

	@EventHandlerMethod
	public void execute(SectionInfo info) throws Exception
	{
		moderationService.viewMetadata(info);
	}

	public Button getMetadataButton()
	{
		return metadataButton;
	}

	public Button getApproveButton()
	{
		return approveButton;
	}

	public Button getRejectButton()
	{
		return rejectButton;
	}
}
