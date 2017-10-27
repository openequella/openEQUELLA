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

package com.tle.common.usermanagement.user;

import java.util.Collection;

import com.tle.beans.Institution;
import com.tle.common.Triple;
import com.tle.common.usermanagement.user.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public interface ModifiableUserState extends UserState
{
	void setSessionID(String sessionID);

	void setInstitution(Institution institution);

	void setAclExpressions(Triple<Collection<Long>, Collection<Long>, Collection<Long>> expression);

	void setIpAddress(String ipAddress);

	void setHostAddress(String hostAddress);

	void setHostReferrer(String hostReferrer);

	void setSharePassEmail(String email);

	void setToken(String token);

	void setTokenSecretId(String tokenSecretId);

	void setLoggedInUser(UserBean user);

}
