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

package com.tle.web.controls.mypages;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.wizard.WizardState;

public class EnsureItemUrls extends AbstractWorkflowOperation
{
	@Inject
	private MyPagesService myPagesService;
	private final WizardState state;

	@Inject
	public EnsureItemUrls(@Assisted WizardState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		myPagesService.convertPreviewUrlsToItemUrls(state);
		return false;
	}

	public void setMyPagesService(MyPagesService myPagesService)
	{
		this.myPagesService = myPagesService;
	}

	@BindFactory
	public interface EnsureFactory
	{
		EnsureItemUrls create(WizardState state);
	}
}
