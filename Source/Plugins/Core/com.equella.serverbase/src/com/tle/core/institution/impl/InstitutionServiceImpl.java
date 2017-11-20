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

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Provider;
import com.tle.beans.DatabaseSchema;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.InstitutionValidationError;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.InstitutionEvent.InstitutionEventType;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.institution.impl.InstitutionMessage.CreateInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.DeleteInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.EditInstitutionMessage;
import com.tle.core.institution.impl.InstitutionMessage.InstitutionMessageResponse;
import com.tle.core.institution.impl.InstitutionMessage.SchemaMessage;
import com.tle.core.institution.impl.InstitutionMessage.SetEnabledMessage;
import com.tle.core.institution.impl.InstitutionMessage.ValidateInstitutionMessage;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.migration.SchemaInfoImpl;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.TaskStatusListener;
import com.tle.core.services.UrlService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SimpleMessage;
import com.tle.core.services.impl.Task;
import com.tle.core.system.dao.DatabaseSchemaDao;

@Bind(InstitutionService.class)
@Singleton
@SuppressWarnings("nls")
public class InstitutionServiceImpl
		implements
		InstitutionService,
		InstitutionListener,
		SchemaListener,
		TaskStatusListener
{
	private static final long DEFAULT_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	@Inject
	private EventService eventService;
	@Inject
	private TaskService taskService;
	@Inject
	private DatabaseSchemaDao databaseSchemaDao;
	@Inject
	private UrlService urlService;

	/**
	 * Local, non-replicated caches that are institution aware.
	 */
	private final List<WeakReference<InstitutionCache<?>>> institutionAwareCaches = Lists.newLinkedList();

	private Map<Long, InstitutionStatus> allInstitutions = Collections.emptyMap();
	private Multimap<Long, Institution> availableInstitutions = ImmutableMultimap.of();
	private Map<Long, Long> schemaMap = Collections.emptyMap();
	private Map<Long, Institution> instMap = Collections.emptyMap();

	@Inject
	private Provider<InstitutionGlobalTask> taskProvider;
	private InstitutionGlobalTask localTask;

	private String institutionTaskId;

	public Task createTask()
	{
		localTask = taskProvider.get();
		return localTask;
	}

	@Override
	public void taskStatusChanged(String taskId, TaskStatus taskStatus)
	{
		Map<Long, InstitutionStatus> statuses = taskStatus.getTaskSubStatus(InstitutionGlobalTask.KEY_STATUSES);
		if( statuses != null )
		{
			setAllInstitutions(statuses);
			List<InstitutionEvent> events = taskStatus.consumeTransient(InstitutionGlobalTask.KEY_EVENTS);
			if( events != null )
			{
				for( InstitutionEvent institutionEvent : events )
				{
					eventService.publishApplicationEvent(institutionEvent);
				}
			}
		}
	}

	@Override
	public Multimap<Long, Institution> getAvailableMap()
	{
		return availableInstitutions;
	}

	@Override
	public Collection<Institution> enumerateAvailable()
	{
		return availableInstitutions.values();
	}

	@Override
	public InstitutionStatus getInstitutionStatus(long institutionId)
	{
		return allInstitutions.get(institutionId);
	}

	@Override
	public Collection<InstitutionStatus> getAllInstitutions()
	{
		return allInstitutions.values();
	}

	@Override
	public Institution getInstitution(long institutionId)
	{
		return instMap.get(institutionId);
	}

	@Override
	@SecureOnCallSystem
	public void setEnabled(final long instId, final boolean b)
	{
		sendTaskMessage(new SetEnabledMessage(instId, b));
	}

	@Override
	public long getSchemaIdForInstitution(Institution institution)
	{
		Long schemaId = schemaMap.get(institution.getUniqueId());
		if( schemaId == null )
		{
			throw new IllegalArgumentException(
					"Institution with id " + institution.getUniqueId() + " has no schema entry");
		}
		return schemaId;
	}

	@Override
	public List<InstitutionValidationError> validate(Institution institution)
	{
		InstitutionMessageResponse response = sendTaskMessage(new ValidateInstitutionMessage(institution));
		return response.getValidationErrors();
	}

	@Override
	public void update(final Institution institution)
	{
		sendTaskMessage(new EditInstitutionMessage(institution));
	}

	@Override
	public void deleteInstitution(final Institution institution)
	{
		sendTaskMessage(new DeleteInstitutionMessage(institution));
	}

	@Override
	@SecureOnCallSystem
	public Institution createInstitution(final Institution institution, long schemaId)
	{
		InstitutionMessageResponse response = sendTaskMessage(new CreateInstitutionMessage(institution, schemaId));
		return response.getInstitution();
	}

	private InstitutionMessageResponse sendTaskMessage(InstitutionMessage message)
	{
		InstitutionMessageResponse response = taskService.postSynchronousMessage(institutionTaskId, message,
				DEFAULT_TIMEOUT);

		Map<Long, InstitutionStatus> map = response.getInstitutionMap();
		if( map != null )
		{
			setAllInstitutions(map);
		}
		Throwable error = response.getError();
		if( error != null )
		{
			Throwables.propagate(error);
		}
		return response;
	}

	private synchronized void setAllInstitutions(Map<Long, InstitutionStatus> map)
	{
		allInstitutions = ImmutableMap.copyOf(map);

		Multimap<Long, Institution> newlyAvailable = ArrayListMultimap.create();
		Multimap<Long, Institution> newlyUnavailable = ArrayListMultimap.create(availableInstitutions);
		Builder<Long, Institution> availableBuilder = ImmutableMultimap.builder();
		ImmutableMap.Builder<Long, Long> schemaBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Long, Institution> instBuilder = ImmutableMap.builder();
		for( Entry<Long, InstitutionStatus> entry : allInstitutions.entrySet() )
		{
			InstitutionStatus instStatus = entry.getValue();
			Institution institution = instStatus.getInstitution();
			Long schemaId = instStatus.getSchemaId();
			if( instStatus.isValid() && institution.isEnabled() )
			{
				availableBuilder.put(schemaId, institution);
				if( !newlyUnavailable.remove(schemaId, institution) )
				{
					newlyAvailable.put(schemaId, institution);
				}
			}
			long uniqueId = institution.getUniqueId();
			schemaBuilder.put(uniqueId, schemaId);
			instBuilder.put(uniqueId, institution);
		}

		availableInstitutions = availableBuilder.build();
		schemaMap = schemaBuilder.build();
		instMap = instBuilder.build();

		if( !newlyAvailable.isEmpty() )
		{
			eventService.publishApplicationEvent(new InstitutionEvent(InstitutionEventType.AVAILABLE, newlyAvailable));
		}

		if( !newlyUnavailable.isEmpty() )
		{
			eventService
					.publishApplicationEvent(new InstitutionEvent(InstitutionEventType.UNAVAILABLE, newlyUnavailable));
		}
	}

	@Override
	public void schemasAvailable(Collection<Long> schemas)
	{
		if( localTask != null )
		{
			localTask.postMessage(new SimpleMessage(null, new SchemaMessage(schemas, true)));
		}
	}

	@Override
	public void schemasUnavailable(Collection<Long> schemas)
	{
		if( localTask != null )
		{
			localTask.postMessage(new SimpleMessage(null, new SchemaMessage(schemas, false)));
		}
	}

	@Override
	public void systemSchemaUp()
	{
		institutionTaskId = taskService
				.getGlobalTask(new BeanClusteredTask("InstitutionKeeper", true, InstitutionService.class, "createTask"),
						DEFAULT_TIMEOUT)
				.getTaskId();
		taskService.addTaskStatusListener(institutionTaskId, this);
		TaskStatus status = taskService.getTaskStatus(institutionTaskId);
		if( status != null )
		{
			taskStatusChanged(null, status);
		}
	}

	@Override
	public boolean canAddInstitution()
	{
		// FIXME check licence
		return true;
	}

	@Override
	public <T> InstitutionCache<T> newInstitutionAwareCache(CacheLoader<Institution, T> loader)
	{
		synchronized( institutionAwareCaches )
		{
			InstitutionCache<T> ic = new InstitutionCacheImpl<>(loader);
			institutionAwareCaches.add(new WeakReference<InstitutionCache<?>>(ic));
			return ic;
		}
	}

	@Override
	public void institutionEvent(InstitutionEvent event)
	{
		switch( event.getEventType() )
		{
			case UNAVAILABLE:
			case DELETED:
				for( final Institution inst : event.getChanges().values() )
				{
					synchronized( institutionAwareCaches )
					{
						for( Iterator<WeakReference<InstitutionCache<?>>> iter = institutionAwareCaches.iterator(); iter
								.hasNext(); )
						{
							InstitutionCache<?> ic = iter.next().get();
							if( ic == null )
							{
								iter.remove();
							}
							else
							{
								ic.clear(inst);
							}
						}
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public List<SchemaInfo> getAllSchemaInfos()
	{
		List<SchemaInfo> infos = Lists.newArrayList();
		Collection<DatabaseSchema> allSchema = databaseSchemaDao.enumerate();
		for( DatabaseSchema databaseSchema : allSchema )
		{
			infos.add(new SchemaInfoImpl(databaseSchema).immutable());
		}
		return infos;
	}

	@Override
	public URL getInstitutionUrl()
	{
		return getInstitutionUrl(CurrentInstitution.get());
	}

	@Override
	public URL getInstitutionUrl(Institution institution)
	{
		if( institution == null )
		{
			return urlService.getAdminUrl();
		}
		return institution.getUrlAsUrl();
	}

	@Override
	public String institutionalise(String url)
	{
		String result;
		try
		{
			result = new URL(getInstitutionUrl(), url).toString();
		}
		catch( MalformedURLException e )
		{
			throw malformedUrl(e, url);
		}
		return result;
	}

	@Override
	public URI getInstitutionUri()
	{
		try
		{
			return getInstitutionUrl().toURI();
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean isInstitutionUrl(String url)
	{
		try
		{
			URL iUrl = getInstitutionUrl();
			URL myUrl = new URL(url);
			int myPort = myUrl.getPort();
			if( myPort == -1 )
			{
				myPort = myUrl.getDefaultPort();
			}
			int iPort = iUrl.getPort();
			if( iPort == -1 )
			{
				iPort = iUrl.getDefaultPort();
			}
			return (iUrl.getHost().equals(myUrl.getHost()) && (myPort == iPort)
					&& myUrl.getPath().startsWith(iUrl.getPath()));
		}
		catch( MalformedURLException e )
		{
			return false;
		}
	}

	@Override
	public String removeInstitution(String url)
	{
		try
		{
			URL iUrl = getInstitutionUrl();
			URL myUrl = new URL(url);
			String myRef = myUrl.getRef(); // anchor e.g. #post1
			String myUrlNoHost = myUrl.getFile() + (myRef == null ? "" : "#" + myRef);
			return myUrlNoHost.substring(iUrl.getPath().length());
		}
		catch( MalformedURLException ex )
		{
			throw malformedUrl(ex, url);
		}
	}

	private RuntimeException malformedUrl(Throwable ex, Object... bits)
	{
		StringBuilder msg = new StringBuilder("Error creating URL");
		for( Object bit : bits )
		{
			if( bit != null )
			{
				msg.append(", ");
				msg.append(bit.toString());
			}
		}

		return new RuntimeException(msg.toString(), ex);
	}
}
