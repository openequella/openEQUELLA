package com.tle.web.api.notification.interfaces.beans;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;

@XmlRootElement
public class NotificationApiBean extends AbstractExtendableBean
{
	@Deprecated
	private Long id;
	private String uuid;
	private String reason;
	private String userTo;
	private Date date;
	private ItemBean item;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
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

	public ItemBean getItem()
	{
		return item;
	}

	public void setItem(ItemBean itemBean)
	{
		this.item = itemBean;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public String getUserTo()
	{
		return userTo;
	}

	public void setUserTo(String userTo)
	{
		this.userTo = userTo;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}
}
