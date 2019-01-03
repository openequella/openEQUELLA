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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;

/**
 * The State class for Checkbox type {@code Section}s and {@code Renderers}.
 * <p>
 * Keeps track of a value, as well as whether or not it was checked.
 * 
 * @see com.tle.web.sections.standard.MappedBooleans
 * @see com.tle.web.sections.standard.Checkbox
 * @see com.tle.web.sections.standard.renderers.toggle.CheckboxRenderer
 * @see com.tle.web.sections.standard.renderers.toggle.RadioButtonRenderer
 * @author jmaginnis
 */
public class HtmlBooleanState extends HtmlComponentState
{
	private String value;
	private boolean checked;

	public boolean isChecked()
	{
		return checked;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public HtmlBooleanState()
	{
		super(RendererConstants.CHECKBOX);
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
