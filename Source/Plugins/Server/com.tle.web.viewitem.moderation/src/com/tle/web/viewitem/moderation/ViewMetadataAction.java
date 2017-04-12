package com.tle.web.viewitem.moderation;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
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
import com.tle.web.workflow.tasks.ModerationService;

@Bind
public class ViewMetadataAction extends AbstractParentViewItemSection<Object>
{
	@Component
	@PlugKey("action.name")
	private Button button;
	@PlugURL("css/moderationsummary.css")
	private static String CSS;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private ModerationService moderationService;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		button.setClickHandler(events.getNamedHandler("execute"));
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setStyleClass("viewMetadata");
		button.addPrerenderables(CssInclude.include(CSS).hasRtl().make());

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
		return SectionUtils.renderSectionResult(context, button);
	}

	@EventHandlerMethod
	public void execute(SectionInfo info) throws Exception
	{
		moderationService.viewMetadata(info);
	}
}
