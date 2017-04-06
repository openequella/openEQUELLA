package com.tle.web.workflow.notification;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.AutoAction;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.user.LazyUserLookup;
import com.tle.core.services.user.UserService;
import com.tle.web.i18n.BundleCache;
import com.tle.web.notification.WebNotificationExtension;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.result.util.UserLabel;
import com.tle.web.workflow.tasks.RootTaskListSection;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TaskNotifications implements WebNotificationExtension
{
	@PlugKey("notereason.")
	private static String KEY_REASON_FILTER;
	@PlugKey("notificationlist.reasons.")
	private static String KEY_REASON_LIST;
	@PlugKey("email.automsg")
	private static String KEY_AUTOMSG;
	@PlugKey("email.autodays")
	private static String KEY_AUTODAYS;
	@PlugKey("email.autoreject")
	private static Label LABEL_AUTOREJECT;
	@PlugKey("email.autoaccept")
	private static Label LABEL_AUTOACCEPT;
	@PlugKey("email.rejecttask")
	private static Label LABEL_CAUSEREJECTED;
	@PlugKey("email.accepttask")
	private static Label LABEL_CAUSEACCEPTED;
	@PlugKey("email.msg.")
	private static String KEY_MESSAGELABEL;

	@Inject
	private BundleCache bundleCache;

	@Inject
	private SectionsController controller;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private ItemService itemService;
	@Inject
	private UserService userService;
	@Inject
	private ExtendedFreemarkerFactory viewFactory;

	@Override
	public boolean isIndexed(String type)
	{
		return !type.equals(Notification.REASON_MODERATE);
	}

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		LazyUserLookup lazyUserLookup = new LazyUserLookup(userService);
		List<TaskNotification> allModerations = createTaskNotifications(typeMap.get(Notification.REASON_MODERATE),
			lazyUserLookup);

		ListMultimap<String, TaskNotification> moderationTypes = ArrayListMultimap.create();
		for( TaskNotification notification : allModerations )
		{
			String group = "other";
			if( notification.getCauseLabel() != null )
			{
				if( notification.isCauseAccepted() )
				{
					group = "accepted";
				}
				else
				{
					group = "rejected";
				}
			}
			moderationTypes.put(group, notification);
		}

		moderationTypes.putAll("overdue",
			createTaskNotifications(typeMap.get(Notification.REASON_OVERDUE), lazyUserLookup));
		EmailNotifications notificationModel = new EmailNotifications(moderationTypes.asMap());

		StringWriter writer = new StringWriter();
		viewFactory.render(viewFactory.createResultWithModel("notification-tasks.ftl", notificationModel), writer);
		return writer.toString();
	}

	private List<TaskNotification> createTaskNotifications(List<Notification> notifications, LazyUserLookup lazyLookup)
	{
		List<ItemTaskId> itemTaskIds = Lists.newArrayList();
		for( Notification notification : notifications )
		{
			itemTaskIds.add(new ItemTaskId(notification.getItemid()));
		}
		LoadingCache<Workflow, Map<String, WorkflowItem>> map = CacheBuilder.newBuilder().build(
			new CacheLoader<Workflow, Map<String, WorkflowItem>>()
			{
				@Override
				public Map<String, WorkflowItem> load(Workflow workflow)
				{
					return workflow.getAllWorkflowItems();
				}
			});

		List<TaskNotification> taskNotifications = Lists.newArrayList();
		Map<ItemId, Item> items = itemService.queryItemsByItemIds(itemTaskIds);
		for( ItemTaskId itemTaskId : itemTaskIds )
		{
			TaskNotification task = new TaskNotification();
			Item item = items.get(ItemId.fromKey(itemTaskId));
			if( item != null )
			{
				Workflow workflow = item.getItemDefinition().getWorkflow();
				Map<String, WorkflowItem> allWorkflowItems = map.getUnchecked(workflow);
				WorkflowItem workflowItem = allWorkflowItems.get(itemTaskId.getTaskId());
				if( workflowItem != null )
				{
					WorkflowItemStatus status = workflowService.getIncompleteStatus(itemTaskId);
					setupCause(lazyLookup, status, task, workflow);
					task.setDueDate(status.getDateDue());
					setupAutoAction(task, workflowItem);
					task.setTaskName(new BundleLabel(workflowItem.getName(), bundleCache));
					task.setItemName(new ItemNameLabel(item, bundleCache));
					task.setLink(RootTaskListSection.createModerateBookmark(controller, itemTaskId));
					taskNotifications.add(task);
				}
			}
		}
		return taskNotifications;
	}

	private void setupAutoAction(TaskNotification task, WorkflowItem workflowItem)
	{
		AutoAction autoAction = workflowItem.getAutoAction();
		if( autoAction != AutoAction.NONE )
		{
			Label actionName = autoAction == AutoAction.REJECT ? LABEL_AUTOREJECT : LABEL_AUTOACCEPT;
			task.setAutoAction(new KeyLabel(KEY_AUTOMSG, actionName, new PluralKeyLabel(KEY_AUTODAYS, workflowItem
				.getActionDays())));
		}
	}

	private void setupCause(LazyUserLookup lazyLookup, WorkflowItemStatus status, TaskNotification task,
		Workflow workflow)
	{
		WorkflowNodeStatus cause = status.getCause();
		if( cause != null )
		{
			if( !workflow.getRoot().getUuid().equals(cause.getNode().getUuid()) )
			{
				boolean accepted = cause.getStatus() == WorkflowNodeStatus.COMPLETE;
				task.setCauseLabel(accepted ? LABEL_CAUSEACCEPTED : LABEL_CAUSEREJECTED);
				task.setCauseTask(new BundleLabel(cause.getNode().getName(), bundleCache));
				task.setCauseAccepted(accepted);
			}
			List<Message> messages = Lists.newArrayList();
			Set<WorkflowMessage> comments = cause.getComments();
			for( WorkflowMessage workflowMessage : comments )
			{
				char type = workflowMessage.getType();
				if( type == WorkflowMessage.TYPE_ACCEPT || type == WorkflowMessage.TYPE_REJECT
					|| type == WorkflowMessage.TYPE_SUBMIT )
				{
					messages.add(new Message(lazyLookup, workflowMessage));
				}
			}
			Collections.sort(messages, new Comparator<Message>()
			{
				@Override
				public int compare(Message m1, Message m2)
				{
					return m1.msg.getDate().compareTo(m2.msg.getDate());
				}
			});
			task.setMessages(messages);
		}
	}

	public static class EmailNotifications
	{
		private final Map<String, Collection<TaskNotification>> groups;

		public EmailNotifications(Map<String, Collection<TaskNotification>> groups)
		{
			this.groups = groups;
		}

		public Map<String, Collection<TaskNotification>> getGroups()
		{
			return groups;
		}
	}

	public static class TaskNotification extends ItemNotification
	{
		private Label taskName;
		private Label causeLabel;
		private Label causeTask;
		private boolean causeAccepted;
		private List<Message> messages = Collections.emptyList();
		private boolean optional;
		private Date dueDate;
		private Label autoAction;

		public Label getTaskName()
		{
			return taskName;
		}

		public void setTaskName(Label taskName)
		{
			this.taskName = taskName;
		}

		public boolean isOptional()
		{
			return optional;
		}

		public void setOptional(boolean optional)
		{
			this.optional = optional;
		}

		public Date getDueDate()
		{
			return dueDate;
		}

		public void setDueDate(Date dueDate)
		{
			this.dueDate = dueDate;
		}

		public Label getAutoAction()
		{
			return autoAction;
		}

		public void setAutoAction(Label autoAction)
		{
			this.autoAction = autoAction;
		}

		public Label getCauseLabel()
		{
			return causeLabel;
		}

		public void setCauseLabel(Label causeLabel)
		{
			this.causeLabel = causeLabel;
		}

		public Label getCauseTask()
		{
			return causeTask;
		}

		public void setCauseTask(Label causeTask)
		{
			this.causeTask = causeTask;
		}

		public List<Message> getMessages()
		{
			return messages;
		}

		public void setMessages(List<Message> messages)
		{
			this.messages = messages;
		}

		public boolean isCauseAccepted()
		{
			return causeAccepted;
		}

		public void setCauseAccepted(boolean causeAccepted)
		{
			this.causeAccepted = causeAccepted;
		}

	}

	public static class Message
	{
		private final WorkflowMessage msg;
		private UserLabel by;

		public Message(LazyUserLookup lazyLookup, WorkflowMessage message)
		{
			this.msg = message;
			this.by = new UserLabel(msg.getUser(), lazyLookup);
		}

		public Label getLabel()
		{
			return new KeyLabel(KEY_MESSAGELABEL + msg.getType());
		}

		public Label getBy()
		{
			return by;
		}

		public String getMessage()
		{
			return msg.getMessage();
		}
	}

	@Override
	public boolean isForceEmail(String type)
	{
		return type.equals(Notification.REASON_OVERDUE);
	}

	@Override
	public Label getReasonLabel(String type)
	{
		return new KeyLabel(KEY_REASON_LIST + type);
	}

	@Override
	public Label getReasonFilterLabel(String type)
	{
		return new KeyLabel(KEY_REASON_FILTER + type);
	}

}
