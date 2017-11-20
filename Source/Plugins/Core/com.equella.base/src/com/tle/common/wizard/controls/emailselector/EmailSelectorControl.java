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

package com.tle.common.wizard.controls.emailselector;

import com.dytech.edge.wizard.beans.control.CustomControl;

public class EmailSelectorControl extends CustomControl
{
	private static final long serialVersionUID = 1L;
	private static final String KEY_SELECT_MULTIPLE = "IsSelectMultiple"; //$NON-NLS-1$

	public EmailSelectorControl()
	{
		setClassType("emailselector"); //$NON-NLS-1$
	}

	public EmailSelectorControl(CustomControl cloned)
	{
		if( cloned != null )
		{
			cloned.cloneTo(this);
		}
	}

	public boolean isSelectMultiple()
	{
		return getBooleanAttribute(KEY_SELECT_MULTIPLE);
	}

	public void setSelectMultiple(boolean multiple)
	{
		getAttributes().put(KEY_SELECT_MULTIPLE, multiple);
	}
}
