package com.tle.web.viewitem.summary.sidebar.actions;

import javax.inject.Inject;

import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewable.ViewableItem;

public abstract class AbstractUnselectItemSummarySection<I extends IItem<?>, M> extends AbstractPrototypeSection<M>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@EventFactory
	private EventGenerator events;

	@Inject
	private SelectionService selectionService;

	@Component
	@PlugKey("summary.sidebar.actions.unselectitem.title")
	private Button button;

	protected abstract ViewableItem<I> getViewableItem(SectionInfo info);

	protected abstract String getItemExtensionType();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, button);
	}

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		button.setStyleClass("unselect");
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setClickHandler(events.getNamedHandler("unselect"));
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return selectionService
			.isSelected(info, getViewableItem(info).getItemId(), null, getItemExtensionType(), false);
	}

	@EventHandlerMethod
	public void unselect(SectionInfo info)
	{
		selectionService.removeSelectedResource(info, new SelectedResourceKey(getViewableItem(info).getItemId(),
			getItemExtensionType()));
	}
}
