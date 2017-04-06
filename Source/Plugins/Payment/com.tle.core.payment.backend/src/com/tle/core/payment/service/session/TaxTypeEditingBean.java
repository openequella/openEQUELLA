package com.tle.core.payment.service.session;

import com.tle.core.services.entity.EntityEditingBean;

public class TaxTypeEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String code;
	private String percent;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getPercent()
	{
		return percent;
	}

	public void setPercent(String percent)
	{
		this.percent = percent;
	}
}