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

package com.dytech.edge.wizard.beans.control;

import com.tle.beans.entity.LanguageBundle;

public class Repeater extends AbstractControlsWizardControl
{
	private static final long serialVersionUID = 1;
	public static final String REPEATER_CLASS = "repeater";

	private LanguageBundle noun;
	private int min;
	private int max;

	public Repeater()
	{
		min = 1;
		max = 10;
	}

	@Override
	public String getClassType()
	{
		return REPEATER_CLASS;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public int getMin()
	{
		return min;
	}

	public void setMin(int min)
	{
		this.min = min;
	}

	public LanguageBundle getNoun()
	{
		return noun;
	}

	public void setNoun(LanguageBundle noun)
	{
		this.noun = noun;
	}
}
