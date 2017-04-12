package com.tle.core.cloud.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
// Note: I know item is not a base entity, but all the required fields are there
// :)
@XmlRootElement
public class CloudItemBean extends BaseEntityBean
{
	private int version;
	private List<CloudAttachmentBean> attachments;
	private String metadata;
	private CloudNavigationSettingsBean navigation;

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public List<CloudAttachmentBean> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<CloudAttachmentBean> attachments)
	{
		this.attachments = attachments;
	}

	public String getMetadata()
	{
		return metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	public CloudNavigationSettingsBean getNavigation()
	{
		return navigation;
	}

	public void setNavigation(CloudNavigationSettingsBean navigation)
	{
		this.navigation = navigation;
	}
}
