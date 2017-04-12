/*
 * Created on Oct 26, 2005
 */
package com.tle.core.schema;

import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.core.dao.AbstractEntityDao;

public interface SchemaDao extends AbstractEntityDao<Schema>
{
	List<String> getExportSchemaTypes();

	List<String> getImportSchemaTypes(long id);

	List<Schema> getSchemasForExportSchemaType(String type);

	/**
	 * @deprecated Use an event to ask for reference
	 */
	@Deprecated
	List<Class<?>> getReferencingClasses(long id);
}
