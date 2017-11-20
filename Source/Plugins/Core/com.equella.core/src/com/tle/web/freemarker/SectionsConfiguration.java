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

package com.tle.web.freemarker;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.methods.UserFormatMethod;

@Bind
@Singleton
public class SectionsConfiguration extends BasicConfiguration
{
	@Inject
	private CustomTemplateLoader customLoader;
	@Inject
	private SectionsBeansWrapper beanWrapper;
	@Inject
	private UserService userService;

	@PostConstruct
	protected void setup()
	{
		setObjectWrapper(beanWrapper);
		setTemplateLoader(customLoader);
		setSharedVariable("_userformat", new UserFormatMethod(userService)); //$NON-NLS-1$
	}
}
