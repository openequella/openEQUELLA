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

package com.tle.web.qti.viewer.questions.renderer.xhtml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.hypertext.A;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dd;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dl;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dt;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Li;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ol;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ul;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Param;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Big;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Hr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.I;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Small;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Sub;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Sup;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.Tt;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Caption;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Col;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tbody;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Td;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tfoot;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Th;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Thead;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Abbr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Acronym;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Address;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Blockquote;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Br;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Cite;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Code;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Dfn;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Div;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Em;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H1;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H2;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H3;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H4;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H5;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.H6;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Kbd;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Pre;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Q;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Samp;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Span;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Strong;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Var;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.Check;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;

/**
 * @author Aaron
 */
public class XhtmlElementRenderer extends QtiNodeRenderer
{
	@AssistedInject
	public XhtmlElementRenderer(@Assisted P p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H1 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H2 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H3 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H4 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H5 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted H6 p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Pre p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Br p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Hr p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Div p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Abbr p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Code p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Span p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Blockquote p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Strong p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted A p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Img p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Acronym p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Address p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Cite p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Dfn p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Em p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Kbd p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Q p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Samp p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Var p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Dl p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Dd p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Dt p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Ul p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Li p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Ol p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Param p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Big p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted I p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Small p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Sub p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Sup p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Tt p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Caption p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Col p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	XhtmlElementRenderer(@Assisted Tbody p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Td p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Tfoot p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Th p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Thead p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	@AssistedInject
	public XhtmlElementRenderer(@Assisted Tr p, @Assisted QtiViewerContext context)
	{
		this((QtiNode) p, context);
	}

	protected XhtmlElementRenderer(QtiNode qtiNode, QtiViewerContext context)
	{
		super(qtiNode, context);
	}

	@Override
	protected void addAttributes(Map<String, String> attrs)
	{
		super.addAttributes(attrs);
		String href = attrs.get("src");
		if( href != null )
		{
			if( isRelativeUrl(href) )
			{
				attrs.put("src", getContext().getViewResourceUrl(href).getHref());
			}
		}
		href = attrs.get("href");
		if( href != null )
		{
			if( isRelativeUrl(href) )
			{
				attrs.put("href", getContext().getViewResourceUrl(href).getHref());
			}
		}
		String styleClass = attrs.get("class");
		if( styleClass == null )
		{
			attrs.put("class", "xhtml");
		}
		else
		{
			attrs.put("class", styleClass + " xhtml");
		}
	}

	private boolean isRelativeUrl(String url)
	{
		try
		{
			return Check.isEmpty(new URL(url).getHost());
		}
		catch( MalformedURLException mal )
		{
			return true;
		}
	}
}
