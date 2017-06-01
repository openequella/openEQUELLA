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

package com.tle.core.user;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public class SharePassUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;

	public SharePassUserState(Institution institution, String email, String token)
	{
		setInstitution(institution);
		setSharePassEmail(email);
		setToken(token);
		setLoggedInUser(new DefaultUserBean(email, email, email, email, email));
		setAuthenticated(true);
	}
}
