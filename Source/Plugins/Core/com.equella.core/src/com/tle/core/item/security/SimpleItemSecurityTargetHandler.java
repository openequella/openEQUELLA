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

package com.tle.core.item.security;

import java.util.Collection;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.SecurityTargetHandler;

@Bind
@Singleton
public class SimpleItemSecurityTargetHandler implements SecurityTargetHandler
{

	@Override
	public void gatherAllLabels(Set<String> labels, Object target)
	{
		SimpleItemSecurity itemSecurity = (SimpleItemSecurity) target;
		long collectionId = itemSecurity.getCollectionId();
		String status = itemSecurity.getStatus();

		labels.add(SecurityConstants.TARGET_ITEM + ':' + itemSecurity.getItemId());
		labels.add(SecurityConstants.TARGET_ITEM_STATUS + ':' + status);
		labels.add(SecurityConstants.TARGET_ITEM_STATUS + ':' + collectionId + ':' + status);
		labels.add(SecurityConstants.TARGET_BASEENTITY + ':' + collectionId);

		Collection<String> metadataTargets = itemSecurity.getMetadataTargets();
		if( !Check.isEmpty(metadataTargets) )
		{
			for( String metaTarget : metadataTargets )
			{
				labels.add(SecurityConstants.TARGET_ITEM_METADATA + ':' + collectionId + ':' + metaTarget);
			}
		}
	}

	@Override
	public String getPrimaryLabel(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object transform(Object target)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOwner(Object target, String userId)
	{
		return ((SimpleItemSecurity) target).isOwner();
	}

}
