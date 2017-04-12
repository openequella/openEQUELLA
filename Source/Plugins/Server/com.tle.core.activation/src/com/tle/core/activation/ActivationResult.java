package com.tle.core.activation;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.services.item.FreetextResult;

public class ActivationResult extends FreetextResult
{
	private static final long serialVersionUID = 1L;

	private final String activationId;

	public ActivationResult(ItemIdKey key, String activationId, float relevance, boolean sortByRelevance)
	{
		super(key, relevance, sortByRelevance);
		this.activationId = activationId;
	}

	public String getActivationId()
	{
		return activationId;
	}

}
