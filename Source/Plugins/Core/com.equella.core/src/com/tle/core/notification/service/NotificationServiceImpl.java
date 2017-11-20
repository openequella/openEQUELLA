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

package com.tle.core.notification.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.notification.EmailKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.NamedThreadFactory;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.notification.NotificationExtension;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.dao.NotificationDao;
import com.tle.core.notification.dao.NotifiedUser;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.security.RunAsUser;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.exceptions.UserException;

@Singleton
@SuppressWarnings("nls")
@Bind(NotificationService.class)
public class NotificationServiceImpl implements NotificationService
{
	private static final int MAX_EMAIL_NOTIFICATIONS = 30;
	private static final long RETRY_MILLIS = TimeUnit.HOURS.toMillis(2);

	private static final Log LOGGER = LogFactory.getLog(NotificationService.class);

	private static String keyPrefix = PluginServiceImpl.getMyPluginId(NotificationServiceImpl.class) + ".";

	private final Executor emailerPool = Executors.newFixedThreadPool(4,
		new NamedThreadFactory("NotificationServiceImpl.emailerPool"));
	private final Executor backgroundProcess = Executors.newSingleThreadExecutor();

	@Inject
	private RunAsInstitution runAs;
	@Inject
	private RunAsUser runAsUser;
	@Inject
	private NotificationDao dao;
	@Inject
	private EmailService emailService;
	@Inject
	private TaskService taskService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SchemaDataSourceService schemaDataSourceService;

	private PluginTracker<NotificationExtension> extensionTracker;

	@Override
	@Transactional
	public void addNotification(ItemKey itemId, String reason, String userTo, boolean batched)
	{
		addNotification(itemId.toString(), ItemId.fromKey(itemId).toString(), reason, userTo, batched);
	}

	@Override
	@Transactional
	public void addNotification(String uuid, String itemOnlyId, String reason, String userTo, boolean batched)
	{
		if( Check.isEmpty(userTo) )
		{
			return;
		}
		Notification notification = dao.getExistingNotification(uuid, reason, userTo);
		if( notification == null )
		{
			notification = new Notification();
			notification.setInstitution(CurrentInstitution.get());
			notification.setItemid(uuid);
			notification.setItemidOnly(itemOnlyId);
			notification.setReason(reason);
			notification.setUserTo(userTo);
		}
		notification.setProcessed(false);
		notification.setBatched(batched);
		notification.setDate(new Date());
		try
		{
			dao.save(notification);
		}
		catch( ConstraintViolationException ex )
		{
			// Safely ignore - two notifications were trying to be created at
			// the same time for the same user, reason, item and institution.
		}
	}

	@Override
	@Transactional
	public void addNotifications(ItemKey itemId, String reason, Collection<String> users, boolean batched)
	{
		for( String user : users )
		{
			addNotification(itemId, reason, user, batched);
		}
	}

	@Override
	@Transactional
	public void addNotifications(String uuid, String reason, Collection<String> userUuids, boolean batched)
	{
		for( String userUuid : userUuids )
		{
			addNotification(uuid, uuid, reason, userUuid, batched);
		}
	}

	@Override
	@Transactional
	public void removeNotification(long notificationId)
	{
		Notification notification = dao.findById(notificationId);
		if( notification != null )
		{
			if( !notification.getUserTo().equals(CurrentUser.getUserID()) )
			{
				throw new AccessDeniedException("Only owner of notification can remove");
			}
			dao.delete(notification);
		}
		else
		{
			throw new NotFoundException("Notification " + notificationId + " doesn't exist");
		}
	}

	@Override
	@Transactional
	public Notification getNotification(long notificationId)
	{
		Notification notification = dao.findById(notificationId);
		if( notification != null )
		{
			if( !notification.getUserTo().equals(CurrentUser.getUserID()) )
			{
				throw new AccessDeniedException("Only owner can access this notification");
			}
		}
		return notification;
	}

	public void emailTask(final boolean batched)
	{
		final Date notBefore = new Date(System.currentTimeMillis() - RETRY_MILLIS);
		final Date processTime = new Date();
		final String attemptId = UUID.randomUUID().toString();
		final ExecutorCompletionService<EmailResult<EmailKey>> completionService = new ExecutorCompletionService<EmailResult<EmailKey>>(
			emailerPool);
		final AtomicInteger emailCounter = new AtomicInteger();
		Multimap<Long, Institution> availableInsts = institutionService.getAvailableMap();
		for( Long schemaId : availableInsts.keySet() )
		{
			schemaDataSourceService.executeWithSchema(schemaId, new Callable<Void>()
			{
				@Override
				public Void call()
				{

					while( processFirstUser(notBefore, processTime, completionService, emailCounter, attemptId,
						batched) )
					{
						Future<EmailResult<EmailKey>> result;
						while( (result = completionService.poll()) != null )
						{
							processResult(result, emailCounter);
						}
					}
					return null;
				}
			});
		}
		try
		{
			while( emailCounter.intValue() > 0 )
			{
				processResult(completionService.take(), emailCounter);
			}
		}
		catch( InterruptedException e )
		{
			LOGGER.error("Error waiting for emails");
		}
	}

	protected void processResult(final Future<EmailResult<EmailKey>> result, AtomicInteger emailCounter)
	{
		emailCounter.decrementAndGet();
		try
		{
			final EmailResult<EmailKey> emailResult = result.get();
			final EmailKey key = emailResult.getKey();
			runAs.executeAsSystem(key.institution(), new Callable<Void>()
			{
				@Override
				public Void call()
				{
					Throwable error = emailResult.getError();
					if( error == null )
					{
						markProcessed(key);
					}
					else
					{
						UserBean user = key.user();
						LOGGER.error(
							"Error sending mail to " + user.getEmailAddress() + " (" + user.getUsername() + ") ",
							error);
					}
					return null;
				}
			});
		}
		catch( Exception e )
		{
			LOGGER.error("Error getting emailer status", e);
		}
	}

	@Transactional
	protected void markProcessed(EmailKey key)
	{
		key.successCallback();
	}

	@Transactional
	protected boolean processFirstUser(Date notAfter, final Date processTime,
		ExecutorCompletionService<EmailResult<EmailKey>> completionService, AtomicInteger emailCounter,
		final String attemptId, final boolean batched)
	{
		NotifiedUser userToNotify = dao.getUserToNotify(notAfter, attemptId, batched);
		if( userToNotify != null )
		{
			final String user = userToNotify.getUser();
			Institution institution = institutionService.getInstitution(userToNotify.getInstId());
			try
			{
				Iterable<Callable<EmailResult<EmailKey>>> emailer = runAsUser.execute(institution, user,
					new NotificationEmailer(batched, processTime, attemptId, dao, emailService));
				for (Callable<EmailResult<EmailKey>> em : emailer)
				{
					emailCounter.incrementAndGet();
					completionService.submit(em);
				}
			}
			catch( UserException ue )
			{
				runAs.executeAsSystem(institution, new Runnable()
				{
					@Override
					public void run()
					{
						dao.updateLastAttempt(user, batched, processTime, attemptId);
						Map<String, Integer> reasonCounts = dao.getReasonCounts(user, attemptId);
						List<String> processed = Lists.newArrayList();
						List<String> deletes = Lists.newArrayList();
						for( Entry<String, Integer> reasonCount : reasonCounts.entrySet() )
						{
							String reason = reasonCount.getKey();
							(getExtensionForType(reason).isIndexed(reason) ? processed : deletes).add(reason);
						}
						dao.deleteUnindexed(user, deletes, attemptId);
						dao.markProcessed(user, processed, attemptId);
					}
				});
			}

		}
		return userToNotify != null;
	}

	@Override
	public void processEmails()
	{
		backgroundProcess.execute(new Runnable()
		{
			@Override
			public void run()
			{
				taskService.getGlobalTask(getClusteredTask(false), TimeUnit.MINUTES.toMillis(1));
			}
		});
	}

	@Override
	public ClusteredTask getClusteredTask(boolean batched)
	{
		return new BeanClusteredTask(NotificationService.class.getName() + '-' + batched, NotificationService.class,
			"emailTask", batched);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<NotificationExtension>(pluginService, "com.tle.core.notification", "notificationExtension",
			"type").setBeanKey("bean");
	}

	@Override
	public NotificationExtension getExtensionForType(String type)
	{
		Extension extension = extensionTracker.getExtension(type);
		if( extension == null )
		{
			return null;
		}
		return extensionTracker.getBeanByExtension(extension);
	}

	@Override
	@Transactional
	public int removeAllForItem(ItemId itemId, String reason)
	{
		return dao.deleteAllForItemReasons(itemId, Collections.singleton(reason));
	}

	@Override
	@Transactional
	public int removeAllForKey(ItemKey itemKey, String reason)
	{
		return dao.deleteAllForKeyReasons(itemKey, Collections.singleton(reason));
	}

	@Override
	@Transactional
	public int removeForUserAndKey(ItemKey itemKey, String userId, String reason)
	{
		return dao.deleteAllForUserKeyReasons(itemKey, userId, Collections.singleton(reason));
	}

	@Override
	public Collection<String> getNotificationTypes()
	{
		return extensionTracker.getExtensionMap().keySet();
	}

	@Override
	public int removeAllForItem(ItemId itemId, Collection<String> reasons)
	{
		return dao.deleteAllForItemReasons(itemId, reasons);
	}

	@Override
	public int removeAllForUuid(String uuid, String reason)
	{
		return dao.deleteAllForUuidReasons(uuid, Collections.singleton(reason));
	}

	@Override
	public int removeAllForUuid(String uuid, Collection<String> reasons)
	{
		return dao.deleteAllForUuidReasons(uuid, reasons);
	}

	@Override
	public int removeAllForKey(ItemKey itemKey, Collection<String> reasons)
	{
		return dao.deleteAllForKeyReasons(itemKey, reasons);
	}

	@Override
	public int removeForUserAndKey(ItemKey itemKey, String userId, Collection<String> reasons)
	{
		return dao.deleteAllForUserKeyReasons(itemKey, userId, reasons);
	}

	@Override
	public boolean userIdChanged(ItemKey itemKey, String fromUserId, String toUserId)
	{
		return dao.userIdChanged(itemKey, fromUserId, toUserId);
	}
}
