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
