package com.tle.web.sections.standard;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author jmaginnis
 */
public class Link extends AbstractEventOnlyComponent<HtmlLinkState>
{
	private boolean disablable;

	public Link()
	{
		super(RendererConstants.LINK);
	}

	@Override
	public Class<HtmlLinkState> getModelClass()
	{
		return HtmlLinkState.class;
	}

	public void setBookmark(SectionInfo info, Bookmark bookmark)
	{
		HtmlLinkState state = getState(info);
		state.setBookmark(bookmark);
	}

	public void setDisablable(boolean isDisablable)
	{
		this.disablable = isDisablable;
	}

	@Override
	protected HtmlLinkState setupState(SectionInfo info, HtmlLinkState state)
	{
		super.setupState(info, state);
		state.setDisablable(disablable);
		return state;
	}
}
