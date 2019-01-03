/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class ImageButtonRenderer extends AbstractComponentRenderer implements JSDisableable
{
	private String source;

	public ImageButtonRenderer(HtmlComponentState state)
	{
		super(state);
	}

	@Override
	protected String getTag()
	{
		return "img"; //$NON-NLS-1$
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		attrs.put("src", source); //$NON-NLS-1$

		final StringWriter swriter = new StringWriter();
		super.writeMiddle(new SectionWriter(swriter, writer));
		final String title = swriter.toString();
		attrs.put("alt", title); //$NON-NLS-1$
		attrs.put("title", title); //$NON-NLS-1$
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this);
	}
}
