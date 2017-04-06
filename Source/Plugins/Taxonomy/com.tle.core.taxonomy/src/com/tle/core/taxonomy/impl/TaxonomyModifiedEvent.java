package com.tle.core.taxonomy.impl;

import com.tle.core.events.ApplicationEvent;

public class TaxonomyModifiedEvent extends ApplicationEvent<TaxonomyModifiedListener>
{
	private static final long serialVersionUID = 1L;

	private final String taxonomyUuid;

	public TaxonomyModifiedEvent(String taxonomyUuid)
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
		this.taxonomyUuid = taxonomyUuid;
	}

	public String getTaxonomyUuid()
	{
		return taxonomyUuid;
	}

	@Override
	public Class<TaxonomyModifiedListener> getListener()
	{
		return TaxonomyModifiedListener.class;
	}

	@Override
	public void postEvent(TaxonomyModifiedListener listener)
	{
		listener.taxonomyModifiedEvent(this);
	}
}
