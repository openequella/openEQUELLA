package com.tle.beans.item;

public final class ItemId extends AbstractItemKey
{
	private static final long serialVersionUID = 1;

	public ItemId(String uuid, int version)
	{
		super(uuid, version);
	}

	@SuppressWarnings("nls")
	public ItemId(String str)
	{
		super();
		int slash = str.indexOf('/');
		if( slash == -1 )
		{
			throw new IllegalArgumentException("Error parsing ItemId:" + str);
		}
		uuid = str.substring(0, slash);
		String versionPart = str.substring(slash + 1);
		try
		{
			version = Integer.parseInt(str.substring(slash + 1));
		}
		catch( NumberFormatException nfe )
		{
			version = -1;
		}
	}

	public static ItemId fromKey(ItemKey itemKey)
	{
		if( itemKey == null )
		{
			return null;
		}
		if( itemKey instanceof ItemId )
		{
			return (ItemId) itemKey;
		}
		return new ItemId(itemKey.getUuid(), itemKey.getVersion());
	}
}
