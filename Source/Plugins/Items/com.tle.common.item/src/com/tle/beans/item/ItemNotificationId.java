/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
