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

import java.io.Serializable;

public class WebWizardPageState implements Serializable
{
	private static final long serialVersionUID = 1;
	private boolean enabled = true;
	private boolean submitted;
	private boolean viewable;
	private boolean valid;
	private boolean showMandatory;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isShowMandatory()
	{
		return showMandatory;
	}

	public void setShowMandatory(boolean showMandatory)
	{
		this.showMandatory = showMandatory;
	}

	public boolean isSubmitted()
	{
		return submitted;
	}

	public void setSubmitted(boolean submitted)
	{
		this.submitted = submitted;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public boolean isViewable()
	{
		return viewable;
	}

	public void setViewable(boolean viewable)
	{
		this.viewable = viewable;
	}

}
