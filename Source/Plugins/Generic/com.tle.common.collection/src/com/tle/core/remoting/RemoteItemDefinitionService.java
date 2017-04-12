/*
 * Created on 18/04/2006
 */
package com.tle.core.remoting;

import java.util.List;
import java.util.Set;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;

public interface RemoteItemDefinitionService extends RemoteAbstractEntityService<ItemDefinition>
{

	String ENTITY_TYPE = "COLLECTION"; //$NON-NLS-1$

	Set<String> enumerateCategories();

	List<BaseEntityLabel> listUsableItemDefinitionsForSchema(long schemaID);

	long getSchemaIdForCollectionUuid(String value);

	byte[] exportControl(String controlXml);

	String importControl(byte[] zipFileData) throws Exception;
}
