/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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