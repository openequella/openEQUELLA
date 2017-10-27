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

package com.tle.core.activation.index;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ImmutableMap;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.ActivationResult;
import com.tle.core.freetext.index.MultipleIndex;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class ActivationIndex extends MultipleIndex<ActivationResult>
{
	private static final Map<String, String> privMap = ImmutableMap.of(ActivationConstants.VIEW_ACTIVATION_ITEM,
		ActivationConstants.VIEW_ACTIVATION_ITEM_PFX, ActivationConstants.DELETE_ACTIVATION_ITEM,
		ActivationConstants.DELETE_ACTIVATION_ITEM_PFX);

	@Override
	protected Set<String> getKeyFields()
	{
		return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID,
			FreeTextQuery.FIELD_ACTIVATION_ID));
	}

	@Override
	public String getIndexId()
	{
		return ActivationConstants.ACTIVATION_INDEX_ID;
	}

	@Override
	protected String getPrefixForPrivilege(String priv)
	{
		if( privMap.containsKey(priv) )
		{
			return privMap.get(priv);
		}
		return super.getPrefixForPrivilege(priv);
	}

	@Override
	protected ActivationResult createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance)
	{
		return new ActivationResult(key, doc.get(FreeTextQuery.FIELD_ACTIVATION_ID), relevance, sortByRelevance);
	}
}
