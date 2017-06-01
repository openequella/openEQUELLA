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

package com.tle.web.wizard.page;

import com.tle.web.sections.render.TagRenderer;

public class ControlResult
{
	private String sectionId;
	private TagRenderer result;

	public ControlResult(String sectionId, TagRenderer renderer)
	{
		this.sectionId = sectionId;
		this.result = renderer;
	}

	public String getSectionId()
	{
		return sectionId;
	}

	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public TagRenderer getResult()
	{
		return result;
	}

	public void setResult(TagRenderer result)
	{
		this.result = result;
	}

}
