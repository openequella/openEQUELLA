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

package com.tle.web.sections.js.generic.expression;

import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ClientSideBookmarkExpression extends AbstractExpression
{
	private final Map<String, JSExpression> exprs;
	private final JSBookmarkModifier modifier;

	public ClientSideBookmarkExpression(JSBookmarkModifier modifier)
	{
		this.modifier = modifier;
		exprs = modifier.getClientExpressions();
	}

	@Override
	public String getExpression(RenderContext info)
	{
		StringBuilder sbuf = new StringBuilder();
		String href = new BookmarkAndModify(info, modifier).getHref();
		appendString(sbuf, href, false);
		for( String key : exprs.keySet() )
		{
			sbuf.append('+');
			appendString(sbuf, key, true);
			sbuf.append("+escape("); //$NON-NLS-1$
			JSExpression expr = exprs.get(key);
			sbuf.append(expr.getExpression(info));
			sbuf.append(")"); //$NON-NLS-1$
		}
		return sbuf.toString();
	}

	private void appendString(StringBuilder sbuf, String str, boolean param)
	{
		sbuf.append('\'');
		if( param )
		{
			sbuf.append('&');
		}
		sbuf.append(JSUtils.escape(str, false));
		if( param )
		{
			sbuf.append('=');
		}
		sbuf.append('\'');
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, exprs.values());
	}

}
