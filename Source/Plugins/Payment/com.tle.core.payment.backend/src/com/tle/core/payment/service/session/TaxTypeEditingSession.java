package com.tle.core.payment.service.session;

import com.tle.common.EntityPack;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

/**
 * @author Aaron
 */
public class TaxTypeEditingSession extends EntityEditingSessionImpl<TaxTypeEditingBean, TaxType>
{
	private static final long serialVersionUID = 1L;

	public TaxTypeEditingSession(String sessionId, EntityPack<TaxType> pack, TaxTypeEditingBean bean)
	{
		super(sessionId, pack, bean);
	}
}
