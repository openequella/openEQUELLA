package com.tle.web.sections.standard.model;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ProcessedLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * The State class for Link {@code Renderers} and {@code Section}s.
 * <p>
 * Typically the Section does not carry any state between requests.
 * 
 * @see Link
 * @see LinkRenderer
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class HtmlLinkState extends HtmlComponentState
{
	public static final String TARGET_BLANK = "_blank";

	private Bookmark bookmark;
	private String target;
	private String rel;
	private boolean disablable;

	public HtmlLinkState()
	{
		super(RendererConstants.LINK);
	}

	public HtmlLinkState(Label label, JSHandler clickHandler)
	{
		this(label, (Bookmark) null);
		setClickHandler(clickHandler);
	}

	public HtmlLinkState(Label label)
	{
		this(label, (Bookmark) null);
	}

	public HtmlLinkState(JSHandler click)
	{
		this();
		setClickHandler(click);
	}

	public HtmlLinkState(Bookmark bookmark)
	{
		this(null, bookmark);
	}

	public HtmlLinkState(Label label, Bookmark bookmark)
	{
		this();
		setLabel(label);
		this.bookmark = bookmark;
	}

	@Override
	public HtmlComponentState setLabel(Label label)
	{
		super.setLabel(label);
		if( label != null && super.getTitle() == null )
		{
			if( label instanceof ProcessedLabel )
			{
				setTitle(((ProcessedLabel) label).getUnprocessedLabel());
			}
			else
			{
				setTitle(label);
			}
		}
		return this;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public Bookmark getBookmark()
	{
		return bookmark;
	}

	public void setBookmark(Bookmark bookmark)
	{
		this.bookmark = bookmark;
	}

	public String getRel()
	{
		return rel;
	}

	public void setRel(String rel)
	{
		this.rel = rel;
	}

	public boolean isDisablable()
	{
		return disablable;
	}

	public void setDisablable(boolean disablable)
	{
		this.disablable = disablable;
	}
}
