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

package com.tle.core.institution.impl;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.tle.core.institution.events.InstitutionEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.common.i18n.InternalI18NString;
import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionDao;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.InstitutionStatus.InvalidReason;
import com.tle.core.institution.InstitutionValidationError;
import com.tle.core.institution.InstitutionValidationException;
import com.tle.core.institution.impl.InstitutionMessage.CreateInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.DeleteInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.EditInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.InstitutionMessageResponse;
import com.tle.core.institution.impl.InstitutionMessage.SchemaMessage;
import com.tle.core.institution.impl.InstitutionMessage.SetEnabledMessage;
import com.tle.core.institution.impl.InstitutionMessage.ValidateInstitutionMessage;
import com.tle.core.migration.MigrationService;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.services.UrlService;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.SimpleMessage;
import com.tle.core.services.impl.TransientListStatusChange;
import com.tle.core.system.SystemConfigService;
import com.tle.core.system.service.SchemaDataSourceService;

@Bind
@SuppressWarnings("nls")
public class InstitutionGlobalTask extends AlwaysRunningTask<SimpleMessage>
{
	public static final String KEY_EVENTS = "events";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_FILESTORE = "filestoreId";
	private static final String FIELD_URL = "url";
	private static final String FIELD_QUOTA = "quota";

	public static final String KEY_STATUSES = "onlineInsts";
	private static final Log LOGGER = LogFactory.getLog(InstitutionGlobalTask.class);

	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(InstitutionGlobalTask.class) + '.';
	@Inject
	private MigrationService migrationService;
	@Inject
	private SchemaDataSourceService dataSourceService;
	@Inject
	private InstitutionDao institutionDao;
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private UrlService urlService;

	private final Map<Long, InstitutionStatus> institutionMap = Maps.newHashMap();
	private final Set<Long> knownSchemas = Sets.newHashSet();

	@Override
	public void init()
	{
		Collection<Long> schemaIds = migrationService.getAvailableSchemaIds();
		if( !schemaIds.isEmpty() )
		{
			checkSchemas(schemaIds);
		}
		publishInstitutions();
	}

	@Override
	protected SimpleMessage waitFor() throws InterruptedException
	{
		return waitForMessage();
	}

	@Override
	public void runTask(SimpleMessage msg)
	{
		InstitutionMessageResponse response = new InstitutionMessageResponse();
		boolean updateInsts = true;
		if( msg != null )
		{
			InstitutionMessage instMsg = msg.getContents();
			try
			{
				switch( instMsg.getType() )
				{
					case VALIDATE:
						updateInsts = false;
						processValidate((ValidateInstitutionMessage) instMsg, response);
						break;
					case SCHEMAS:
						processSchemas((SchemaMessage) instMsg);
						break;
					case SETENABLED:
						processSetEnabled((SetEnabledMessage) instMsg);
						break;
					case EDIT:
						processEdit((EditInstitutionMessage) instMsg);
						break;
					case DELETE:
						processDelete((DeleteInstitutionMessage) instMsg);
						break;
					case CREATE:
						processCreate((CreateInstitutionMessage) instMsg, response);
						break;
				}
			}
			catch( Exception t )
			{
				LOGGER.error("Error processing message", t);
				response.setError(t);
			}
		}

		if( updateInsts )
		{
			recheckInstitutions();
			response.setInstitutionMap(institutionMap);
			publishInstitutions();
		}

		String msgId = msg != null ? msg.getMessageId() : null;
		if( msgId != null )
		{
			sendResponse(msgId, response);
		}
	}

	private void recheckInstitutions()
	{
		Collection<Long> allIds = Sets.newHashSet(institutionMap.keySet());
		for( Long instId : allIds )
		{
			InstitutionStatus status = removeInstitution(instId);
			status = createStatus(status.getInstitution(), status.getSchemaId());
			addInstitution(status);
		}
	}

	private void processSchemas(SchemaMessage schMsg)
	{
		Collection<Long> schemaIds = schMsg.getSchemaIds();
		if( schMsg.isAvailable() )
		{
			checkSchemas(schemaIds);
		}
		else
		{
			removeSchemas(schemaIds);
		}
	}

	private void publishInstitutions()
	{
		setSubTaskStatus(KEY_STATUSES, (Serializable) institutionMap);
		publishStatus();
	}

	private void addInstitution(InstitutionStatus status)
	{
		Institution institution = status.getInstitution();
		institutionMap.put(institution.getUniqueId(), status);
	}

	private InstitutionStatus removeInstitution(long instId)
	{
		return institutionMap.remove(instId);
	}

	private Institution processCreate(CreateInstitutionMessage msg, InstitutionMessageResponse response)
	{
		long schemaId = msg.getSchemaId();
		final Institution institution = msg.getInstitution();
		Institution newInst = dataSourceService.executeWithSchema(schemaId, new Callable<Institution>()
		{
			@Override
			public Institution call()
			{
				return doCreate(institution);
			}
		});
		addInstitution(createStatus(institution, schemaId));
		response.setInstitution(institution);
		return newInst;
	}

	private InstitutionStatus createStatus(Institution institution, long schemaId)
	{
		InvalidReason reason = null;
		for( InstitutionStatus status : institutionMap.values() )
		{
			if( status.isValid() )
			{
				Institution inst = status.getInstitution();
				if( inst.getUniqueId() != institution.getUniqueId() && containsDuplicate(institution, inst) )
				{
					reason = InvalidReason.INVALID;
					break;
				}
			}
		}
		return new InstitutionStatus(institution, schemaId, reason);
	}

	private boolean containsDuplicate(Institution institution, Institution existing)
	{
		return existing.getName().equals(institution.getName()) || existing.getUrl().equals(institution.getUrl())
				|| existing.getFilestoreId().equals(institution.getFilestoreId());
	}

	private void processDelete(DeleteInstitutionMessage msg)
	{
		final Institution institution = msg.getInstitution();
		long uniqueId = institution.getUniqueId();
		InstitutionStatus status = institutionMap.get(uniqueId);
		long schemaId = status.getSchemaId();
		dataSourceService.executeWithSchema(schemaId, new Callable<Void>()
		{
			@Override
			public Void call()
			{
				doDelete(institution);
				return null;
			}
		});
		removeInstitution(status.getInstitution().getUniqueId());
		TransientListStatusChange changer = new TransientListStatusChange(KEY_EVENTS);
		changer.add(new InstitutionEvent(InstitutionEvent.InstitutionEventType.DELETED, ImmutableMultimap.of(schemaId, institution)));
		mergeChanges(changer);
	}

	private void processEdit(EditInstitutionMessage msg)
	{
		final Institution institution = msg.getInstitution();
		InstitutionStatus oldStatus = removeInstitution(msg.getInstitution().getUniqueId());
		long schemaId = oldStatus.getSchemaId();
		Institution edited = dataSourceService.executeWithSchema(schemaId, new Callable<Institution>()
		{
			@Override
			public Institution call()
			{
				return doUpdate(institution);
			}
		});
		updateSingleInst(schemaId, edited);
	}

	private void processSetEnabled(SetEnabledMessage msg)
	{
		final long institutionId = msg.getInstitutionId();
		final boolean enabled = msg.isEnabled();
		InstitutionStatus oldStatus = removeInstitution(institutionId);
		long schemaId = oldStatus.getSchemaId();
		Institution institution = dataSourceService.executeWithSchema(schemaId, new Callable<Institution>()
		{
			@Override
			public Institution call()
			{
				return doSetEnabled(institutionId, enabled);
			}
		});
		updateSingleInst(schemaId, institution);
	}

	private void updateSingleInst(long schemaId, Institution institution)
	{
		removeInstitution(institution.getUniqueId());
		addInstitution(createStatus(institution, schemaId));
		TransientListStatusChange changer = new TransientListStatusChange(KEY_EVENTS);
		changer.add(new InstitutionEvent(InstitutionEvent.InstitutionEventType.EDITED, ImmutableMultimap.of(schemaId, institution)));
		mergeChanges(changer);
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.core.institution.institutionglobaltask";
	}

	@Transactional
	protected Institution doCreate(Institution institution)
	{
		institution.setDatabaseId(0);
		institution.setUniqueId(createUniqueId());
		institution.setEnabled(false);
		fixUrlEnding(institution);
		List<InstitutionValidationError> errors = validate(institution);
		if( !errors.isEmpty() )
		{
			throw new InstitutionValidationException(errors);
		}
		institutionDao.save(institution);
		return institution;
	}

	private void fixUrlEnding(Institution institution)
	{
		String url = institution.getUrl();
		if( !url.endsWith("/") )
		{
			institution.setUrl(url + '/');
		}
	}

	@Transactional
	protected void doDelete(Institution institution)
	{
		institutionDao.delete(institution);
	}

	@Transactional
	protected Institution doUpdate(Institution institution)
	{
		Institution current = institutionDao.findByUniqueId(institution.getUniqueId());
		if( !Check.isEmpty(institution.getName()) )
		{
			current.setName(institution.getName());
		}

		if( !Check.isEmpty(institution.getUrl()) )
		{
			fixUrlEnding(institution);
			current.setUrl(institution.getUrl());
		}

		if( !Check.isEmpty(institution.getAdminPassword()) )
		{
			current.setAdminPassword(Hash.hashPassword(institution.getAdminPassword()));
		}
		current.setQuota(institution.getQuota());

		current.setFilestoreId(institution.getFilestoreId());
		current.setTimeZone(institution.getTimeZone());
		List<InstitutionValidationError> errors = validate(current);
		if( !errors.isEmpty() )
		{
			throw new InstitutionValidationException(errors);
		}
		return current;
	}

	@Transactional
	protected Institution doSetEnabled(long instId, boolean b)
	{
		Institution inst = institutionDao.findByUniqueId(instId);
		inst.setEnabled(b);
		institutionDao.save(inst);
		return inst;
	}

	@Transactional
	protected List<Institution> sanitizeInstitutions()
	{
		List<Institution> institutions = institutionDao.findAllByCriteria();
		for( Institution institution : institutions )
		{
			long id = institution.getUniqueId();
			if( institutionMap.containsKey(id) )
			{
				institution.setUniqueId(createUniqueId());
			}
		}
		return institutions;
	}

	private long createUniqueId()
	{
		long id;
		do
		{
			id = systemConfigService.createUniqueInstitutionId();
		}
		while( institutionMap.containsKey(id) );
		return id;
	}

	public void checkSchemas(Collection<Long> schemas)
	{
		for( final Long schemaId : schemas )
		{
			if( !knownSchemas.contains(schemaId) )
			{
				knownSchemas.add(schemaId);
				dataSourceService.executeWithSchema(schemaId, new Callable<Void>()
				{
					@Override
					public Void call()
					{
						List<Institution> institutions = sanitizeInstitutions();
						for( Institution inst : institutions )
						{
							addInstitution(createStatus(inst, schemaId));
						}
						return null;
					}
				});
			}
		}
	}

	public void removeSchemas(Collection<Long> schemas)
	{
		Set<Long> toRemove = Sets.newHashSet(schemas);
		Iterator<InstitutionStatus> iter = institutionMap.values().iterator();
		while( iter.hasNext() )
		{
			InstitutionStatus status = iter.next();
			if( toRemove.contains(status.getSchemaId()) )
			{
				iter.remove();
			}
		}
		knownSchemas.removeAll(schemas);
	}

	private void processValidate(ValidateInstitutionMessage valMsg, InstitutionMessageResponse response)
	{
		Institution institution = valMsg.getInstitution();
		fixUrlEnding(institution);
		List<InstitutionValidationError> errors = validate(institution);
		response.setValidationErrors(errors);
	}

	private void validateField(List<InstitutionValidationError> errors, String id, String displayKey, String value,
							   int maxLength, boolean ensureLettersOrDigits)
	{
		if( Check.isEmpty(value) )
		{
			errors.add(new InstitutionValidationError(id, validateString("blank", validateString(displayKey))));
		}
		else if( value.length() > maxLength )
		{
			errors
					.add(new InstitutionValidationError(id, validateString("long", validateString(displayKey), maxLength)));
		}
		else if( ensureLettersOrDigits && !value.matches("^[A-Za-z0-9]+$") )
		{
			errors.add(new InstitutionValidationError(id, validateString("letters", validateString(displayKey))));
		}
	}

	private List<InstitutionValidationError> validate(Institution institution)
	{
		final List<InstitutionValidationError> errors = Lists.newArrayList();
		final String filestoreId = institution.getFilestoreId();
		final String url = institution.getUrl();
		final String name = institution.getName();
		final double quota = institution.getQuota();

		URI uri = null;
		try
		{
			uri = new URI(url);
			String hostname = uri.getHost();
			if( hostname == null )
			{
				uri = null;
				errors.add(new InstitutionValidationError(FIELD_URL, validateString("url.invalidhost")));
			}
		}
		catch( URISyntaxException urie )
		{
			errors.add(new InstitutionValidationError(FIELD_URL, validateString("url.valid", url)));
		}

		for( InstitutionStatus status : institutionMap.values() )
		{
			final Institution existing = status.getInstitution();
			if( existing.getUniqueId() == institution.getUniqueId() )
			{
				continue;
			}

			if( existing.getName().equals(name) )
			{
				errors.add(new InstitutionValidationError(FIELD_NAME, inUseString("display", name)));
			}

			if( existing.getUrl().equals(url) )
			{
				errors.add(new InstitutionValidationError(FIELD_URL, inUseString(FIELD_URL, url)));
			}

			if( existing.getFilestoreId().equals(filestoreId) )
			{
				errors.add(new InstitutionValidationError(FIELD_FILESTORE, inUseString("filestore", filestoreId)));
			}

			if( uri != null )
			{
				final URI otherUri = existing.getUrlAsUri();

				if( uri.getHost().equals(otherUri.getHost()) && uri.getPort() == otherUri.getPort() )
				{
					final String path = uri.getPath();
					final String otherPath = otherUri.getPath();

					String msg = null;
					if( otherPath.startsWith(path) )
					{
						msg = "overwrite";
					}
					else if( path.startsWith(otherPath) )
					{
						msg = "inside";
					}

					if( msg != null )
					{
						errors.add(new InstitutionValidationError(FIELD_URL,
								validateString("url." + msg, otherUri.toString())));
					}
				}
			}
			if( quota < 0 )
			{
				errors.add(new InstitutionValidationError(FIELD_QUOTA, validateString("quota.notanumber")));
			}
		}

		if( url.equalsIgnoreCase(urlService.getAdminUrl().toString()) )
		{
			errors.add(new InstitutionValidationError(FIELD_URL, validateString("url.use", url)));
		}

		validateField(errors, FIELD_NAME, "display", name, 100, false);
		validateField(errors, FIELD_FILESTORE, "filestore", filestoreId, 20, true);
		validateField(errors, FIELD_URL, "url", url, 100, false);

		return errors;
	}

	private InternalI18NString validateString(String postfix, Object... vals)
	{
		return new KeyString(KEY_PREFIX + "institution.validate." + postfix, vals);
	}

	private InternalI18NString inUseString(String field, String value)
	{
		return validateString("use", validateString(field), value);
	}
}
