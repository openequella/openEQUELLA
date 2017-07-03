/*
 * Created on Oct 26, 2005
 */
package com.tle.core.schema.dao;

import java.util.List;

import com.tle.beans.entity.Schema;
import com.tle.core.entity.dao.AbstractEntityDao;

public interface SchemaDao extends AbstractEntityDao<Schema>
{
	List<String> getExportSchemaTypes();

	List<String> getImportSchemaTypes(long id);

	List<Schema> getSchemasForExportSchemaType(String type);
}
