package com.tle.beans.item;

public class ItemIdKey extends AbstractItemKey
{
	private static final long serialVersionUID = 1;

	protected long key;

	public ItemIdKey(String str)
	{
		super();
		int slash = str.indexOf('/');
		if( slash == -1 )
		{
			throw new IllegalArgumentException("Error parsing ItemIdKey:" + str);
		}
		key = Integer.parseInt(str.substring(0, slash));
		ItemId itemId = new ItemId(str.substring(slash + 1));
		uuid = itemId.getUuid();
		version = itemId.getVersion();
	}

	public ItemIdKey(long key, String uuid, int version)
	{
		super(uuid, version);
		this.key = key;
	}

	public ItemIdKey(long key, ItemKey itemKey)
	{
		this(key, itemKey.getUuid(), itemKey.getVersion());
	}

	public ItemIdKey(Item item)
	{
		this(item.getId(), item.getUuid(), item.getVersion());
	}

	public long getKey()
	{
		return key;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( !super.equals(obj) )
		{
			return false;
		}
		return key == ((ItemIdKey) obj).key;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + (int) key;
	}

	@Override
	public String toString(int version)
	{
		return key + "/" + super.toString(version); //$NON-NLS-1$
	}
}
