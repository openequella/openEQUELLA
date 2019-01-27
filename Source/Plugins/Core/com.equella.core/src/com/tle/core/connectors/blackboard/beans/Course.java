package com.tle.core.connectors.blackboard.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Course
{
	private String id;
	private String uuid;
	private String externalId;
	private String dataSourceId;
	private String courseId;
	private String name;
	private String created;
	private String organization;
	private String ultraStatus;
	private Boolean allowGuests;
	private Boolean readOnly;
	private Availability availability;
	private Enrollment enrollment;
	private Locale locale;
	private String externalAccessUrl;
	private String guestAccessUrl;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getExternalId()
	{
		return externalId;
	}

	public void setExternalId(String externalId)
	{
		this.externalId = externalId;
	}

	public String getDataSourceId()
	{
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId)
	{
		this.dataSourceId = dataSourceId;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCreated()
	{
		return created;
	}

	public void setCreated(String created)
	{
		this.created = created;
	}

	public String getOrganization()
	{
		return organization;
	}

	public void setOrganization(String organization)
	{
		this.organization = organization;
	}

	public String getUltraStatus()
	{
		return ultraStatus;
	}

	public void setUltraStatus(String ultraStatus)
	{
		this.ultraStatus = ultraStatus;
	}

	public Boolean getAllowGuests()
	{
		return allowGuests;
	}

	public void setAllowGuests(Boolean allowGuests)
	{
		this.allowGuests = allowGuests;
	}

	public Boolean getReadOnly()
	{
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	public Availability getAvailability()
	{
		return availability;
	}

	public void setAvailability(Availability availability)
	{
		this.availability = availability;
	}

	public Enrollment getEnrollment()
	{
		return enrollment;
	}

	public void setEnrollment(Enrollment enrollment)
	{
		this.enrollment = enrollment;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	public String getExternalAccessUrl()
	{
		return externalAccessUrl;
	}

	public void setExternalAccessUrl(String externalAccessUrl)
	{
		this.externalAccessUrl = externalAccessUrl;
	}

	public String getGuestAccessUrl()
	{
		return guestAccessUrl;
	}

	public void setGuestAccessUrl(String guestAccessUrl)
	{
		this.guestAccessUrl = guestAccessUrl;
	}

	@XmlRootElement
	public static class Enrollment
	{
		private String type; //InstructorLed

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}
	}

	@XmlRootElement
	public static class Locale
	{
		private Boolean force;

		public Boolean getForce()
		{
			return force;
		}

		public void setForce(Boolean force)
		{
			this.force = force;
		}
	}
}


