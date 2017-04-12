package com.tle.core.payment.service.session;

import com.tle.core.services.entity.EntityEditingBean;

public class PaymentGatewayEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private Object extraData;
	private String type;

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

}
