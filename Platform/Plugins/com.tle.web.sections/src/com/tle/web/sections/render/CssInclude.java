/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.render;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;

@SuppressWarnings("nls")
public class CssInclude implements PreRenderable, Comparable<CssInclude>
{
	public enum Browser
	{
		ANY()
		{
			@Override
			public String getConditionStart()
			{
				return null;
			}

			@Override
			public String getConditionEnd()
			{
				return null;
			}
		},
		IE_ANY()
		{
			@Override
			public String getConditionStart()
			{
				return "<!--[if IE]>";
			}

			@Override
			public String getConditionEnd()
			{
				return "<![endif]-->";
			}
		},
		IE_8()
		{
			@Override
			public String getConditionStart()
			{
				return "<!--[if IE 8]>";
			}

			@Override
			public String getConditionEnd()
			{
				return "<![endif]-->";
			}
		};

		public abstract String getConditionStart();

		public abstract String getConditionEnd();
	}

	// http://www.w3.org/TR/CSS2/media.html#media-types
	// Note: We are only ever likely to use the first 4
	// Also note: technically multiple medias should be supported in a comma
	// separated list... implement this if we need it
	public enum Media
	{
		ALL, SCREEN, PRINT, HANDHELD, BRAILLE, EMBOSSED, PROJECTION, SPEECH, TTY, TV
	}

	public enum Priority
	{
		/**
		 * Something like styles.css would use this since it is the base styles
		 * to be overridden where appropriate
		 */
		LOWEST,

		/**
		 * The vast majority of styles will have this priority
		 */
		NORMAL,

		/**
		 * Something like customer.css would use this since it needs to override
		 * other styles
		 */
		HIGHEST
	}

	private final String cssFile;
	private boolean hasNew;
	private boolean hasRtl;
	private boolean hasMin;
	private Browser browser = Browser.ANY;
	private Media media = Media.ALL;
	private Priority priority = Priority.NORMAL;
	private PreRenderable[] preRenderables;

	@Deprecated
	public CssInclude(String cssFile, PreRenderable... preRenderables)
	{
		this.cssFile = cssFile;
		this.preRenderables = preRenderables;
	}

	@Deprecated
	public CssInclude(String cssFile, boolean hasRtl, PreRenderable... preRenderables)
	{
		this.cssFile = cssFile;
		this.hasRtl = hasRtl;
		this.preRenderables = preRenderables;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(preRenderables);
		info.addCss(this);
	}

	public String getHref(SectionInfo info)
	{
		return minified(info, cssFile);
	}

	private String minified(SectionInfo info, String filename)
	{
		if (hasMin && info != null && info.getBooleanAttribute(SectionInfo.KEY_MINIFIED))
		{
			return filename.replace(".css", ".min.css");
		}
		return filename;
	}

	public String getRtlHref(SectionInfo info)
	{
		return minified(info, cssFile.replace(".css", ".rtl.css"));
	}

	public boolean isHasRtl()
	{
		return hasRtl;
	}

	public Browser getBrowser()
	{
		return browser;
	}

	public String getMedia()
	{
		return media.name().toLowerCase();
	}

	@Override
	public int hashCode()
	{
		return cssFile.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof CssInclude) )
		{
			return false;
		}

		return cssFile.equals(((CssInclude) obj).cssFile);
	}

	@Override
	public int compareTo(CssInclude other)
	{
		int prioCompare = priority.compareTo(other.priority);
		if( prioCompare == 0 )
		{
			// check browser specificity
			if( browser != Browser.ANY )
			{
				return 1;
			}
			else if( other.browser != Browser.ANY )
			{
				return -1;
			}
		}
		return prioCompare;
	}

	public static CssIncludeBuilder include(String href)
	{
		return new CssIncludeBuilder(href);
	}

	public static final class CssIncludeBuilder
	{
		private final CssInclude inc;

		private CssIncludeBuilder(String href)
		{
			inc = new CssInclude(href);
		}

		public CssInclude make()
		{
			return inc;
		}

		public CssIncludeBuilder prerender(PreRenderable... prs)
		{
			inc.preRenderables = prs;
			return this;
		}

		public CssIncludeBuilder hasRtl()
		{
			inc.hasRtl = true;
			return this;
		}

		public CssIncludeBuilder priority(Priority p)
		{
			inc.priority = p;
			return this;
		}

		public CssIncludeBuilder browser(Browser b)
		{
			inc.browser = b;
			return this;
		}

		public CssIncludeBuilder media(Media m)
		{
			inc.media = m;
			return this;
		}

		public CssIncludeBuilder hasMin()
		{
			inc.hasMin = true;
			return this;
		}
	}
}
