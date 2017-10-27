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

package com.tle.common.usermanagement.user.valuebean;

import java.io.Serializable;

/**
 * @author adame
 */
public interface UserBean extends Serializable
{
	/**
	 * @return a unique, unchanging ID for the user
	 */
	String getUniqueID();

	/**
	 * @return a username for the user
	 */
	String getUsername();

	/**
	 * @return the first name for the user
	 */
	String getFirstName();

	/**
	 * @return the last name for the user
	 */
	String getLastName();

	/**
	 * @return the email address for the user
	 */
	String getEmailAddress();
}
