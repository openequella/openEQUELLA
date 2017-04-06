package com.tle.web.viewitem.summary.section;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.service.ItemXsltService;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class XsltSection extends AbstractParentViewItemSection<Object> implements DisplaySectionConfiguration
{
	// @Inject
	// private HtmlEditorService htmlEditorService;
	@Inject
	private ItemXsltService itemXslService;
	private String config;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( Check.isEmpty(config) )
		{
			return null;
		}

		ItemSectionInfo iinfo = getItemInfo(context);
		// TODO: XSLT should *really* allow for HTML output, but it doesn't
		// return new DivRenderer(HtmlEditorService.DISPLAY_CLASS,
		// htmlEditorService.getHtmlRenderable(context,
		// itemXslService.renderSimpleXsltResult(context, iinfo, config)));
		return new SimpleSectionResult(itemXslService.renderSimpleXsltResult(context, iinfo, config));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "xslt"; //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		this.config = config.getConfiguration();
	}
}
