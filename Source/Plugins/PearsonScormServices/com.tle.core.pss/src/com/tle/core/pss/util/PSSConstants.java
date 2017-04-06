package com.tle.core.pss.util;

public class PSSConstants
{
	// URL paths for PearsonScormServices (PSS) enQueue requests and launch
	// requests, suffixed to whatever we have as the PSS Base URL.
	// The PSS will ensure that a '/' is either present or
	// will be interposed before being appended to the baseUrl value.
	public static final String PSS_ENQUEUE_URL_PATH = "queues/cartridges";
	public static final String PSS_LAUNCH_URL_PATH = "Launch";

	// Various custom_LTI parameter-keys for SCORM PSS launch request
	public static final String CARTRIDGE_ID = "cartridge_id";
	public static final String VERSION = "version";
	public static final String UI_FEATURES = "ui_features";
	public static final String LOG_LEVEL = "log_level";
	public static final String LOG_TARGET = "log_target";
	public static final String SESSION_TIMEOUT = "session_timeout";
	public static final String CONTENT_HOST = "content_host";
	public static final String SCORM_MODE = "scorm_mode";

	public static final String DEFAULT_UI_FEATURES = "tocgrid;suspend_all;navbar;bbar;tbfill;scrolling";
	public static final String VIEWER_UI_FEATURES = "uiFeatures";

	public static final String PSS_VIEWER_ID = "pssViewer";

	// License feature
	public static final String PEARSON_SCORM_SERVICES_LICENCE = "PearsonSCORMServices";

	public PSSConstants()
	{
		throw new Error("class not meant to be instantiated");
	}
}
