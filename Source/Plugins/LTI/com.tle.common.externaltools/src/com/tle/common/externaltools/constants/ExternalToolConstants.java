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

package com.tle.common.externaltools.constants;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Constants common to both LTI environments: ExternalTool and SCORM-PSS
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public final class ExternalToolConstants
{
	// ACLs
	public final static String PRIV_EDIT_TOOL = "EDIT_EXTERNAL_TOOL";
	public final static String PRIV_DELETE_TOOL = "DELETE_EXTERNAL_TOOL";
	public final static String PRIV_CREATE_TOOL = "CREATE_EXTERNAL_TOOL";

	// MIME
	public static final String MIME_TYPE = "equella/attachment-lti";
	public static final String MIME_ICON_PATH = "icons/lti.png";
	public static final String MIME_DESC = "External tool provider";
	public static final String VIEWER_ID = "externalToolViewer";

	public static final String CUSTOM_ATTACHMENT_TYPE = "lti";
	// Attachment data keys
	public static final String EXTERNAL_TOOL_PROVIDER_UUID = "EXTERNAL_TOOL_PROVIDER_UUID";
	public static final String LAUNCH_URL = "LAUNCH_URL";
	public static final String CUSTOM_PARAMS = "CUSTOM_PARAMS";
	public static final String CONSUMER_KEY = "CONSUMER_KEY";
	public static final String SHARED_SECRET = "SHARED_SECRET";
	public static final String ICON_URL = "ICON_URL";
	public static final String SHARE_NAME = "SHARE_NAME";
	public static final String SHARE_EMAIL = "SHARE_EMAIL";

	// Constant for the "automatic based on URL" option
	public static final String AUTOMATIC_UUID = "AUTOMATIC_UUID";

	// LTI launch parameter standard constant values for EQUELLA branding
	public static final String EQUELLA_PRODUCT_CODE = "equella";
	
	/**
	 * The value for "roles" parameter, recommended to be a comma-separated list
	 * of URN values for roles, in this case a single URN.
	 */
	// Hi this is Arnold. Your Instructor.

	public static final String URN = "urn:lti:instrole:ims/lis/";
	public static final String CONTEXT_URN = "urn:lti:role:ims/lis/";

	public static final String INSTRUCTOR_ROLE = "Instructor";
	public static final String INSTRUCTOR_ROLE_URN = URN + INSTRUCTOR_ROLE;
	public static final String INSTRUCTOR_CONTEXT_ROLE_URN = CONTEXT_URN + INSTRUCTOR_ROLE;
	public static final Set<String> INSTRUCTOR_ROLES_SET = Sets.newHashSet(INSTRUCTOR_CONTEXT_ROLE_URN,
		INSTRUCTOR_ROLE, INSTRUCTOR_ROLE_URN);

	public static final String LEARNER_ROLE = "Learner";
	public static final String LEARNER_ROLE_URN = URN + LEARNER_ROLE;
	public static final String LEARNER_CONTEXT_ROLE_URN = CONTEXT_URN + LEARNER_ROLE;

	/**
	 * not required by LTI, but moodle likes to send some, so we may as well
	 * pass them on
	 */
	public static final String EXT_PREFIX = "ext_";
	public static final String EXT_LMS = EXT_PREFIX + "lms";
	/*********************************************
	 * implementation parameters, will vary between EQUELLA launches &
	 * third-party (eg Moodle) launches
	 *********************************************/
	public static final String RESOURCE_LINK_DESCRIPTION = "resource_link_description";
	public static final String RESOURCE_LINK_ID = "resource_link_id";
	public static final String RESOURCE_LINK_TITLE = "resource_link_title";

	public static final String USER_ID = "user_id";
	public static final String USER_IMAGE = "user_image";

	public static final String LAUNCH_PRESENTATION_CSS_URL = "launch_presentation_css_url";
	public static final String LAUNCH_PRESENTATION_DOCUMENT_TARGET = "launch_presentation_document_target";
	public static final String LAUNCH_PRESENTATION_HEIGHT = "launch_presentation_height";
	public static final String LAUNCH_PRESENTATION_LOCALE = "launch_presentation_locale";
	public static final String LAUNCH_PRESENTATION_RETURN_URL = "launch_presentation_return_url";
	public static final String LAUNCH_PRESENTATION_WIDTH = "launch_presentation_width";

	public static final String TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE = "tool_consumer_info_product_family_code";
	public static final String TOOL_CONSUMER_INFO_VERSION = "tool_consumer_info_version";
	public static final String TOOL_CONSUMER_INSTANCE_CONSUMER_CONTACT_EMAIL = "tool_consumer_instance_contact_email";
	public static final String TOOL_CONSUMER_INSTANCE_DESC = "tool_consumer_instance_description";
	public static final String TOOL_CONSUMER_INSTANCE_GUID = "tool_consumer_instance_guid";
	public static final String TOOL_CONSUMER_INSTANCE_NAME = "tool_consumer_instance_name";
	public static final String TOOL_CONSUMER_INSTANCE_URL = "tool_consumer_instance_url";

	public static final String LAUNCHER_ROLES = "roles";
	public static final String ROLE_SCOPE_MENTOR = "role_scope_mentor";

	public static final String LIS_PERSON_SOURCEDID = "lis_person_sourcedid";
	public static final String LIS_PERSON_CONTACT_EMAIL_PRIMARY = "lis_person_contact_email_primary";
	public static final String LIS_PERSON_NAME_GIVEN = "lis_person_name_given";
	public static final String LIS_PERSON_NAME_FAMILY = "lis_person_name_family";
	public static final String LIS_PERSON_NAME_FULL = "lis_person_name_full";

	// LMS specific parameters
	public static final String LIS_OUTCOME_SERVICE_URL = "lis_outcome_service_url";
	public static final String LIS_RESULT_SOURCEDID = "lis_result_sourcedid";

	// NB: outcome service URL must not lead with a '/'
	public static final String OUTCOME_SERVICE_URL_PATH = "ltilaunch/results_outcome.do";
	public static final String CONTEXT_ID = "context_id";
	public static final String CONTEXT_LABEL = "context_label";
	public static final String CONTEXT_TITLE = "context_title";
	public static final String CONTEXT_TYPE = "context_type";
	public static final String CONTEXT_TYPE_EQUELLA_ITEM = "urn:lti:context-type:equella/Item";

	// LTI launch parameter constant key/values
	public static final String LTI_MESSAGE_TYPE = "lti_message_type";
	public static final String BASIC_LTI_LAUNCH_REQUEST = "basic-lti-launch-request";
	public static final String TOOL_PROXY_REGISTRATION_REQUEST = "ToolProxyRegistrationRequest";
	public static final String TOOL_PROXY_REREGISTRATION_REQUEST = "ToolProxyReregistrationRequest";
	public static final String LTI_MESSAGE_TYPE_VALUES = BASIC_LTI_LAUNCH_REQUEST + ","
		+ TOOL_PROXY_REGISTRATION_REQUEST + "," + TOOL_PROXY_REREGISTRATION_REQUEST;

	public static final String LTI_VERSION = "lti_version";
	// despite the "p0", applies to all v1.n (assuming likewise for all v2.n)
	public static final String LTI_VERSION_1_VALUE = "LTI-1p0";
	public static final String LTI_VERSION_2_VALUE = "LTI-2p0";
	public static final String LTI_VERSION_VALUES = LTI_VERSION_1_VALUE + "," + LTI_VERSION_2_VALUE;

	// normal, error and error log parameters which may be returned by the Tool
	// Provider
	public static final String LTI_MSG = "lti_msg";
	public static final String LTI_ERROR = "lti_error";
	public static final String LTI_ERRORMSG = "lti_errormsg";
	public static final String LTI_ERRORLOG = "lti_errorlog";

	public static final String LAUNCH_TARGET_IFRAME = "iframe"; // frame|iframe|window

	public static final String CUSTOM_PARAMS_POST_PREFIX = "custom_";

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private ExternalToolConstants()
	{
		throw new Error("class not meant to be instantiated");
	}
}
