/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import java.util.List;

import com.tle.beans.entity.PowerSearch;

public interface RemotePowerSearchService extends RemoteAbstractEntityService<PowerSearch>
{
	String ENTITY_TYPE = "POWER_SEARCH"; //$NON-NLS-1$

	List<Long> enumerateItemdefIds(long powerSearchId);
}
