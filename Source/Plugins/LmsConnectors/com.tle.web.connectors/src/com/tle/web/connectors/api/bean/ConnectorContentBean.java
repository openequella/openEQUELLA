/**
 * 
 */
package com.tle.web.connectors.api.bean;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * This class exists to provide item-usage information for the REST API
 * implementation of connector/uses query, and is a scaled down version of the
 * ConnectorContent class.
 * 
 * @author larry
 */
@XmlRootElement
public class ConnectorContentBean extends AbstractExtendableBean
{
	private String id;
	private String courseId;
	private String course;
	private String courseCode;
	private String apiLink;
	// private String courseUrl;
	// private String folderId;
	private String folder;
	// private String folderUrl;
	private Date dateAdded;
	// private Date dateModified;
	// private String attachmentUuid;
	// private String attachmentUrl;
	// private String uuid;
	// private int version;
	private String externalTitle;
	private String externalDescription;

	// private boolean available;
	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getCourse()
	{
		return course;
	}

	public void setCourse(String course)
	{
		this.course = course;
	}

	public String getCourseCode()
	{
		return courseCode;
	}

	public void setCourseCode(String courseCode)
	{
		this.courseCode = courseCode;
	}

	public String getApiLink()
	{
		return apiLink;
	}

	public void setApiLink(String apiLink)
	{
		this.apiLink = apiLink;
	}

	public String getFolder()
	{
		return folder;
	}

	public void setFolder(String folder)
	{
		this.folder = folder;
	}

	public Date getDateAdded()
	{
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded)
	{
		this.dateAdded = dateAdded;
	}

	public String getExternalTitle()
	{
		return externalTitle;
	}

	public void setExternalTitle(String externalTitle)
	{
		this.externalTitle = externalTitle;
	}

	public String getExternalDescription()
	{
		return externalDescription;
	}

	public void setExternalDescription(String externalDescription)
	{
		this.externalDescription = externalDescription;
	}
}
