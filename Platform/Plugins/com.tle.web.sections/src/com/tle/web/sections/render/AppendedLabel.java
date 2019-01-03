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

package com.tle.web.sections.render;

import com.tle.web.sections.SectionUtils;

public class AppendedLabel implements Label
{
	private Label first;
	private Label second;
	private boolean html;

	AppendedLabel(Label first, Label second)
	{
		this.first = first;
		this.second = second;
		html = first.isHtml() || second.isHtml();
	}

	@Override
	public String getText()
	{
		return textEnc(first) + textEnc(second);
	}

	private String textEnc(Label label)
	{
		if( html && !label.isHtml() )
		{
			return SectionUtils.ent(label.getText());
		}
		return label.getText();
	}

	@Override
	public boolean isHtml()
	{
		return html;
	}

	public static Label get(Label first, Label second, Label seperator)
	{
		if( first == null )
		{
			return second;
		}
		if( second == null )
		{
			return first;
		}
		if( seperator == null )
		{
			return new AppendedLabel(first, second);
		}
		return new AppendedLabel(first, new AppendedLabel(seperator, second));
	}

	public static Label get(Label first, Label second)
	{
		return get(first, second, null);
	}

}
