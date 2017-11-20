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

package com.tle.core.connectors.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;

import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.ConnectorTypeDescriptor;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.connectors.dao.ConnectorDao;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(ConnectorService.class)
@Singleton
@SecureEntity(ConnectorService.ENTITY_TYPE)
public class ConnectorServiceImpl extends AbstractEntityServiceImpl<ConnectorEditingBean, Connector, ConnectorService>
	implements
		ConnectorService
{
	protected final ConnectorDao connectorDao;

	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private DefaultConnectorExtensionService defaultExtension;

	private PluginTracker<ConnectorServiceExtension> typesTracker;
	private Map<String, ConnectorTypeDescriptor> typeDescriptorMap;
	private Collection<ConnectorTypeDescriptor> typeDescriptorList;

	@Inject
	public ConnectorServiceImpl(ConnectorDao dao)
	{
		super(Node.CONNECTOR, dao);
		connectorDao = dao;
	}

	@Override
	public List<Class<? extends Connector>> getAdditionalEntityClasses()
	{
		return null;
	}

	@Override
	public void afterAdd(EntityPack<Connector> pack)
	{
		Connector connector = pack.getEntity();
		getExtensionForConnector(connector.getLmsType()).add(connector);
	}

	private ConnectorServiceExtension getExtensionForConnector(String lmsType)
	{
		ConnectorServiceExtension extension = typesTracker.getBeanMap().get(lmsType);
		if( extension == null )
		{
			extension = defaultExtension;
		}
		return extension;
	}

	private synchronized Map<String, ConnectorTypeDescriptor> getTypeDescriptorMap()
	{
		if( typesTracker.needsUpdate() || typeDescriptorMap == null )
		{
			Map<String, ConnectorTypeDescriptor> tempTypeDescriptors = new HashMap<String, ConnectorTypeDescriptor>();

			for( Extension ext : typesTracker.getExtensions() )
			{
				final String type = ext.getParameter("id").valueAsString();
				final String nameKey = ext.getParameter("nameKey").valueAsString();
				final String descriptionKey = ext.getParameter("descriptionKey").valueAsString();
				// final String node = ext.getParameter("node").valueAsString();

				tempTypeDescriptors.put(type, new ConnectorTypeDescriptor(type, nameKey, descriptionKey));
			}

			typeDescriptorMap = Collections.unmodifiableMap(tempTypeDescriptors);
			typeDescriptorList = null;
		}

		return typeDescriptorMap;
	}

	private synchronized Collection<ConnectorTypeDescriptor> getTypeDescriptorList()
	{
		if( typesTracker.needsUpdate() || typeDescriptorList == null )
		{
			typeDescriptorList = getTypeDescriptorMap().values();
		}
		return typeDescriptorList;
	}

	@Override
	protected void doValidation(EntityEditingSession<ConnectorEditingBean, Connector> session, Connector connector,
		List<ValidationError> errors)
	{
		// Nothing to validate
	}

	@Override
	protected void doValidationBean(ConnectorEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);
	}

	@Override
	protected void deleteReferences(Connector connector)
	{
		getExtensionForConnector(connector.getLmsType()).deleteExtra(connector);
	}

	@Override
	public final EntityPack<Connector> startEditInternal(Connector entity)
	{
		ensureNonSystem(entity);
		EntityPack<Connector> pack = new EntityPack<Connector>();
		pack.setEntity(entity);

		// Prepare staging
		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(from) )
		{
			fileSystemService.copy(from, staging);
		}
		pack.setStagingID(staging.getUuid());

		fillTargetLists(pack);
		return pack;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected ConnectorEditingBean createEditingBean()
	{
		return new ConnectorEditingBean();
	}

	@Override
	public boolean canViewContent(BaseEntityLabel connector)
	{
		return canViewContent((Object) connector);
	}

	@Override
	public boolean canViewContent(Connector connector)
	{
		return canViewContent((Object) connector);
	}

	private boolean canViewContent(Object connector)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR);
		return !aclManager.filterNonGrantedPrivileges(connector, privs).isEmpty();
	}

	@Override
	public boolean canExport(Connector connector)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR);
		return !aclManager.filterNonGrantedPrivileges(connector, privs).isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<ConnectorEditingBean, Connector>> SESSION createSession(
		String sessionId, EntityPack<Connector> pack, ConnectorEditingBean bean)
	{
		return (SESSION) new ConnectorEditingSessionImpl(sessionId, pack, bean);
	}

	@Override
	protected void populateEditingBean(ConnectorEditingBean conBean, Connector entity)
	{
		super.populateEditingBean(conBean, entity);

		final TargetList targets = aclManager.getTargetList(Node.CONNECTOR, entity);
		String exportableExpression = null;
		String viewableExpression = null;
		for( TargetListEntry target : targets.getEntries() )
		{
			if( target.getPrivilege().equals(ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR) )
			{
				exportableExpression = target.getWho();
			}
			else if( target.getPrivilege().equals(ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR) )
			{
				viewableExpression = target.getWho();
			}
		}

		conBean.setContentExportableExpression(exportableExpression);
		conBean.setContentViewableExpression(viewableExpression);
		conBean.setLmsType(entity.getLmsType());
		conBean.setServerUrl(entity.getServerUrl());
		conBean.setUseLoggedInUsername(entity.isUseLoggedInUsername());
		conBean.setUsernameScript(entity.getUsernameScript());
		conBean.setExtraData(entity.getExtraData());
	}

	@Override
	protected void populateEntity(ConnectorEditingBean conBean, Connector entity)
	{
		super.populateEntity(conBean, entity);
		entity.setLmsType(conBean.getLmsType());
		entity.setServerUrl(conBean.getServerUrl());
		entity.setUseLoggedInUsername(conBean.isUseLoggedInUsername());
		entity.setUsernameScript(conBean.getUsernameScript());
		entity.setExtraData(conBean.getExtraData());

		// clone connector specific props
		final ConnectorServiceExtension connectorExtension = getExtensionForConnector(conBean.getLmsType());
		connectorExtension.edit(entity, conBean);
	}

	@Override
	protected void saveTargetLists(EntityEditingSession<ConnectorEditingBean, Connector> session,
		EntityPack<Connector> pack)
	{
		super.saveTargetLists(session, pack);

	}

	@Override
	protected void beforeClone(TemporaryFileHandle staging, EntityPack<Connector> pack)
	{
		// export the prefs into the staging area
		prepareExport(staging, pack.getEntity(),
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));
	}

	@Override
	public Collection<ConnectorTypeDescriptor> listAllAvailableTypes()
	{
		return getTypeDescriptorList();
	}

	@Override
	public Map<String, ConnectorTypeDescriptor> mapAllAvailableTypes()
	{
		return getTypeDescriptorMap();
	}

	@SecureOnReturn(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public List<BaseEntityLabel> listExportable()
	{
		return listEnabled();
	}

	@SecureOnReturn(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<BaseEntityLabel> listForViewing()
	{
		final List<BaseEntityLabel> allConnectors = Lists.newArrayList();
		// still a bit inefficient. You'd probably want the dao to return a
		// ConnectorBaseEntityLabel which includes the connector type as well.
		for( Connector connector : connectorDao.enumerateAll() )
		{
			if( supportsFindUses(connector) && !connector.isDisabled() )
			{
				allConnectors.add(new BaseEntityLabel(connector.getId(), connector.getUuid(),
					connector.getName().getId(), connector.getOwner(), connector.isSystemType()));
			}
		}

		return allConnectors;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		typesTracker = new PluginTracker<ConnectorServiceExtension>(pluginService, "com.tle.core.connectors",
			"connectorType", "id");
		typesTracker.setBeanKey("serviceClass");
	}

	@Override
	public Connector getForEdit(String connectorUuid)
	{
		Connector connector = getByUuid(connectorUuid);
		getExtensionForConnector(connector.getLmsType()).loadExtra(connector);
		return connector;
	}

	private boolean supportsFindUses(Connector connector)
	{
		return repositoryService.supportsFindUses(connector.getLmsType());
	}

	@Override
	public List<Connector> enumerateForUrl(String url)
	{
		return connectorDao.enumerateForUrl(url);
	}
}
