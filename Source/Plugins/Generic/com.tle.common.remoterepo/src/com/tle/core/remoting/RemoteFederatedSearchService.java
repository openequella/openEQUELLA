/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import com.tle.beans.entity.FederatedSearch;

public interface RemoteFederatedSearchService extends RemoteAbstractEntityService<FederatedSearch>
{
	String ENTITY_TYPE = "FEDERATED_SEARCH"; //$NON-NLS-1$
}
