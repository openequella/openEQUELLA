package com.tle.core.mimetypes;

import com.tle.annotation.NonNullByDefault;

@SuppressWarnings("nls")
@NonNullByDefault
public final class MimeTypeConstants
{
	public static final String KEY_DEFAULT_VIEWERID = "defaultViewerId";
	public static final String KEY_ENABLED_VIEWERS = "enabledViewers";
	public static final String KEY_DISABLE_FILEVIEWER = "fileViewerDisabled";
	public static final String KEY_ICON_WEBAPPRELATIVE = "webappIcon";
	public static final String KEY_ICON_GIFBASE64 = "base64GIFIcon";
	public static final String KEY_ICON_PLUGINICON = "PluginIconPath";
	public static final String KEY_VIEWER_CONFIG_PREFIX = "viewerConfig-";
	public static final String KEY_VIEWER_CONFIG_APPENDTOKEN = "appendToken";

	public static final String MIME_LINK = "equella/link";
	public static final String MIME_PLAN = "equella/plan";
	public static final String MIME_ITEM = "equella/item";
	public static final String MIME_IMS = "equella/ims-package";
	public static final String MIME_SCORM = "equella/scorm-package";

	public static final String VAL_DEFAULT_VIEWERID = "file";

	private MimeTypeConstants()
	{
		throw new Error();
	}
}
