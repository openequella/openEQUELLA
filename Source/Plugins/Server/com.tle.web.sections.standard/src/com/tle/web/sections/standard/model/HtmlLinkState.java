/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
