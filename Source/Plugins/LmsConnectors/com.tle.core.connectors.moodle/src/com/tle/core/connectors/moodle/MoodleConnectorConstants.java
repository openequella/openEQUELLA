package com.tle.core.connectors.moodle;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class MoodleConnectorConstants
{
	private MoodleConnectorConstants()
	{
		throw new Error();
	}

	public static final String CONNECTOR_TYPE = "moodle";

	public static final String FIELD_WEBSERVICE_TOKEN = "webServiceToken";
	public static final String FIELD_TESTED_WEBSERVICE = "testedWebservice";

	// Repeated Strings for SONAR
	public static final String USER_PARAM = "user";
	public static final String COURSE_ID_PARAM = "courseid";
	public static final String SECTION_ID_PARAM = "sectionid";
	public static final String ARCHIVED_PARAM = "archived";
	public static final String COURSE_CODE_PARAM = "coursecode";
	public static final String KEY_PATH = "//KEY";
	public static final String SINGLE_KEY_PATH = "RESPONSE/SINGLE/KEY";
	public static final String NAME_NODE = "name";
	public static final String VALUE_NODE = "VALUE";
	public static final String SUCCESS_KEY = "success";
}
