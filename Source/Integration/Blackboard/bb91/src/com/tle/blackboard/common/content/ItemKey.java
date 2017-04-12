package com.tle.blackboard.common.content;

/**
 * @author Aaron
 */
// @NonNullByDefault
public class ItemKey
{
	private final String institutionUrl;
	private final String uuid;
	private final int version;
	/* @Nullable */
	private final String contentId;
	private final String courseId;
	private final String folderId;
	/* @Nullable */
	private final String page;

	private int databaseId;

	public ItemKey(String institutionUrl, String uuid, int version, /* @Nullable */String contentId, String courseId,
		String folderId, /* @Nullable */String page)
	{
		this.institutionUrl = institutionUrl;
		this.uuid = uuid;
		this.version = version;
		this.contentId = contentId;
		this.courseId = courseId;
		this.folderId = folderId;
		this.page = page;
	}

	@Override
	public boolean equals(/* @Nullable */Object obj)
	{
		if( obj == null )
		{
			return false;
		}
		if( obj instanceof ItemKey )
		{
			final ItemKey other = (ItemKey) obj;

			if( contentId != null && other.contentId != null )
			{
				return contentId.equals(other.contentId);
			}

			return institutionUrl.equals(other.institutionUrl)
				&& uuid.equals(other.uuid)
				&& version == other.version
				&& courseId.equals(other.courseId)
				&& folderId.equals(other.folderId)
				&& ((page == null && other.page == null) || (page != null && other.page != null && page
					.equals(other.page)));
		}
		return false;
	}

	@SuppressWarnings("null")
	@Override
	public int hashCode()
	{
		return institutionUrl.hashCode() + uuid.hashCode() + version + courseId.hashCode() + folderId.hashCode()
			+ (page == null ? 0 : page.hashCode());
	}

	public String getInstitutionUrl()
	{
		return institutionUrl;
	}

	public String getUuid()
	{
		return uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public String getFolderId()
	{
		return folderId;
	}

	public int getDatabaseId()
	{
		return databaseId;
	}

	public void setDatabaseId(int databaseId)
	{
		this.databaseId = databaseId;
	}

	/* @Nullable */
	public String getPage()
	{
		return page;
	}

	/* @Nullable */
	public String getContentId()
	{
		return contentId;
	}

	@Override
	public String toString()
	{
		return "(" + contentId + ") " + institutionUrl + " - " + uuid + "/" + version
			+ (page == null ? "/(summary)" : "/attachment=" + page) + " in " + courseId + "/" + folderId;
	}
}