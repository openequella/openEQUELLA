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

package com.tle.core.url.guice;

import com.tle.core.config.guice.OptionalConfigModule;

/**
 * @author Aaron
 *
 */
public class URLModule extends OptionalConfigModule
{
	private static final int TRIES_UNTIL_WARNING = 5;
	private static final int TRIES_UNTIL_DISABLED = 10;

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindInt("urlChecker.triesUntilWarning", TRIES_UNTIL_WARNING);
		bindInt("urlChecker.triesUntilDisabled", TRIES_UNTIL_DISABLED);
	}
}
