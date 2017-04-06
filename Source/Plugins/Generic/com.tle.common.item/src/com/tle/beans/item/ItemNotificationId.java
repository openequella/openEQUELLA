package com.tle.beans.item;

public class ItemNotificationId extends AbstractItemKey
{
	private Long notificationId;

	public ItemNotificationId(ItemKey key, long notificationId)
	{
		super(key.getUuid(), key.getVersion());
		setNotificationId(notificationId);
	}

	public ItemNotificationId(String uuid, int ver, long notificationId)
	{
		super(uuid, ver);
		setNotificationId(notificationId);
	}

	public void setNotificationId(long notificationId)
	{
		this.notificationId = notificationId;
	}

	public long getNotificationId()
	{
		return notificationId;
	}

	@Override
	public String toString(int version)
	{
		return super.toString(version) + ":" + notificationId.toString(); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj) && (this.getNotificationId() == ((ItemNotificationId) obj).getNotificationId());
	}

	@Override
	public int hashCode()
	{
		int ni = (int) (notificationId == null ? 0 : notificationId);
		return super.hashCode() + ni;
	}
}
