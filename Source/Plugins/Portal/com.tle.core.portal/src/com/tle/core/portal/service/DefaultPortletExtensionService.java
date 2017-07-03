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

package com.tle.core.portal.service;

import java.util.List;

import javax.inject.Singleton;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class DefaultPortletExtensionService implements PortletServiceExtension
{
	@Override
	public void changeUserId(String fromUserId, String toUserId)
	{
		// Nothing to change
	}

	@Override
	public void deleteExtra(Portlet portlet)
	{
		// No
	}

	@Override
	public void edit(Portlet oldPortlet, PortletEditingBean newPortlet)
	{
		// No
	}

	@Override
	public void add(Portlet portlet)
	{
		// No
	}

	@Override
	public void loadExtra(Portlet portlet)
	{
		// No
	}

	@Override
	public void doValidation(PortletEditingBean newPortlet, List<ValidationError> errors)
	{
		// No
	}
}
