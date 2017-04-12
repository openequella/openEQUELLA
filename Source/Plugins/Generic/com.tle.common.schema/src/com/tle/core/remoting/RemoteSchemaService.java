/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;

public interface RemoteSchemaService extends RemoteAbstractEntityService<Schema>
{
	String ENTITY_TYPE = "SCHEMA"; //$NON-NLS-1$

	List<BaseEntityLabel> getSchemaUses(long id);

	List<String> getImportSchemaTypes(long id);
}
