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

package com.tle.web.controls.universal;

import com.tle.web.sections.render.Label;

public class AttachmentHandlerLabel implements Label
{
	private final Label name;
	private final Label description;

	public AttachmentHandlerLabel(Label name, Label description)
	{
		this.name = name;
		this.description = description;
	}

	@Override
	@SuppressWarnings("nls")
	public String getText()
	{
		return "<h4>" + name.getText() + "</h4>" + description.getText();
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}
}