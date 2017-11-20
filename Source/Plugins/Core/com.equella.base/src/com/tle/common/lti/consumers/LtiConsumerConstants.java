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

package com.tle.common.lti.consumers;

public class LtiConsumerConstants
{
	public static enum UnknownUser
	{
		DENY(0), IGNORE(1), CREATE(2);

		private int value;

		private UnknownUser(int value)
		{
			this.value = value;
		}

		public int getValue()
		{
			return value;
		}
	}

	public final static String PRIV_EDIT_CONSUMER = "EDIT_LTI_CONSUMER";
	public final static String PRIV_DELETE_CONSUMER = "DELETE_LTI_CONSUMER";
	public final static String PRIV_CREATE_CONUSMER = "CREATE_LTI_CONSUMER";

	public static final String PARAM_CONSUMER_KEY = "oauth_consumer_key";
	public static final String PARAM_SIGNATURE = "oauth_signature";

	// These are taken straight from the URN values from the LIS standard.
	// Excluding Instructor and None. URN uri bullshit added during LtiWrapper
	public static final String[] CUSTOM_ROLES_AUTOCOMPLETE = {"Student", "Faculty", "Member", "Learner", "Mentor",
			"Staff", "Alumni", "ProspectiveStudent", "Guest", "Other", "Administrator", "Observer"};
}
