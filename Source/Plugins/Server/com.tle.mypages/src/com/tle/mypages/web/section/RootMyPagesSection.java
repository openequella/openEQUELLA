package com.tle.mypages.web.section;

import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.web.model.RootMyPagesModel;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;

/**
 * @author aholland
 */
public class RootMyPagesSection extends AbstractMyPagesSection<RootMyPagesModel> implements HtmlRenderer
{
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		RootMyPagesModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());

		return viewFactory.createResult("mypagesmodal.ftl", context);
	}

	@Override
	public Class<RootMyPagesModel> getModelClass()
	{
		return RootMyPagesModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return MyPagesConstants.SECTION_ROOT;
	}
}
