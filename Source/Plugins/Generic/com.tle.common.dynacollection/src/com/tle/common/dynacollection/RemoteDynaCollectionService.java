package com.tle.common.dynacollection;

import com.tle.beans.entity.DynaCollection;
import com.tle.core.remoting.RemoteAbstractEntityService;

public interface RemoteDynaCollectionService extends RemoteAbstractEntityService<DynaCollection>
{

	String ENTITY_TYPE = "DYNA_COLLECTION"; //$NON-NLS-1$
}
