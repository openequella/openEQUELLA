package com.tle.core.payment;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;
import com.tle.common.property.annotation.PropertyList;

/**
 * @author Dustin Lashmar
 */
public class StoreSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@Property(key = "store.name")
	private String name;

	@Property(key = "store.description")
	private String description;

	@Property(key = "store.icon")
	private String icon;

	@Property(key = "store.iconsmall")
	private String iconSmall;

	@Property(key = "store.contact.name")
	private String contactName;

	@Property(key = "store.contact.number")
	private String contactNumber;

	@Property(key = "store.contact.email")
	private String contactEmail;

	@Property(key = "store.enabled")
	private boolean enabled;

	@PropertyList(key = "store.administrators")
	private List<String> admins = new ArrayList<String>(); // UUIDS

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getIconSmall()
	{
		return iconSmall;
	}

	public void setIconSmall(String iconSmall)
	{
		this.iconSmall = iconSmall;
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

	public List<String> getAdmins()
	{
		return admins;
	}

	public void setAdmins(List<String> admins)
	{
		this.admins = admins;
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public boolean getEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
