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

package com.tle.web.api.institution;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.exceptions.BadRequestException;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.mime.MimeEntry;
import com.tle.beans.usermanagement.standard.wrapper.GroupWrapperSettings;
import com.tle.beans.usermanagement.standard.wrapper.RoleWrapperSettings;
import com.tle.beans.usermanagement.standard.wrapper.UserWrapperSettings;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.hash.Hash;
import com.tle.common.i18n.KeyString;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.mimetypes.dao.MimeEntryDao;
import com.tle.core.mimetypes.institution.MimeMigrator;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.security.RunAsUser;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.system.SystemConfigService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.institution.interfaces.InstitutionResource;
import com.tle.web.api.institution.interfaces.beans.InstitutionBean;
import com.tle.web.api.interfaces.beans.SearchBean;

/**
 * See interface class for @Path and other annotations
 * 
 * @author larry
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(InstitutionResource.class)
@Singleton
public class InstitutionResourceImpl implements InstitutionResource
{
	private static final Logger LOGGER = Logger.getLogger(InstitutionResource.class);

	@Inject
	private InstitutionService institutionService;
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private SchemaDataSourceService schemaDataSourceService;
	@Inject
	private InstitutionImportService institutionImportService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private RunAsUser runAs;
	@Inject
	private ConfigurationService configService;
	@Inject
	private UserService userService;
	@Inject
	private MimeEntryDao mimeEntryDao;

	// For compatibility with EPS test and error syntax
	private static final String VALDATION_PREAMBLE = "Validation errors: ";
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(InstitutionService.class) + '.';

	@SecureOnCallSystem
	@Override
	public Response newInstitution(long schemaId, final InstitutionBean institutionBean)
	{
		// Throws BadRequestException on invalidation
		validateEssentialFields(institutionBean);

		if( schemaId == -1 )
		{
			Collection<SchemaInfo> values = institutionService.getAllSchemaInfos();
			for( SchemaInfo schemaInfo : values )
			{
				if( !schemaInfo.isSystem() )
				{
					schemaId = schemaInfo.getDatabaseSchema().getId();
					break;
				}
			}

			if( schemaId == -1 )
			{
				throw new RuntimeException("No available schemas");
			}
		}
		final Institution newInsti;
		try
		{
			newInsti = saveNewInstitution(institutionBean, schemaId);
		}
		catch( Exception ex )
		{
			// If it's an access denied error, then we can meet the callers'
			// expectations on that score ...
			if( ex instanceof AccessDeniedException
				|| (ex.getCause() != null && ex.getCause() instanceof AccessDeniedException) )
			{
				return Response.status(Status.FORBIDDEN).entity(ex.getLocalizedMessage()).build();
			}
			else
			// it's some other stuff ...
			{
				LOGGER.error("Error creating institution", ex);
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getLocalizedMessage()).build();
			}
		}

		long newInstitutonId = newInsti.getUniqueId();
		return Response.status(Status.CREATED).location(createUrl(newInstitutonId)).build();
	}

	@Transactional
	protected void makeDefaults(Institution institution)
	{
		// Dirty dirty hack
		// There should be an endpoint to turn these on/off

		UserWrapperSettings user = configService.getProperties(new UserWrapperSettings());
		user.setEnabled(true);
		userService.setPluginConfig(user);

		GroupWrapperSettings group = configService.getProperties(new GroupWrapperSettings());
		group.setEnabled(true);
		userService.setPluginConfig(group);

		RoleWrapperSettings role = configService.getProperties(new RoleWrapperSettings());
		role.setEnabled(true);
		userService.setPluginConfig(role);

		// Make default MIME types
		final List<MimeEntry> defaultEntries = MimeMigrator.getDefaultMimeEntries();
		for( MimeEntry def : defaultEntries )
		{
			final MimeEntry m = new MimeEntry();
			m.setInstitution(institution);
			m.setDescription(def.getDescription());
			m.setExtensions(new ArrayList<String>(def.getExtensions()));
			m.setType(def.getType());
			m.setAttributes(new HashMap<String, String>(def.getAttributes()));
			mimeEntryDao.save(m);
		}
	}

	@SecureOnCallSystem
	@Override
	public Response deleteInstitution(long uniqueId)
	{
		Institution institution = institutionService.getInstitution(uniqueId);
		if( institution == null )
		{
			return Response.status(Status.NOT_FOUND).entity(uniqueId).build();
		}
		// the institutionImportService does all the preliminary deletes of
		// entities. The callback cannot be null, so we need a do-nothing
		institutionImportService.delete(institution, new ListProgressCallback());
		return Response.noContent().build();
	}

	@Override
	public SearchBean<InstitutionBean> getInstitutions()
	{
		final List<InstitutionBean> resultsOfBeans = Lists.newArrayList();

		Collection<InstitutionStatus> rawResults = institutionService.getAllInstitutions();
		for( InstitutionStatus rawStatus : rawResults )
		{
			InstitutionBean result = serialize(rawStatus.getInstitution());
			resultsOfBeans.add(result);
		}

		SearchBean<InstitutionBean> retBean = new SearchBean<InstitutionBean>();

		retBean.setStart(0);
		retBean.setLength(resultsOfBeans.size());
		retBean.setAvailable(rawResults.size());
		retBean.setResults(resultsOfBeans);

		return retBean;
	}

	@Override
	public InstitutionBean getInstitution(long uniqueId)
	{
		Institution institution = institutionService.getInstitution(uniqueId);
		if( institution == null )
		{
			Response response = Response.status(Status.NOT_FOUND).entity(uniqueId).build();
			throw new ResponseProcessingException(response, uniqueId + " not found");
		}
		InstitutionBean bean = serialize(institution);
		return bean;
	}

	@SecureOnCallSystem
	@Override
	public Response editInstitution(long uniqueId, final InstitutionBean institutionBean)
	{
		try
		{
			doEdit(uniqueId, institutionBean);
			return Response.ok().build();
		}
		catch( NotFoundException nfe )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		catch( Exception e )
		{
			throw e;
		}
	}

	@Transactional
	private void doEdit(long uniqueId, InstitutionBean bean)
	{
		Institution originalInstitution = institutionService.getInstitution(uniqueId);
		if( originalInstitution == null )
		{
			throw new NotFoundException("Institution with uniqueId " + uniqueId + " not found.");
		}

		Institution amendedInstitution = deserialize(bean, uniqueId, originalInstitution);
		institutionService.update(amendedInstitution);

		// Emits the necessary to the cluster world
		institutionService.setEnabled(uniqueId, bean.isEnabled());
	}

	private void validateEssentialFields(InstitutionBean institutionBean)
	{
		if( Check.isEmpty(institutionBean.getPassword()) )
		{
			throw new BadRequestException(VALDATION_PREAMBLE + "Password must not be left blank");
		}
		if( Check.isEmpty(institutionBean.getName()) )
		{
			throw new BadRequestException(VALDATION_PREAMBLE + "Institution name must not be left blank");
		}
		if( Check.isEmpty(institutionBean.getFilestoreId()) )
		{
			throw new BadRequestException(VALDATION_PREAMBLE + "Institution Filestore Id must not be left blank");
		}
		if( Check.isEmpty(institutionBean.getUrl()) )
		{
			throw new BadRequestException(VALDATION_PREAMBLE + "Institution URL must not be left blank");
		}
		// ensure uniqueness of specified name and filestore
		for( InstitutionStatus instati : institutionService.getAllInstitutions() )
		{
			if( Objects.equals(institutionBean.getName(), instati.getInstitution().getName()) )
			{
				KeyString messg = new KeyString(KEY_PREFIX + "institution.validate.use",
					VALDATION_PREAMBLE + "Institution name", institutionBean.getName());
				throw new BadRequestException(messg.toString());
			}
			if( Objects.equals(institutionBean.getFilestoreId(), instati.getInstitution().getFilestoreId()) )
			{
				KeyString messg = new KeyString(KEY_PREFIX + "institution.validate.use",
					VALDATION_PREAMBLE + "Filestore ID", institutionBean.getFilestoreId());
				throw new BadRequestException(messg.toString());
			}
			if( Objects.equals(institutionBean.getUrl(), instati.getInstitution().getUrl()) )
			{
				KeyString messg = new KeyString(KEY_PREFIX + "institution.validate.use",
					VALDATION_PREAMBLE + "Institution URL", institutionBean.getFilestoreId());
				throw new BadRequestException(messg.toString());
			}
		}
	}

	@Transactional
	private Institution saveNewInstitution(final InstitutionBean institutionBean, final long schemaId)
	{
		final long uniqueId = systemConfigService.createUniqueInstitutionId();
		final long resultantId = schemaDataSourceService.executeWithSchema(schemaId, new Callable<Long>()
		{
			@Override
			public Long call() throws Exception
			{
				Institution inst = deserialize(institutionBean, uniqueId, null);
				// ensure we can create the base directory for the filestore
				FileHandle handle = new InstitutionFile(inst);
				boolean createdBaseDir = false;
				if( !fileSystemService.fileExists(handle) )
				{
					fileSystemService.mkdir(handle, null);
					createdBaseDir = true;
				}
				else if( !fileSystemService.fileIsDir(handle, null) )
				{
					throw new FileAlreadyExistsException(handle.getAbsolutePath(), null, "non-directory file exists");
				}
				try
				{
					return institutionService.createInstitution(inst, schemaId).getUniqueId();
				}
				catch( Exception e )
				{
					if( createdBaseDir )
					{
						// try a cleanup if we manufactured the base directory
						try
						{
							fileSystemService.removeFile(handle);
						}
						catch( Throwable t )
						{
							// ignore an exception on delete, rethrow original
							// exception
						}
					}
					throw e;
				}
			}
		});
		final Institution institution = institutionService.getInstitution(resultantId);

		// Emits the necessary to the cluster world
		institutionService.setEnabled(institution.getUniqueId(), institutionBean.isEnabled());

		schemaDataSourceService.executeWithSchema(schemaId, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				runAs.execute(institution, CurrentUser.getUserState(), new Runnable()
				{
					@Override
					public void run()
					{
						makeDefaults(institution);
					}
				});
				return null;
			}
		});
		return institution;
	}

	private Institution deserialize(InstitutionBean bean, final long uniqueId,
		@Nullable Institution originalInstitution)
	{
		Institution newInstitution = originalInstitution != null ? originalInstitution : new Institution();
		newInstitution.setUniqueId(uniqueId);

		String adminPassword = bean.getPassword();
		if( !Check.isEmpty(adminPassword) )
		{
			newInstitution.setAdminPassword(Hash.hashPassword(adminPassword));
		}

		newInstitution.setFilestoreId(bean.getFilestoreId());
		newInstitution.setName(bean.getName());
		newInstitution.setUrl(bean.getUrl());
		newInstitution.setTimeZone(bean.getTimeZone());
		newInstitution.setEnabled(bean.isEnabled());
		return newInstitution;
	}

	private InstitutionBean serialize(Institution institution)
	{
		final InstitutionBean institutionBean = new InstitutionBean();
		institutionBean.setFilestoreId(institution.getFilestoreId());
		institutionBean.setName(institution.getName());
		institutionBean.setTimeZone(institution.getTimeZone());
		institutionBean.setUrl(institution.getUrl());
		institutionBean.setUniqueId(institution.getUniqueId());
		institutionBean.setEnabled(institution.isEnabled());
		return institutionBean;
	}

	private URI createUrl(long uniqueID)
	{
		URI uri;
		try
		{
			uri = new URI(institutionService.institutionalise("api/institution/" + uniqueID + '/'));
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
		return uri;
	}
}
