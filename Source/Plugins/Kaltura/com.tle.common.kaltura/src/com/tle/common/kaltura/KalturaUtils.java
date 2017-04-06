package com.tle.common.kaltura;


@SuppressWarnings("nls")
public final class KalturaUtils
{
	public static final String KALTURA_SAAS_DEFAULT_PLAYER_ID = "1913582";
	public static final String KALTURA_SAAS_ENDPOINT = "http://www.kaltura.com";

	public static final String ATTACHMENT_TYPE = "kaltura";
	public static final String MIME_TYPE = "equella/attachment-kaltura";
	public static final String MIME_DESC = "Kaltura media";

	public static final String PROPERTY_KALTURA_SERVER = "kalturaServer";

	public static final String PROPERTY_ENTRY_ID = "entryId";
	public static final String PROPERTY_TITLE = "title";
	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_DATE = "uploaded";
	public static final String PROPERTY_THUMB_URL = "thumbUrl";
	public static final String PROPERTY_TAGS = "tags";
	public static final String PROPERTY_DURATION = "duration"; // seconds
	public static final String PROPERTY_CUSTOM_PLAYER = "customPlayer";

	// This was used for the first release of Kaltura in 5.2 when the
	// attachments stored the static URL to their respective servers
	@Deprecated
	public static final String PROPERTY_DATA_URL = "dataUrl";

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private KalturaUtils()
	{
		throw new Error();
	}
}
