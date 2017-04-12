package com.tle.beans.item;

import com.tle.common.Check;

public class ItemActivationId extends AbstractItemKey implements ItemKeyExtension
{
	private static final long serialVersionUID = 1L;

	public static final String PARAM_KEY = "ACT_ID"; //$NON-NLS-1$
	private String activationId;

	public ItemActivationId(ItemKey key, String extraId)
	{
		super(key.getUuid(), key.getVersion());
		setActivationId(extraId);
	}

	public ItemActivationId(String uuid, int ver, String extraId)
	{
		super(uuid, ver);
		setActivationId(extraId);
	}

	public String getActivationId()
	{
		return activationId;
	}

	public void setActivationId(String activationId)
	{
		this.activationId = activationId;
	}

	@Override
	public String toString(int version)
	{
		return super.toString(version) + ':' + activationId;
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(uuid, version, activationId);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !super.equals(obj) )
		{
			return false;
		}
		ItemActivationId rhs = (ItemActivationId) obj;
		return activationId.equals(rhs.getActivationId());
	}

	@Override
	public String getExtensionId()
	{
		return "activation"; //$NON-NLS-1$
	}
}
