package com.tle.blackboard.webservice;

public class Content
{
	private String id;
	private String uuid;
	private int version;
	private String title;
	private String description;
	// private Course course;
	// private Folder folder;
	private long createdDate;
	private long modifiedDate;
	private boolean available;
	private String page;
	private String folderUrl;
	private int usageId;
	private String courseId;
	private String folderId;
	private long dateAccessed;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	// public Course getCourse()
	// {
	// return course;
	// }
	//
	// public void setCourse(Course course)
	// {
	// this.course = course;
	// }

	// public Folder getFolder()
	// {
	// return folder;
	// }
	//
	// public void setFolder(Folder folder)
	// {
	// this.folder = folder;
	// }

	public long getCreatedDate()
	{
		return createdDate;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	public void setCreatedDate(long createdDate)
	{
		this.createdDate = createdDate;
	}

	public long getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(long modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	public String getPage()
	{
		return page;
	}

	public void setPage(String page)
	{
		this.page = page;
	}

	public String getFolderUrl()
	{
		return folderUrl;
	}

	public void setFolderUrl(String folderUrl)
	{
		this.folderUrl = folderUrl;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getUsageId()
	{
		return usageId;
	}

	public void setUsageId(int usageId)
	{
		this.usageId = usageId;
	}

	@Override
	public String toString()
	{
		return id + ": " + title + " (" + uuid + "/" + version + (page == null ? "" : "/" + page) + ") in "
			+ (getCourseId() == null ? "?" : getCourseId()) + "/" + (getFolderId() == null ? "?" : getFolderId());
	}

	public long getDateAccessed()
	{
		return dateAccessed;
	}

	public void setDateAccessed(long dateAccessed)
	{
		this.dateAccessed = dateAccessed;
	}
}
