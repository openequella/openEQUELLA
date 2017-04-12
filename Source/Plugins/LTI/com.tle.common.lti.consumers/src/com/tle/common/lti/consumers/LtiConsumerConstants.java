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
