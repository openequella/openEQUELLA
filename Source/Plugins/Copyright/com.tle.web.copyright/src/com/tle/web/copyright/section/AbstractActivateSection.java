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

package com.tle.web.copyright.section;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.schema.Citation;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.EntityPack;
import com.tle.common.NameValue;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
import com.tle.common.util.LocalDate;
import com.tle.common.util.TleDate;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.activation.ActivationResultsExtension;
import com.tle.web.activation.filter.SelectCourseDialog;
import com.tle.web.copyright.CopyrightOverrideSection;
import com.tle.web.copyright.section.AbstractCopyrightSummarySection.AttachmentId;
import com.tle.web.copyright.section.ViewByRequestSection.ViewRequestUrl;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Calendar;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.CourseListSection;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

import net.sf.json.JSONObject;

@SuppressWarnings("nls")
@NonNullByDefault
@TreeIndexed
public abstract class AbstractActivateSection extends AbstractContentSection<AbstractActivateSection.Model>
{
	@PlugKey("activate.title")
	private static Label LABEL_TITLE;
	@PlugKey("activate.citatenone")
	private static String KEY_CITATENONE;
	@PlugKey("activate.error.nocourses")
	private static String KEY_NOCOURSES;
	@PlugKey("activate.error.coursenotfound")
	private static String KEY_COURSENOTFOUND;
	@PlugKey("activate.error.emptycourse")
	private static String KEY_NO_COURSE_SELECTED;
	@PlugKey("activate.error.accessdenied")
	private static Label LABEL_ACCESS_DENIED;
	@PlugKey("activate.course.select")
	private static Label LABEL_SELECT_COURSE;
	@PlugKey("activate.course.change")
	private static Label LABEL_CHANGE_COURSE;

	@Inject
	private ActivationService activationService;
	@Inject
	private SelectionService selectionService;
	private CopyrightWebService<? extends Holding> copyrightWebService;

	@AjaxFactory
	protected AjaxGenerator ajax;

	@Inject
	private ActivationResultsExtension resultsExtension;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private ItemDao itemDao;
	@Inject
	private TLEAclManager aclService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Component
	private Button cancelButton;
	@Component
	private Button activateButton;
	@Component(name = "c")
	private SingleSelectionList<Void> citationList;
	@Component(name = "fd")
	private Calendar fromDate;
	@Component(name = "ud")
	private Calendar untilDate;

	@Component
	private Button selectCourse;
	@Inject
	@Component
	private SelectCourseDialog selectCourseDialog;

	@TreeLookup
	private ItemSummaryContentSection summarySection;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);
		if( !itemInfo.getPrivileges().contains("COPYRIGHT_ITEM") )
		{
			throw new AccessDeniedException(LABEL_ACCESS_DENIED.getText());
		}

		Model model = getModel(context);
		if( model.getException() != null )
		{
			return fatalError(this);
		}

		IntegrationInterface integration = integrationService.getIntegrationInterface(context);
		String courseCode = integration == null ? null : integration.getCourseInfoCode();
		if( integration != null && courseCode != null )
		{
			if( courseInfoService.getByCode(courseCode) == null )
			{
				if( canAutoCreate(itemInfo.getItem()) )
				{
					CourseInfo i = new CourseInfo();
					i.setCode(courseCode);
					LanguageBundle nameBundle = new LanguageBundle();
					LangUtils.setString(nameBundle, CurrentLocale.getLocale(), courseCode);
					i.setName(nameBundle);
					model.setCourse(i);
					model.setShowCourseSelector(false);
					model.setCourseAutoCreated(true);
					updateCourseData(context);
				}
				else
				{
					model.setShowCourseSelector(true);
				}
			}
			else
			{
				model.setCourse(courseInfoService.getByCode(courseCode));
				updateCourseData(context);
			}
		}
		else
		{
			model.setShowCourseSelector(true);
			if( courseInfoService.enumerateEnabled().isEmpty() )
			{
				model.setException(makeException(KEY_NOCOURSES));
				return fatalError(this);
			}
			if( model.getCourseUuid() != null )
			{
				model.setCourse(courseInfoService.getByUuid(model.getCourseUuid()));
			}

		}

		if( model.getCourse() == null )
		{
			selectCourse.setLabel(context, LABEL_SELECT_COURSE);
		}
		else
		{
			selectCourse.setLabel(context, LABEL_CHANGE_COURSE);
		}

		StringBuilder sbuf = new StringBuilder();
		boolean first = true;
		for( AttachmentId attachId : model.getSelectedAttachments() )
		{
			Item item = itemDao.findById(attachId.getId());
			Map<String, Attachment> attachMap = copyrightWebService.getAttachmentMap(context, item);
			Attachment attachment = attachMap.get(attachId.getAttachmentId());
			if( !first )
			{
				sbuf.append(", ");
			}
			sbuf.append(attachment.getDescription());
			first = false;
		}
		model.setAttachmentList(sbuf.toString());
		model.setAddLabel(isSelectingForIntegration(context) ? AbstractCopyrightSummarySection.getActivateAndAddLabel()
			: AbstractCopyrightSummarySection.getActivateLabel());

		addDefaultBreadcrumbs(context, itemInfo, LABEL_TITLE);

		return viewFactory.createResult("activate.ftl", this);
	}

	private boolean canAutoCreate(Item item)
	{
		return !aclService.filterNonGrantedPrivileges(item, ActivationConstants.AUTO_CREATE_COURSE).isEmpty();
	}

	protected abstract String getActivationType();

	protected abstract CopyrightWebService<? extends Holding> getCopyrightServiceImpl();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
		activateButton.setClickHandler(events.getNamedHandler("activate"));
		citationList.setListModel(new CitationListModel());
		selectCourse.setClickHandler(new OverrideHandler(selectCourseDialog.getOpenFunction()));
		selectCourseDialog.setAjax(true);
		selectCourseDialog.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("courseChanged"), "courseajax", "errorajax"));
	}

	//Priority even lower than SectionEvent.PRIORITY_LOW so RootItemFileSection.ensureResourceBeforeRender happens first.
	//@EventHandlerMethod(priority = -150)
	@EventHandlerMethod
	public void activate(SectionInfo info) throws Exception
	{
		Model model = getModel(info);
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		LanguageBundle error = null;
		ActivateRequest request = new ActivateRequest();
		request.setCitation(citationList.getSelectedValueAsString(info));

		request.setFrom(toDate(fromDate.getDate(info)));
		request.setUntil(toDate(untilDate.getDate(info)));

		CourseInfo course = null;
		IntegrationInterface integration = integrationService.getIntegrationInterface(info);
		if( integration != null )
		{
			String courseCode = integration.getCourseInfoCode();
			if( courseCode != null && model.isCourseAutoCreated() )
			{
				CourseInfo i = new CourseInfo();
				i.setUuid(UUID.randomUUID().toString());
				i.setCode(courseCode);
				LanguageBundle nameBundle = new LanguageBundle();
				LangUtils.setString(nameBundle, CurrentLocale.getLocale(), courseCode);
				i.setName(nameBundle);
				EntityPack<CourseInfo> pack = new EntityPack<CourseInfo>(i, null);
				courseInfoService.add(pack, false);
			}
			if( courseCode != null && courseInfoService.getByCode(courseCode) != null )
			{
				course = courseInfoService.getByCode(courseCode);
				NameValue location = integration.getLocation();
				if( location != null )
				{
					request.setLocationId(location.getValue());
					request.setLocationName(location.getName());
				}
			}
		}
		if( course == null )
		{
			course = courseInfoService.getByUuid(model.getCourseUuid());
		}

		List<AttachmentId> attachments = model.getSelectedAttachments();

		Map<Long, List<ActivateRequest>> requestMap = new HashMap<Long, List<ActivateRequest>>();

		try
		{
			if( course == null )
			{
				throw new InvalidDataException(new ValidationError("", CurrentLocale.get(KEY_NO_COURSE_SELECTED)));
			}
			request.setCourse(course);

			for( AttachmentId attachId : attachments )
			{
				long itemId = attachId.getId();
				ActivateRequest newRequest = (ActivateRequest) request.clone();
				List<ActivateRequest> actRequests = requestMap.get(itemId);
				if( actRequests == null )
				{
					actRequests = new ArrayList<ActivateRequest>();
					requestMap.put(itemId, actRequests);
				}
				newRequest.setAttachment(attachId.getAttachmentId());
				actRequests.add(newRequest);
			}
			activationService.activateAll(getActivationType(), requestMap, false);
			if( updateSelectionSession(info, requestMap) )
			{
				return;
			}
		}
		catch( CopyrightViolationException we )
		{

			boolean canOverride = !aclService.filterNonGrantedPrivileges(ActivationConstants.COPYRIGHT_OVERRIDE)
				.isEmpty();
			if( canOverride && we.isCALBookPercentageException() )
			{
				CopyrightOverrideSection overrideSection = getOverrideSection();
				if( overrideSection != null )
				{
					overrideSection.doOverride(info, requestMap);
				}
			}
			else
			{
				if( model.isCourseAutoCreated() )
				{
					courseInfoService.delete(course, false);
				}
				model.setException(we);
			}
			info.preventGET();
			return;
		}
		catch( InvalidDataException e )
		{
			ValidationError er = e.getErrors().get(0);
			error = LangUtils.createTextTempLangugageBundle(er.getMessage());
			info.preventGET();
			model.setError(error);
			if( model.isCourseAutoCreated() )
			{
				courseInfoService.delete(course, false);
			}
			return;
		}
		summarySection.setSummaryId(info, null);
		itemInfo.refreshItem(true);
	}

	public boolean updateSelectionSession(SectionInfo info, Map<Long, List<ActivateRequest>> requestMap)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			// TODO: an even which extends the SelectedResource with
			// additional info e.g. folder?
			final CourseListSection cls = info.lookupSection(CourseListSection.class);

			for( Map.Entry<Long, List<ActivateRequest>> entry : requestMap.entrySet() )
			{
				Item item = itemDao.findById(entry.getKey());
				if( item != null )
				{
					for( ActivateRequest activateRequest : entry.getValue() )
					{
						Attachment attachment = copyrightWebService.getAttachmentMap(info, item)
							.get(activateRequest.getAttachment());

						if( cls != null && cls.isApplicable(info) )
						{
							for( String folder : cls.getSelectedFolders(info) )
							{
								addResource(info, new SelectedResource(item.getItemId(), attachment,
									selectionService.findTargetFolder(info, folder), null), activateRequest);
							}
						}
						else
						{
							addResource(info, new SelectedResource(item.getItemId(), attachment, null, null),
								activateRequest);
						}
						if( info.isRendered() )
						{
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	private void addResource(SectionInfo info, SelectedResource resource, ActivateRequest activateRequest)
	{
		resource.addExtender(new ViewRequestUrl(activateRequest.getUuid()));
		resultsExtension.addRequest(resource, activateRequest);
		selectionService.addSelectedResource(info, resource, true);
	}

	@Nullable
	private Date toDate(@Nullable TleDate date)
	{
		if( date == null )
		{
			return null;
		}
		// treat user's midnight 'today' as right now
		final LocalDate nowLocal = new LocalDate(new UtcDate(), CurrentTimeZone.get());
		if( date.conceptualDate().equals(nowLocal.conceptualDate()) )
		{
			return nowLocal.toDate();
		}
		// treat user's midnight 'tomorrow' as right now +24hours
		final LocalDate tomorrowLocal = nowLocal.addDays(1);
		if( date.conceptualDate().equals(tomorrowLocal.conceptualDate()) )
		{
			return tomorrowLocal.toDate();
		}

		return UtcDate.convertUtcMidnightToLocalMidnight(date, CurrentTimeZone.get()).toDate();
	}

	public SectionResult fatalError(SectionId from)
	{
		return viewFactory.createResult("violation.ftl", from);
	}

	private boolean isSelectingForIntegration(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		return (session != null && Boolean.TRUE.equals(session.getAttribute(IntegrationService.KEY_FORINTEGRATION)));
	}

	private CopyrightViolationException makeException(String key)
	{
		return new CopyrightViolationException(LangUtils.createTempLangugageBundle(key));
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		summarySection.setSummaryId(info, null);
	}

	@EventHandlerMethod
	public void courseChanged(SectionInfo info, String courseUUid)
	{
		CourseInfo course = courseInfoService.getByUuid(courseUUid);
		getModel(info).setCourse(course);
		updateCourseData(info);
		if( course != null )
		{
			citationList.setSelectedStringValue(info, course.getCitation());
		}
	}

	private void updateCourseData(SectionInfo info)
	{
		Model model = getModel(info);
		CourseInfo course = model.getCourse();
		if( course != null )
		{
			model.setCourseUuid(course.getUuid());

			final UtcDate[] cals = activationService.getDefaultCourseDates(course);
			fromDate.setDate(info, cals[0]);
			untilDate.setDate(info, cals[1]);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public Button getActivateButton()
	{
		return activateButton;
	}

	@PostConstruct
	void setupCopyrightService()
	{
		copyrightWebService = getCopyrightServiceImpl();
	}

	public void doActivate(SectionInfo info, String[] selectedSections)
	{
		final Model model = getModel(info);
		model.setSelected(selectedSections);
		summarySection.setSummaryId(info, this);
		updateCourseData(info);
	}

	@Nullable
	protected CopyrightOverrideSection getOverrideSection()
	{
		return null;
	}

	public static class CitationListModel extends DynamicHtmlListModel<Void>
	{
		@Nullable
		@Override
		protected Iterable<Void> populateModel(SectionInfo info)
		{
			return null;
		}

		@Override
		protected Iterable<Option<Void>> populateOptions(SectionInfo info)
		{
			ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
			List<Citation> citations = iinfo.getItem().getItemDefinition().getSchema().getCitations();
			List<Option<Void>> citNvs = new ArrayList<Option<Void>>();
			citNvs.add(new VoidKeyOption(KEY_CITATENONE, Constants.BLANK));
			for( Citation cite : citations )
			{
				citNvs.add(new SimpleOption<Void>(cite.getName(), cite.getName()));
			}
			return citNvs;
		}
	}

	public SingleSelectionList<Void> getCitationList()
	{
		return citationList;
	}

	public Calendar getFromDate()
	{
		return fromDate;
	}

	public Calendar getUntilDate()
	{
		return untilDate;
	}

	public Button getSelectCourse()
	{
		return selectCourse;
	}

	@NonNullByDefault(false)
	public static class Model
	{
		@Bookmarked(stateful = true)
		private String[] selected;
		// course can't be bookmarked for the activate() methods
		@Bookmarked(stateful = false)
		private String courseUuid;
		private CourseInfo course;
		private boolean showCourseSelector;
		private String attachmentList;
		private LanguageBundle error;
		private CopyrightViolationException exception;
		private Label addLabel;
		@Bookmarked(stateful = false)
		private boolean courseAutoCreated;

		public List<AttachmentId> getSelectedAttachments()
		{
			ArrayList<AttachmentId> attachments = new ArrayList<AttachmentId>();
			if( selected != null )
			{
				for( String selectId : selected )
				{
					JSONObject selObj = JSONObject.fromObject(selectId);
					AttachmentId attachId = (AttachmentId) JSONObject.toBean(selObj, AttachmentId.class);
					attachments.add(attachId);
				}
			}
			return attachments;
		}

		public String getAttachmentList()
		{
			return attachmentList;
		}

		public void setAttachmentList(String attachmentList)
		{
			this.attachmentList = attachmentList;
		}

		public LanguageBundle getError()
		{
			return error;
		}

		public void setError(LanguageBundle error)
		{
			this.error = error;
		}

		public CopyrightViolationException getException()
		{
			return exception;
		}

		public void setException(CopyrightViolationException exception)
		{
			this.exception = exception;
		}

		public CourseInfo getCourse()
		{
			return course;
		}

		public void setCourse(CourseInfo course)
		{
			this.course = course;
		}

		public String[] getSelected()
		{
			return selected;
		}

		public void setSelected(String[] selected)
		{
			this.selected = selected;
		}

		public boolean isShowCourseSelector()
		{
			return showCourseSelector;
		}

		public void setShowCourseSelector(boolean showCourseSelector)
		{
			this.showCourseSelector = showCourseSelector;
		}

		public Label getAddLabel()
		{
			return addLabel;
		}

		public void setAddLabel(Label addLabel)
		{
			this.addLabel = addLabel;
		}

		public String getCourseUuid()
		{
			if( courseUuid == null && course != null )
			{
				return course.getUuid();
			}
			return courseUuid;
		}

		public void setCourseUuid(String courseUuid)
		{
			this.courseUuid = courseUuid;
		}

		public boolean isCourseAutoCreated()
		{
			return courseAutoCreated;
		}

		public void setCourseAutoCreated(boolean courseAutoCreated)
		{
			this.courseAutoCreated = courseAutoCreated;
		}

	}
}
