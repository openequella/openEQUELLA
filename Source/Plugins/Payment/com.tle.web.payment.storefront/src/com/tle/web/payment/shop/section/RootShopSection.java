package com.tle.web.payment.shop.section;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout.TwoColumnModel;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CombinedTemplateResult;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class RootShopSection extends TwoColumnLayout<TwoColumnModel>
{
	@PlugKey("store.list.title")
	private static Label TITLE_LABEL;

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(RootShopSection.class);
	private static final CssInclude CSS = CssInclude.include(resources.url("css/shop/shop.css")).make();
	private static final List<CssInclude> CSS_INCLUDES = Lists.newArrayList(CSS);

	@Override
	protected TemplateResult getTemplateResult(RenderEventContext info)
	{
		final CombinedTemplateResult templateResult = new CombinedTemplateResult();

		final List<SectionId> children = getChildIds(info);
		for( SectionId childId : children )
		{
			String side = info.getLayout(childId.getSectionId());
			if( !TwoColumnLayout.RIGHT.equals(side) )
			{
				side = OneColumnLayout.BODY;
			}
			templateResult.addResult(side, SectionUtils.renderSectionResult(info, childId));
		}
		templateResult.addNamedResult(OneColumnLayout.BODY, CombinedRenderer.combineMultipleResults(CSS_INCLUDES));

		// addBlueBarBits(info, templateResult);
		return templateResult;
	}

	protected List<SectionId> getChildIds(RenderContext info)
	{
		return info.getChildIds(this);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		if( decorations.getTitle() == null )
		{
			decorations.setTitle(TITLE_LABEL);
		}
		decorations.setContentBodyClass("shop-layout");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new TwoColumnModel();
	}
}
