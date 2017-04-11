package com.tle.web.api.staging.interfaces.beans;

import java.util.List;

public class MultipartCompleteBean
{
	private List<PartBean> parts;

	public List<PartBean> getParts()
	{
		return parts;
	}

	public void setParts(List<PartBean> parts)
	{
		this.parts = parts;
	}
}
