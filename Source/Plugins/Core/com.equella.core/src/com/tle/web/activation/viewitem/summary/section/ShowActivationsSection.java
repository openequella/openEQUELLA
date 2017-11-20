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

package com.tle.web.activation.viewitem.summary.section;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.time.FastDateFormat;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleLink;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderCell;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class ShowActivationsSection extends AbstractContentSection<ShowActivationsSection.ShowActivationsModel>
{
	@PlugKey("activations.title")
	private static Label ACTIVATIONS_TITLE;
	@PlugKey("activations.link.delete")
	private static Label DELETE_LABEL;
	@PlugKey("activations.link.edit")
	private static Label EDIT_LABEL;
	@PlugKey("activations.confirm.delete")
	private static Confirm CONFIRM_DELETE;
	@PlugKey("activations.detached")
	private static String DETACHED_KEY;

	@PlugKey("activations.attachment")
	private static Label ATTACHMENT_LABEL;
	@PlugKey("activations.status")
	private static Label STATUS_LABEL;
	@PlugKey("activations.information")
	private static Label INFORMATION_LABEL;

	@PlugKey("activations.students")
	private static String STUDENTS_KEY;
	@PlugKey("activations.user")
	private static String USER_KEY;
	@PlugKey("activations.course")
	private static String COURSE_KEY;
	@PlugKey("activations.from")
	private static String FROM_KEY;
	@PlugKey("activations.until")
	private static String UNTIL_KEY;

	@Inject
	private UserService userService;
	@Inject
	private ActivationService activationService;
	@TreeLookup
	private EditActivationSection editActivationSection;

	@ViewFactory
	private FreemarkerFactory view;

	private SubmitValuesFunction deleteFunc;

	@Component
	private Table activationsTable;

	private JSCallable editActivationFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		deleteFunc = events.getSubmitValuesFunction("delete");
		TableHeaderCell attHeader = new TableHeaderCell(activationsTable, ATTACHMENT_LABEL);
		attHeader.addClass("attachment");
		activationsTable.setColumnHeadings(attHeader, STATUS_LABEL, INFORMATION_LABEL, null);
		editActivationFunction = events.getSubmitValuesFunction("editActivation");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		ShowActivationsModel model = getModel(context);
		if( canView(context, itemInfo, null) )
		{
			List<ActivateRequest> requests = model.getRequests();
			Set<ActivateRequest> deletableSet = new HashSet<ActivateRequest>(
				activationService.filterDeletableRequest(requests));
			Set<ActivateRequest> editableSet = new HashSet<ActivateRequest>(
				activationService.filterEditableRequest(requests));

			List<ActivationDisplay> activationList = new ArrayList<ActivationDisplay>();

			for( ActivateRequest request : requests )
			{
				Map<String, Attachment> attachMap = UnmodifiableAttachments.convertToMapUuid(request.getItem()
					.getAttachmentsUnmodifiable());

				ActivationDisplay activation = new ActivationDisplay();
				activation.setUser(userService.getInformationForUser(request.getUser()));
				Attachment attachment = attachMap.get(request.getAttachment());
				if( attachment != null )
				{
					activation.setAttachment(attachment.getDescription());
				}
				else
				{
					activation.setAttachment(CurrentLocale.get(DETACHED_KEY));
				}
				activation.setCourseName(request.getCourse().getName());
				LanguageBundle descriptionBundle = request.getCourse().getDescription();
				String description = CurrentLocale.get(descriptionBundle, null);
				if( !Check.isEmpty(description) )
				{
					try
					{
						String link = description.trim();
						if( link.indexOf('@') != -1 )
						{
							link = "mailto:" + link; //$NON-NLS-1$
						}
						new URL(link);
						activation.setCourse(new LinkRenderer(new SimpleLink(link, description, null)));
					}
					catch( MalformedURLException mue )
					{
						activation.setCourse(new SimpleSectionResult(Utils.ent(description)));
					}
				}
				activation.setFrom(request.getFrom());
				activation.setStudents(request.getCourse().getStudents());
				activation.setStatus(activationService.getStatusKey(request.getStatus()));
				activation.setUntil(request.getUntil());
				if( deletableSet.contains(request) )
				{
					HtmlLinkState delete = new HtmlLinkState();
					delete.setLabel(DELETE_LABEL);
					delete.setClickHandler(new OverrideHandler(deleteFunc, request.getId())
						.addValidator(CONFIRM_DELETE));
					activation.setDelete(delete);
				}
				if( editableSet.contains(request) )
				{
					HtmlLinkState edit = new HtmlLinkState();
					edit.setLabel(EDIT_LABEL);
					edit.setClickHandler(new OverrideHandler(editActivationFunction, request.getUuid()));
					activation.setEdit(edit);
				}
				activationList.add(activation);
			}
			model.setActivations(!activationList.isEmpty());
			for( ActivationDisplay activation : activationList )
			{
				TableCell attCell = new TableCell(new TextLabel(activation.getAttachment(), true));
				attCell.addClass("attachment");

				TableCell statusCell = new TableCell(new KeyLabel(activation.getStatus()));
				statusCell.addClass("status");

				TableCell infoCell = new TableCell(new KeyLabel(STUDENTS_KEY, activation.getStudents()));
				infoCell.addContent(new KeyLabel(USER_KEY, Format.format(activation.getUser())));
				infoCell.addContent(new KeyLabel(COURSE_KEY, CurrentLocale.get(activation.getCourseName())));
				SectionRenderable course = activation.getCourse();
				if( course != null )
				{
					infoCell.addContent(" - ");
					infoCell.addContent(course);
				}
				FastDateFormat dateFormat = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG,
					FastDateFormat.LONG, CurrentLocale.getLocale());
				infoCell.addContent(new TextLabel("<br>", true));
				infoCell.addContent(new KeyLabel(FROM_KEY, dateFormat.format(activation.getFrom())));
				infoCell.addContent(new KeyLabel(UNTIL_KEY, dateFormat.format(activation.getUntil())));
				infoCell.addClass("info");

				TableCell actionCell = new TableCell();
				if( activation.getEdit() != null )
				{
					actionCell.addContent(new LinkRenderer(activation.getEdit()));
				}
				if( activation.getDelete() != null )
				{
					if( !Check.isEmpty(actionCell.getContent()) )
					{
						actionCell.addContent(new TextLabel(" | "));
					}
					actionCell.addContent(new LinkRenderer(activation.getDelete()));
				}
				actionCell.addClass("actions");

				activationsTable.addRow(context, attCell, statusCell, infoCell, actionCell);
			}
		}

		addDefaultBreadcrumbs(context, itemInfo, ACTIVATIONS_TITLE);
		displayBackButton(context);

		return view.createResult("activations.ftl", this);
	}

	@EventHandlerMethod
	public void editActivation(SectionInfo info, String activationUuid)
	{
		editActivationSection.doEdit(info, activationUuid);
	}

	@Override
	public Class<ShowActivationsModel> getModelClass()
	{
		return ShowActivationsModel.class;
	}

	protected List<String> getActivationTypes()
	{
		return activationService.getImplementationTypes();
	}

	public boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		final List<ActivateRequest> requests = new ArrayList<ActivateRequest>();
		for( String implType : getActivationTypes() )
		{
			requests.addAll(activationService.getAllRequests(implType, itemInfo.getItem()));
		}

		// FIXME
		// CourseInfo course = new CourseInfo();
		// course.setName(LangUtils.createTextTempLangugageBundle("Coursicals"));
		// ActivateRequest ar = new ActivateRequest();
		// ar.setAttachment("72185e80-9cf8-4ffa-a2be-06cbd8dbd87e");
		// ar.setCitation("HARVARD");
		// ar.setCourse(course);
		// ar.setStatus(ActivateRequest.TYPE_ACTIVE);
		// Date from = new Date();
		// from.setYear(from.getYear() - 1);
		// ar.setFrom(from);
		// Date until = new Date();
		// until.setYear(until.getYear() + 1);
		// ar.setUntil(new Date());
		// ar.setTime(new Date());
		// ar.setFromDate(from);
		// ar.setUser("caladmin");
		// ar.setType("cal");
		// ar.setItem(ParentViewItemSectionUtils.getItemInfo(info).getItem());
		// ar.setId(999999);
		// requests.add(ar);

		final ShowActivationsModel model = getModel(info);

		Collections.sort(requests, new Comparator<ActivateRequest>()
		{
			@Override
			public int compare(ActivateRequest o1, ActivateRequest o2)
			{
				int status1 = getStatus(o1);
				int status2 = getStatus(o2);
				if( status1 == status2 )
				{
					return o2.getTime().compareTo(o1.getTime());
				}
				return status1 - status2;
			}

			private int getStatus(ActivateRequest o1)
			{
				switch( o1.getStatus() )
				{
					case ActivateRequest.TYPE_ACTIVE:
						return 0;
					case ActivateRequest.TYPE_PENDING:
						return 1;
					case ActivateRequest.TYPE_INACTIVE:
						return 2;
					default:
						throw new RuntimeException("Unknown status"); //$NON-NLS-1$
				}
			}
		});
		model.setRequests(requests);
		return !requests.isEmpty();
	}

	@EventHandlerMethod
	public void delete(SectionInfo info, long id)
	{
		activationService.delete(null, id);
	}

	public Table getActivationsTable()
	{
		return activationsTable;
	}

	public static class ActivationDisplay
	{
		private String attachment;
		private UserBean user;
		private LanguageBundle courseName;
		private int students;
		private String status;
		private Date from;
		private Date until;
		private HtmlComponentState delete;
		private HtmlComponentState edit;
		private SectionRenderable course;

		public UserBean getUser()
		{
			return user;
		}

		public void setUser(UserBean user)
		{
			this.user = user;
		}

		public LanguageBundle getCourseName()
		{
			return courseName;
		}

		public void setCourseName(LanguageBundle courseName)
		{
			this.courseName = courseName;
		}

		public int getStudents()
		{
			return students;
		}

		public void setStudents(int students)
		{
			this.students = students;
		}

		public String getStatus()
		{
			return status;
		}

		public void setStatus(String status)
		{
			this.status = status;
		}

		public Date getFrom()
		{
			return from;
		}

		public void setFrom(Date from)
		{
			this.from = from;
		}

		public Date getUntil()
		{
			return until;
		}

		public void setUntil(Date until)
		{
			this.until = until;
		}

		public String getAttachment()
		{
			return attachment;
		}

		public void setAttachment(String attachment)
		{
			this.attachment = attachment;
		}

		public HtmlComponentState getDelete()
		{
			return delete;
		}

		public void setDelete(HtmlComponentState delete)
		{
			this.delete = delete;
		}

		public HtmlComponentState getEdit()
		{
			return edit;
		}

		public void setEdit(HtmlComponentState edit)
		{
			this.edit = edit;
		}

		public SectionRenderable getCourse()
		{
			return course;
		}

		public void setCourse(SectionRenderable course)
		{
			this.course = course;
		}
	}

	public static class ShowActivationsModel
	{
		private List<ActivateRequest> requests;
		private boolean activations;

		public List<ActivateRequest> getRequests()
		{
			return requests;
		}

		public void setRequests(List<ActivateRequest> requests)
		{
			this.requests = requests;
		}

		public boolean isActivations()
		{
			return activations;
		}

		public void setActivations(boolean activations)
		{
			this.activations = activations;
		}
	}
}
