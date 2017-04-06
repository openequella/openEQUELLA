package com.tle.core.payment.beans.store;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;

/**
 * @author Aaron
 */
// Note: I know item is not a base entity, but all the required fields are there
// :)
@XmlRootElement
public class StoreCatalogueItemBean extends BaseEntityBean
{
	private int version;
	private List<StoreCatalogueAttachmentBean> attachments;
	private boolean free;
	private StorePurchaseTierBean purchaseTier;
	private StoreSubscriptionTierBean subscriptionTier;
	private NavigationTreeBean navigation;

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public List<StoreCatalogueAttachmentBean> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<StoreCatalogueAttachmentBean> attachments)
	{
		this.attachments = attachments;
	}

	public boolean isFree()
	{
		return free;
	}

	public void setFree(boolean free)
	{
		this.free = free;
	}

	public StorePurchaseTierBean getPurchaseTier()
	{
		return purchaseTier;
	}

	public void setPurchaseTier(StorePurchaseTierBean purchaseTier)
	{
		this.purchaseTier = purchaseTier;
	}

	public StoreSubscriptionTierBean getSubscriptionTier()
	{
		return subscriptionTier;
	}

	public void setSubscriptionTier(StoreSubscriptionTierBean subscriptionTier)
	{
		this.subscriptionTier = subscriptionTier;
	}

	public void setNavigation(NavigationTreeBean navigation)
	{
		this.navigation = navigation;
	}

	public NavigationTreeBean getNavigation()
	{
		return navigation;
	}
}
