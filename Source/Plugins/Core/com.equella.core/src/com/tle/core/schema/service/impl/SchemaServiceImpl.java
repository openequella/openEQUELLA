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

package com.tle.core.schema.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.Schema.CloneDefinition;
import com.tle.beans.entity.SchemaTransform;
import com.tle.beans.entity.schema.Citation;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.schema.dao.SchemaDao;
import com.tle.core.schema.event.SchemaDeletionEvent;
import com.tle.core.schema.event.SchemaReferencesEvent;
import com.tle.core.schema.extension.SchemaSaveExtension;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.ValidationHelper;
import com.tle.core.xslt.service.XsltService;

/**
 * @author Nicholas Read
 */
@Bind(SchemaService.class)
@Singleton
@SecureEntity(RemoteSchemaService.ENTITY_TYPE)
public class SchemaServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, Schema, SchemaService>
	implements
		SchemaService
{
	private static final String INSTITUTION_URL_NODE = "institutionUrl"; //$NON-NLS-1$
	private static final String[] BLANKS = {"name"}; //$NON-NLS-1$

	@Inject
	private PluginTracker<SchemaReferences> referenceTracker;
	@Inject
	private PluginTracker<SchemaSaveExtension> saveExtensions;
	@Inject
	private XsltService xsltService;

	private final SchemaDao schemaDao;

	@Inject
	public SchemaServiceImpl(SchemaDao schemaDao)
	{
		super(Node.SCHEMA, schemaDao);
		this.schemaDao = schemaDao;
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		SchemaReferencesEvent event = new SchemaReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}

	@Override
	@Transactional
	public List<BaseEntityLabel> getSchemaUses(long id)
	{
		List<BaseEntityLabel> results = Lists.newArrayList();
		for( SchemaReferences refs : referenceTracker.getBeanList() )
		{
			results.addAll(refs.getSchemaUses(id));
		}
		return results;
	}

	@Override
	protected void deleteReferences(Schema schema)
	{
		publishEvent(new SchemaDeletionEvent(schema));
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, Schema> session, Schema entity,
		List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, BLANKS, errors);
	}

	@Override
	public List<String> getExportSchemaTypes()
	{
		return schemaDao.getExportSchemaTypes();
	}

	@Override
	public List<String> getImportSchemaTypes(long id)
	{
		return schemaDao.getImportSchemaTypes(id);
	}

	@Override
	public Set<Schema> getSchemasForExportSchemaType(String type)
	{
		return new HashSet<Schema>(schemaDao.getSchemasForExportSchemaType(type));
	}

	@SuppressWarnings("nls")
	@Override
	protected void afterStopEdit(EntityPack<Schema> pack, Schema oldSchema)
	{
		final Schema newSchema = pack.getEntity();
		for( SchemaSaveExtension extension : saveExtensions.getBeanList() )
		{
			extension.schemaSaved(oldSchema, newSchema);
		}
	}

	@Override
	public String transformForExport(long schemaId, String type, PropBagEx itemxml, boolean omitXmlDeclaration)
	{
		itemxml.setNode(INSTITUTION_URL_NODE, CurrentInstitution.get().getUrl());
		return transform(schemaId, type, itemxml, get(schemaId).getExportTransforms(), omitXmlDeclaration);
	}

	@Override
	public String transformForImport(long schemaId, String type, PropBagEx foreignXml)
	{
		return transform(schemaId, type, foreignXml, get(schemaId).getImportTransforms(), false);
	}

	private String transform(long schemaId, String type, PropBagEx xml, List<SchemaTransform> transforms,
		boolean omitXmlDeclaration)
	{
		String filename = null;
		for( SchemaTransform transform : transforms )
		{
			if( transform.getType().equalsIgnoreCase(type) )
			{
				filename = transform.getFilename();
				break;
			}
		}

		if( filename == null )
		{
			return null;
		}
		else
		{
			EntityFile file = new EntityFile(schemaId);
			return xsltService.transform(file, filename, xml, omitXmlDeclaration);
		}
	}

	@Override
	protected void preUnlinkForClone(Schema schema)
	{
		Hibernate.initialize(schema.getExportTransforms());
		Hibernate.initialize(schema.getImportTransforms());
		Hibernate.initialize(schema.getCitations());
	}

	@Override
	protected void postUnlinkForClone(Schema schema)
	{
		schema.setExportTransforms(new ArrayList<SchemaTransform>(schema.getExportTransforms()));
		schema.setImportTransforms(new ArrayList<SchemaTransform>(schema.getImportTransforms()));
		schema.setCitations(new ArrayList<Citation>(schema.getCitations()));
	}

	@Override
	public void prepareImport(TemporaryFileHandle importFolder, Schema entity, ConverterParams params)
	{
		super.prepareImport(importFolder, entity, params);
		entity.setDefinition(entity.withDefinition(new CloneDefinition()));
	}
}
