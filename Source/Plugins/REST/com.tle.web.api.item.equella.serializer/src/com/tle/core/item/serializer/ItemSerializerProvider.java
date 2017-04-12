package com.tle.core.item.serializer;

import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

/**
 * Methods will be invoked in the order you see them listed here.
 * 
 * @author nick
 */
public interface ItemSerializerProvider
{
	/**
	 * There will always be a query of the item table, so feel free to add your
	 * own selects, joins, etc... and it'll get done for you. Any projections
	 * that you add must include an alias, and due to a hibernate 3.5 bug (fixed
	 * in 3.6), aliases cannot be the same as the property.
	 */
	void prepareItemQuery(ItemSerializerState state);

	/**
	 * If you can't get your results in the normal item query, then you can
	 * perform any additional queries in here. Store you state in
	 * state.queryResults for each record. Try to perform queries for all search
	 * results in one go, and minimise the number of queries overall.
	 * <p>
	 * If there are language bundles to resolve to the current locale, add them
	 * to the state using addBundleToResolve(). All bundles will be retrieved
	 * later in a minimum number of queries.
	 * <p>
	 * The results from the base item query are available from state at this
	 * point.
	 */
	void performAdditionalQueries(ItemSerializerState state);

	void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId);

	void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId);
}
