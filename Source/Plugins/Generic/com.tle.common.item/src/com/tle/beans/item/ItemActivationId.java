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
