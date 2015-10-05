package com.tle.web.api.payment;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.payment.StoreFrontEditorImpl.StoreFrontEditorFactory;
import com.tle.web.api.payment.beans.StoreFrontBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class StoreFrontBeanSerializer
	extends
		AbstractEquellaBaseEntitySerializer<StoreFront, StoreFrontBean, StoreFrontEditor>
{
	@Inject
	private StoreFrontService storeFrontService;
	@Inject
	private StoreFrontEditorFactory editorFactory;

	@Override
	protected StoreFrontBean createBean()
	{
		return new StoreFrontBean();
	}

	@Override
	protected StoreFront createEntity()
	{
		return new StoreFront();
	}

	@Override
	protected StoreFrontEditor createExistingEditor(StoreFront entity, String stagingUuid, String lockId,
		boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected StoreFrontEditor createNewEditor(StoreFront entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected void copyCustomFields(StoreFront storeFront, StoreFrontBean bean, Object data)
	{
		// bean.setClient(convertOAuthClientBean(storeFront.getClient()));
		bean.setContactPhone(storeFront.getContactPhone());
		bean.setCountry(storeFront.getCountry());
		bean.setFree(storeFront.isAllowFree());
		bean.setProduct(storeFront.getProduct());
		bean.setProductVersion(storeFront.getProductVersion());
		bean.setPurchase(storeFront.isAllowPurchase());
		bean.setSubscription(storeFront.isAllowSubscription());
	}

	@Override
	protected AbstractEntityService<?, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.STOREFRONT;
	}
}
