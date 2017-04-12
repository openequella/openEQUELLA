package com.tle.core.notification.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.NamedThreadFactory;
import com.tle.common.i18n.CurrentLocale;
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
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.exceptions.UserException;

@Singleton
@SuppressWarnings("nls")
@Bind(NotificationService.class)
public class NotificationServiceImpl implements NotificationService
{
	private static final int MAX_EMAIL_NOTIFICATIONS = 30;
	private static final long RETRY_MILLIS = TimeUnit.HOURS.toMillis(2);

	private static final String KEY_EMAILS_DISABLED = "notification.email.disabled";
	private static final Log LOGGER = LogFactory.getLog(NotificationService.class);

	private static String keyPrefix = PluginServiceImpl.getMyPluginId(NotificationServiceImpl.class) + ".";

	private final Executor emailerPool = Executors.newFixedThreadPool(4, new NamedThreadFactory(
		"NotificationServiceImpl.emailerPool"));
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
	private UserPreferenceService userPreferenceService;
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
		final Date notAfter = new Date(System.currentTimeMillis() - RETRY_MILLIS);
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

					while( processFirstUser(notAfter, processTime, completionService, emailCounter, attemptId, batched) )
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
			runAs.executeAsSystem(key.getInstitution(), new Callable<Void>()
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
						UserBean user = key.getUser();
						LOGGER.error("Error sending mail to " + user.getEmailAddress() + " (" + user.getUsername()
							+ ") ", error);
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
		String user = key.getUser().getUniqueID();
		String attemptId = key.getAttemptId();
		dao.markProcessed(user, key.getReasons(), attemptId);
		dao.deleteUnindexed(user, key.getDeletes(), attemptId);
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
				Callable<EmailResult<EmailKey>> emailer = runAsUser.execute(institution, user, new UserEmailer(user,
					batched, processTime, attemptId));
				if( emailer != null )
				{
					emailCounter.incrementAndGet();
					completionService.submit(emailer);
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

	public class UserEmailer implements Callable<Callable<EmailResult<EmailKey>>>
	{
		private boolean batched;
		private String user;
		private Date processDate;
		private String attemptId;

		public UserEmailer(String user, boolean batched, Date processDate, String attemptId)
		{
			this.user = user;
			this.batched = batched;
			this.processDate = processDate;
			this.attemptId = attemptId;
		}

		@Override
		public Callable<EmailResult<EmailKey>> call()
		{
			Callable<EmailResult<EmailKey>> emailer = null;
			dao.updateLastAttempt(user, batched, processDate, attemptId);
			Map<String, Integer> reasonCounts = dao.getReasonCounts(user, attemptId);
			List<String> processed = Lists.newArrayList();
			List<String> deletes = Lists.newArrayList();
			UserBean actualUser = CurrentUser.getUserState().getUserBean();
			if( !Check.isEmpty(actualUser.getEmailAddress()) )
			{
				final Set<NotificationExtension> emailers = Sets.newIdentityHashSet();
				List<String> processedReasons = Lists.newArrayList();
				List<String> deletedReasons = Lists.newArrayList();
				List<String> allowedReasons = Lists.newArrayList();
				boolean emailsEnabled = !Boolean.parseBoolean(userPreferenceService.getPreference(KEY_EMAILS_DISABLED));
				int notificationCount = 0;
				for( Entry<String, Integer> reasonCount : reasonCounts.entrySet() )
				{
					String reason = reasonCount.getKey();
					NotificationExtension extension = getExtensionForType(reason);
					boolean indexed = extension.isIndexed(reason);
					if( emailsEnabled || extension.isForceEmail(reason) )
					{
						emailers.add(extension);
						allowedReasons.add(reason);
						(indexed ? processedReasons : deletedReasons).add(reason);
						notificationCount += reasonCount.getValue();
					}
					else
					{
						(indexed ? processed : deletes).add(reason);
					}
				}
				List<Notification> notifications = dao.getNewestNotificationsForUser(MAX_EMAIL_NOTIFICATIONS, user,
					allowedReasons, attemptId);
				final StringBuilder message = new StringBuilder();
				final ListMultimap<String, Notification> typeMap = ArrayListMultimap.create();
				for( Notification notification : notifications )
				{
					typeMap.put(notification.getReason(), notification);
				}
				message.append(CurrentLocale.get(keyPrefix + "email.header", actualUser.getFirstName(),
					actualUser.getLastName(), actualUser.getUsername()));

				if( !emailers.isEmpty() )
				{
					for( NotificationExtension ext : emailers )
					{
						message.append(ext.emailText(typeMap));
					}
					if( notificationCount > notifications.size() )
					{
						message.append(CurrentLocale.get(keyPrefix + "email.more", notifications.size()));
					}

					if( emailService.hasMailSettings() )
					{
						emailer = emailService.createEmailer(CurrentLocale.get(keyPrefix + "email.subject"),
							Collections.singletonList(actualUser.getEmailAddress()), message.toString(), new EmailKey(
								actualUser, CurrentInstitution.get(), attemptId, processedReasons, deletedReasons,
								batched));
					}
				}
			}
			else
			{
				for( String reason : reasonCounts.keySet() )
				{
					NotificationExtension extension = getExtensionForType(reason);
					if( !extension.isIndexed(reason) )
					{
						deletes.add(reason);
					}
					else
					{
						processed.add(reason);
					}
				}
			}
			dao.deleteUnindexed(user, deletes, attemptId);
			dao.markProcessed(user, processed, attemptId);
			return emailer;
		}
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

	public static class EmailKey
	{
		private final UserBean user;
		private final Institution institution;
		private final String attemptId;
		private final Collection<String> reasons;
		private final Collection<String> deletes;
		private final boolean batched;

		public EmailKey(UserBean user, Institution institution, String attemptId, Collection<String> reasons,
			Collection<String> deletes, boolean batched)
		{
			this.user = user;
			this.institution = institution;
			this.attemptId = attemptId;
			this.reasons = reasons;
			this.deletes = deletes;
			this.batched = batched;
		}

		public Institution getInstitution()
		{
			return institution;
		}

		public UserBean getUser()
		{
			return user;
		}

		public Collection<String> getReasons()
		{
			return reasons;
		}

		public Collection<String> getDeletes()
		{
			return deletes;
		}

		public boolean isBatched()
		{
			return batched;
		}

		public String getAttemptId()
		{
			return attemptId;
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<NotificationExtension>(pluginService, getClass(), "notificationExtension",
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
	public boolean isEmailEnabled()
	{
		return !Boolean.parseBoolean(userPreferenceService.getPreference(KEY_EMAILS_DISABLED));
	}

	@Override
	public void setEmailEnabled(boolean enabled)
	{
		userPreferenceService.setPreference(KEY_EMAILS_DISABLED, Boolean.toString(!enabled));
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
