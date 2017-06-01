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

package com.tle.core.usermanagement.standard.wrapper;

import java.util.Calendar;
import java.util.Objects;

import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.common.hash.Hash.Digester;
import com.tle.core.guice.Bind;

@Bind
public class RemoteSupportWrapper extends AbstractSystemUserWrapper
{
	@Override
	protected boolean authenticatePassword(String suppliedPassword)
	{
		final String supportKey = ""; // FIXME
		if( Check.isEmpty(supportKey) )
		{
			return false;
		}

		final StringBuilder b = new StringBuilder(supportKey);

		final Calendar c = Calendar.getInstance();
		b.append(c.get(Calendar.YEAR));
		b.append(c.get(Calendar.MONTH) + 1);
		b.append(c.get(Calendar.DAY_OF_MONTH));

		final String expectedPass = Hash.rawHash(Digester.MD5, b.toString());
		return Objects.equals(suppliedPassword, expectedPass);
	}

	@Override
	protected String getEmailAddress()
	{
		return "support@thelearningedge.com.au"; //$NON-NLS-1$
	}
}
