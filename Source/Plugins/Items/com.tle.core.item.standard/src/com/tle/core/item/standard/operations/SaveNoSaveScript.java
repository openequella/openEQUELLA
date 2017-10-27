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

package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class SaveNoSaveScript extends SaveOperation
{
	private boolean noSaveScript;

	@AssistedInject
	protected SaveNoSaveScript(@Assisted boolean noSaveScript)
	{
		super();
		this.noSaveScript = noSaveScript;
		setNoSaveScript(noSaveScript);
	}

	@Override
	protected void runSaveScript()
	{
		if( noSaveScript )
		{
			return;
		}
		super.runSaveScript();
	}

}
