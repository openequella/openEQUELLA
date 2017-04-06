package com.tle.mycontent.web.section;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.mycontent.service.MyContentService;
import com.tle.mycontent.web.search.ScrapbookSubSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class ContributeMyContentAction extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugURL("css/handler.css")
	private static String CSS;

	@Inject
	private MyContentService myContentService;

	@TreeLookup
	private ScrapbookSubSearch scrapbook;

	@EventFactory
	private EventGenerator events;

	@Component(name = "b")
	private Link actionLink;

	private Label buttonLabel;
	private String handlerId;

	public void setButtonLabel(Label buttonLabel)
	{
		this.buttonLabel = buttonLabel;
	}

	public void setHandlerId(String handlerId)
	{
		this.handlerId = handlerId;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		
		actionLink.setStyleClass("add");
		actionLink.setLabel(buttonLabel);
		actionLink.setClickHandler(events.getNamedHandler("contribute"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !scrapbook.isEnabled(context) || !myContentService.isMyContentContributionAllowed() )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, actionLink);
	}

	@EventHandlerMethod
	public void contribute(SectionInfo info)
	{
		myContentService.forwardToContribute(info, handlerId);
	}
}
