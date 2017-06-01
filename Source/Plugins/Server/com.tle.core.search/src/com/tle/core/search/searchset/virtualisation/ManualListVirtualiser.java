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

package com.tle.core.search.searchset.virtualisation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.common.GeneralConstants;
import com.thoughtworks.xstream.XStream;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.guice.Bind;
import com.tle.core.search.VirtualisableAndValue;

@Bind
@Singleton
public class ManualListVirtualiser implements SearchSetVirtualiser
{
	private static XStream xstream = new XStream();

	/**
	 * For a hierarchical topic (hence searchSet.supportsHierachy), we do a
	 * matrixSearch to determine the tally that each search returns.
	 * 
	 * @param results
	 * @param obj
	 * @param set
	 * @param helper
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> void expandSearchSet(List<VirtualisableAndValue<T>> results, T obj, SearchSet set,
		Map<String, String> mappedValues, Collection<String> collectionUuids, VirtualisationHelper<T> helper)
	{
		for( String value : (List<String>) xstream.fromXML(set.getAttribute(set.getVirtualiserPluginId())) )
		{
			results.add(helper.newVirtualisedPathFromPrototypeForValue(obj, value, GeneralConstants.UNCALCULATED));
		}
	}
}
