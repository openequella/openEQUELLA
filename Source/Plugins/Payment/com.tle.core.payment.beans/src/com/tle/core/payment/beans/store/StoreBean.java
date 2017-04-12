package com.tle.core.payment.beans.store;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreBean extends BaseEntityBean
{
	private String icon;
	private String image;
	private String contactName;
	private String contactNumber;
	private String contactEmail;
	private boolean enabled;
	private Date lastHarvest;

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getContactName()
	{
		return contactName;
	}

	public void setContactName(String contactName)
	{
		this.contactName = contactName;
	}

	public String getContactNumber()
	{
		return contactNumber;
	}

	public void setContactNumber(String contactNumber)
	{
		this.contactNumber = contactNumber;
	}

	public String getContactEmail()
	{
		return contactEmail;
	}

	public void setContactEmail(String contactEmail)
	{
		this.contactEmail = contactEmail;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public Date getLastHarvest()
	{
		return lastHarvest;
	}

	public void setLastHarvest(Date lastHarvest)
	{
		this.lastHarvest = lastHarvest;
	}
}
