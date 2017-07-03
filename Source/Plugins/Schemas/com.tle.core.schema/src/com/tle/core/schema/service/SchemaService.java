package com.tle.core.schema.service;

import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.Schema;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemoteSchemaService;

/**
 * @author Nicholas Read
 */
public interface SchemaService extends AbstractEntityService<EntityEditingBean, Schema>, RemoteSchemaService
{
	List<String> getExportSchemaTypes();

	Set<Schema> getSchemasForExportSchemaType(String type);

	// Need this for OAI, otherwise run out of connections...
	String transformForExport(long id, String type, PropBagEx itemxml, boolean omitXmlDeclaration);

	String transformForImport(long id, String type, PropBagEx foreignXml);
}