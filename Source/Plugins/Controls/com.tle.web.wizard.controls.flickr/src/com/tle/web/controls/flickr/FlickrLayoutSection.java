/**
 * 
 */
package com.tle.web.controls.flickr;

import java.util.List;

import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;

/**
 * @author larry
 */
public class FlickrLayoutSection extends AbstractRootSearchSection<AbstractRootSearchSection.Model>
{
	@PlugURL("css/flickr.css")
	private static String FLICKR_CSS;

	@Override
	public Label getTitle(SectionInfo info)
	{
		return new TextLabel(this.getClass().getCanonicalName());
	}

	@Override
	protected void createCssIncludes(List<CssInclude> includes)
	{
		super.createCssIncludes(includes);
		includes.add(CssInclude.include(FLICKR_CSS).hasRtl().make());
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}
}
