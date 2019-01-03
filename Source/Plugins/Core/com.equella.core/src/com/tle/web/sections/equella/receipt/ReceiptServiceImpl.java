/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.receipt;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.render.Label;

@Bind(ReceiptService.class)
@Singleton
public class ReceiptServiceImpl implements ReceiptService
{
	@SuppressWarnings("nls")
	private static final String SESSION_KEY = "receipt";

	@Inject
	private UserSessionService session;

	@SuppressWarnings("nls")
	@Override
	public void setReceipt(Label receipt)
	{
		if( !(receipt instanceof Serializable) )
		{
			throw new UnsupportedOperationException("Use a serializable Label like KeyLabel");
		}
		session.setAttribute(SESSION_KEY, receipt);
	}

	@Override
	public Label getReceipt()
	{
		Label rv = session.getAttribute(SESSION_KEY);
		session.removeAttribute(SESSION_KEY);
		return rv;
	}
}
