package com.tle.web.activation;

import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.events.RenderContext;

@Bind
public class ActivationItemListEntry extends AbstractItemListEntry
{
	private String activationId;

	public void setActivationId(String activationId)
	{
		this.activationId = activationId;
	}

	public String getActivationId()
	{
		return activationId;
	}

	@Override
	protected void setupMetadata(RenderContext context)
	{
		// Do nothing
	}
}
