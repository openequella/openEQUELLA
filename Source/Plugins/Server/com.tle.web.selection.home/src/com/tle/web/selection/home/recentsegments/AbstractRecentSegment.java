package com.tle.web.selection.home.recentsegments;

import java.util.List;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.RecentSelectionsSegment;
import com.tle.web.selection.home.model.RecentSegmentModel;
import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;

@SuppressWarnings("nls")
public abstract class AbstractRecentSegment extends AbstractPrototypeSection<RecentSegmentModel>
	implements
		HtmlRenderer,
		RecentSelectionsSegment
{
	protected static final int MAX_RESULTS = 5;

	@Inject
	private SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SelectionSession session = selectionService.getCurrentSession(context);
		getModel(context).setRecent(getSelections(context, session, MAX_RESULTS));
		return viewFactory.createResult("recentsegment.ftl", this);
	}

	protected abstract List<RecentSelection> getSelections(SectionInfo info, SelectionSession session, int maximum);

	@Override
	public Class<RecentSegmentModel> getModelClass()
	{
		return RecentSegmentModel.class;
	}
}