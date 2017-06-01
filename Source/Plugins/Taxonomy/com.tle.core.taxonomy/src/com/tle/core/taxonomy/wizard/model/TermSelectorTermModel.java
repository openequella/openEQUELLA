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

package com.tle.core.taxonomy.wizard.model;

import com.tle.web.sections.standard.model.HtmlLinkState;

public class TermSelectorTermModel
{
	private String term;
	private HtmlLinkState deleteButton;

	public TermSelectorTermModel(String term, HtmlLinkState deleteButton)
	{
		this.term = term;
		this.deleteButton = deleteButton;
	}

	public String getTerm()
	{
		return term;
	}

	public void setTerm(String term)
	{
		this.term = term;
	}

	public HtmlLinkState getDeleteButton()
	{
		return deleteButton;
	}

	public void setDeleteButton(HtmlLinkState deleteButton)
	{
		this.deleteButton = deleteButton;
	}
}
