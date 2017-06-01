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

package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * TODO: this should be in it's own plugin!
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ITunesUSettings extends UniversalSettings
{
	private static final String INSTITUTION_ID = "institutionId";

	public ITunesUSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public ITunesUSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public String getInstitutionId()
	{
		return (String) wrapped.getAttributes().get(INSTITUTION_ID);
	}

	public void setInstitutionId(String institutionId)
	{
		wrapped.getAttributes().put(INSTITUTION_ID, institutionId);
	}
}
