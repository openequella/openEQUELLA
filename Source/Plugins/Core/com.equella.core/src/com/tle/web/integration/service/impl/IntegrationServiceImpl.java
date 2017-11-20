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

package com.tle.web.integration.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.integration.IntegrationLoggingService;
import com.tle.core.integration.IntegrationSelection;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserSessionService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.integration.Integration;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.IntegrationInterface;
import com.tle.web.integration.IntegrationSection;
import com.tle.web.integration.IntegrationSessionData;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.generic.AbstractSectionFilter;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.TreeLookupSelectionCallback;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind(IntegrationService.class)
@Singleton
public class IntegrationServiceImpl extends AbstractSectionFilter implements IntegrationService
{
	@Inject
	private UserSessionService sessionService;
	@Inject
	private IntegrationLoggingService integrationLoggingService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewableItemResolver viewableItemResolver;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private PluginTracker<Integration<? extends IntegrationSessionData>> integrationTracker;
	@Inject
	@Named("integrationTree")
	private SectionTree tree;
	@Inject
	private PluginTracker<IntegrationActionInfo> actionInfoTracker;
	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SectionsController sectionsController;
	@Inject
	private SelectionService selectionService;
	@Inject
	private TLEAclManager aclService;

	@Nullable
	@Override
	public IntegrationInterface getIntegrationInterface(SectionInfo info)
	{
		return info.getAttribute(IntegrationInterface.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Integration<IntegrationSessionData> getIntegrationServiceForId(String id)
	{
		return (Integration<IntegrationSessionData>) integrationTracker
			.getBeanByExtension(integrationTracker.getExtension(id));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Integration<IntegrationSessionData> getIntegrationServiceForData(IntegrationSessionData data)
	{
		return (Integration<IntegrationSessionData>) integrationTracker.getBeanMap().get(data.getIntegrationType());
	}

	@Override
	protected SectionTree getFilterTree()
	{
		return tree;
	}

	@Nullable
	@Override
	public IntegrationSessionData getSessionData(SectionInfo info)
	{
		IntegrationSection isection = info.lookupSection(IntegrationSection.class);
		if( isection == null )
		{
			return null;
		}

		IntegrationSessionData data = null;
		String id = isection.getStateId(info);
		if( id != null )
		{
			data = sessionService.getAttribute(id);
			if( data == null )
			{
				isection.newSession(info, null);
			}
		}
		return data;
	}

	@Override
	public String setupSessionData(SectionInfo info, IntegrationSessionData data)
	{
		IntegrationSection isection = info.lookupSection(IntegrationSection.class);
		String id = sessionService.createUniqueKey();
		sessionService.setAttribute(id, data);
		isection.newSession(info, id);
		return id;
	}

	@Override
	public boolean isInIntegrationSession(SectionInfo info)
	{
		return getSessionData(info) != null;
	}

	@Override
	public void logSelections(SectionInfo info, SelectionSession session)
	{
		Collection<SelectedResource> resources = session.getSelectedResources();
		List<IntegrationSelection> logs = new ArrayList<IntegrationSelection>();
		for( SelectedResource selectedResource : resources )
		{
			logs.add(convertToLog(info, selectedResource));
		}
		integrationLoggingService.logSelections(logs);
	}

	private IntegrationSelection convertToLog(SectionInfo info, SelectedResource selectedResource)
	{
		ItemId itemId = new ItemId(selectedResource.getUuid(), selectedResource.getVersion());
		IntegrationSelection selection = new IntegrationSelection(itemId);
		String resource = null;
		String contentType = null;
		String selectionValue = null;
		char type = selectedResource.getType();
		final String itemExtensionType = selectedResource.getKey().getExtensionType();
		switch( type )
		{
			case SelectedResource.TYPE_ATTACHMENT:
				IItem<? extends IAttachment> item = itemResolver.getItem(itemId, itemExtensionType);
				if( item == null )
				{
					throw new Error("Unknown item " + itemId.toString() + " in selection session");
				}
				Map<String, ? extends IAttachment> attachmentMap = UnmodifiableAttachments.convertToMapUuid(item);
				ViewableResource vres = attachmentResourceService.getViewableResource(info,
					viewableItemResolver.createViewableItem(item, itemExtensionType),
					attachmentMap.get(selectedResource.getAttachmentUuid()));
				ViewAuditEntry auditEntry = vres.getViewAuditEntry();
				if( auditEntry != null )
				{
					contentType = auditEntry.getContentType();
					resource = auditEntry.getPath();
				}
				selectionValue = selectedResource.getAttachmentUuid();
				break;
			case SelectedResource.TYPE_REMOTE:
				resource = selectedResource.getUrl();
				break;
			case SelectedResource.TYPE_PATH:
				if( selectedResource.getUrl().length() == 0 )
				{
					type = 'i';
				}
				else
				{
					selectionValue = selectedResource.getAttachmentUuid();
					resource = selectedResource.getUrl();
				}
				break;

			default:
				break;
		}
		selection.setLatest(selectedResource.isLatest());
		selection.setType(type);
		selection.setResource(resource);
		selection.setSelection(selectionValue);
		selection.setContentType(contentType);
		return selection;
	}

	@Override
	@Nullable
	public IntegrationActionInfo getActionInfoForUrl(String url)
	{
		for( IntegrationActionInfo info : actionInfoTracker.getBeanList() )
		{
			if( Objects.equals(url, info.getPath()) )
			{
				return info;
			}
		}
		return null;
	}

	@Override
	public IntegrationActionInfo getActionInfo(String name, @Nullable String userOptions)
	{
		IntegrationActionInfo action = new IntegrationActionInfo();
		IntegrationActionInfo predefined = actionInfoTracker.getBeanMap().get(name);

		if( predefined == null )
		{
			if( name.indexOf('.') != -1 || name.indexOf('/') != -1 )
			{
				action.setPath(name);
				action.setName("unknown");
			}
			else
			{
				throw new SectionsRuntimeException("Unknown integration action:" + name);
			}
		}
		else
		{
			action.initFromOther(predefined);
		}
		Map<String, Object> allOptions = action.getOptionMap();
		if( userOptions != null )
		{
			Map<String, String[]> userOptionsMap = SectionUtils.parseParamString(userOptions);
			for( Map.Entry<String, String[]> entry : userOptionsMap.entrySet() )
			{
				allOptions.put(entry.getKey(), entry.getValue()[0]);
			}
		}
		action.setOptionMap(allOptions);
		return action;
	}

	@Nullable
	private String matchCourseInfo(@Nullable String code)
	{
		if( !Strings.isNullOrEmpty(code) && courseInfoService.getByCode(code) != null )
		{
			return code;
		}
		return null;
	}

	/**
	 * Returns the code of the first matching CourseInfo against courseCodes then courseId.
	 * Returns the first non-empty courseCode or courseId if none of those matched.
	 *
	 * @param courseId
	 * @param courseCodes
	 * @return
	 */
	@Nullable
	@Override
	public String getCourseInfoCode(@Nullable String courseId, String... courseCodes)
	{
		// Look for CourseInfo with matching code
		for( String code : courseCodes )
		{
			final String match = matchCourseInfo(code);
			if( match != null )
			{
				return match;
			}
		}

		// Try the course ID
		final String match = matchCourseInfo(courseId);
		if( match != null )
		{
			return match;
		}

		// Return the first non-empty code (nothing has matched)
		final String firstNonEmpty = Utils.coalesce(courseCodes);
		if( !Strings.isNullOrEmpty(firstNonEmpty) )
		{
			return firstNonEmpty;
		}

		// return the courseId, null or otherwise
		return courseId;
	}

	@Override
	public void standardForward(SectionInfo info, String forward, IntegrationSessionData data,
		IntegrationActionInfo action, SingleSignonForm form)
	{
		checkIntegrationAllowed();

		SelectionSession session = new SelectionSession(
			new TreeLookupSelectionCallback(IntegrationService.KEY_INTEGRATION_CALLBACK));
		session.setSelectMultiple(false);
		session.setAttribute(IntegrationService.KEY_FORINTEGRATION, true);

		populateSessionOptions(session, action.getOptionMap());
		String selectable = action.getSelectable();
		if( session.getHomeSelectable() != null )
		{
			selectable = session.getHomeSelectable();
		}

		final Integration<IntegrationSessionData> integration = getIntegrationServiceForData(data);
		session = integration.setupSelectionSession(info, data, session, form);

		SectionInfo forwardInfo;
		if( Check.isEmpty(selectable) || session == null )
		{
			final URL iUrl = institutionService.getInstitutionUrl();
			URL forwardUrl;
			try
			{
				forwardUrl = new URL(iUrl, forward);
			}
			catch( MalformedURLException e )
			{
				throw Throwables.propagate(e);
			}
			forward = forwardUrl.getPath().substring(iUrl.getPath().length() - 1);

			if( sectionsController.treeExistsForUrlPath(forward) )
			{
				final Map<String, String[]> props = SectionUtils.parseParamString(forwardUrl.getQuery());

				forwardInfo = sectionsController.createInfo(forward, info.getRequest(), info.getResponse(), info, props,
					Collections.singletonMap(SectionInfo.KEY_FROM_REQUEST, true));
			}
			else
			{
				info.forwardToUrl(forwardUrl.toString());
				return;
			}
		}
		else
		{
			SelectableInterface selectableObj = selectionService.getNamedSelectable(selectable);
			forwardInfo = selectionService.getSelectionSessionForward(info, session, selectableObj);
		}
		setupSessionData(forwardInfo, data);
		integration.forward(info, data, forwardInfo);
		//What is this?? Not required
		//info.forwardAsBookmark(forwardInfo);
	}

	private void populateSessionOptions(SelectionSession session, Map<String, Object> options)
	{
		if( options.containsKey("layout") )
		{
			switch( (String) options.get("layout") )
			{
				case "COURSE":
					session.setLayout(Layout.COURSE);
					break;
				case "SKINNY":
					session.setLayout(Layout.SKINNY);
					break;
				default:
					session.setLayout(Layout.NORMAL);
			}
		}

		if( options.containsKey("home") )
		{
			String home = (String) options.get("home");
			Layout layout = session.getLayout();
			if( layout.equals(Layout.COURSE) || layout.equals(Layout.SKINNY) )
			{
				switch( home )
				{
					case "search":
						home = layout.equals(Layout.COURSE) ? "coursesearch" : "skinnysearch";
						break;
					case "browse":
						home = "skinnybrowse";
						break;
					case "favourites":
						home = "skinnyfavourites";
						break;
				}
			}
			session.setHomeSelectable(home);
		}
		if( options.containsKey("defaultCollectionUuid") )
		{
			String defaultCollectionUuid = (String) options.get("defaultCollectionUuid");
			session.setDefaultCollectionUuid(defaultCollectionUuid);
		}
		session.setAllCollections(bool(session.isAllCollections(), options, "allCollections"));
		session.setAllPowerSearches(bool(session.isAllPowerSearches(), options, "allPowerSearches"));
		session.setAllContributionCollections(
			bool(session.isAllContributionCollections(), options, "allContributionCollections"));
		session.setAllRemoteRepositories(bool(session.isAllRemoteRepositories(), options, "allRemoteRepositories"));
		session.setUseDownloadPrivilege(bool(session.isUseDownloadPrivilege(), options, "useDownloadPrivilege"));

		Set<String> collectionIds = uuidSet(options, "collectionIds");
		if( collectionIds != null )
		{
			session.setCollectionUuids(collectionIds);
			session.setAllCollections(false);
		}

		Set<String> powerSearches = uuidSet(options, "powerSearchIds");
		if( powerSearches != null )
		{
			session.setPowerSearchIds(powerSearches);
			session.setAllPowerSearches(false);
		}

		Set<String> contributionIds = uuidSet(options, "contributionCollectionIds");
		if( contributionIds != null )
		{
			session.setContributionCollectionIds(contributionIds);
			session.setAllContributionCollections(false);
		}

		Set<String> dynamicCollectionIds = uuidSet(options, "dynamicCollectionIds");
		if( dynamicCollectionIds != null )
		{
			session.setDynamicCollectionIds(dynamicCollectionIds);
			session.setAllDynamicCollections(false);
		}

		Set<String> remoteRepositoryIds = uuidSet(options, "remoteRepositoryIds");
		if( remoteRepositoryIds != null )
		{
			session.setRemoteRepositoryIds(remoteRepositoryIds);
			session.setAllRemoteRepositories(false);
		}

		Set<String> mimeTypes = uuidSet(options, "mimeTypes");
		if( !Check.isEmpty(mimeTypes) )
		{
			session.setMimeTypes(mimeTypes);
		}
	}

	private boolean bool(boolean already, Map<String, Object> options, String property)
	{
		if( options.containsKey(property) )
		{
			return Boolean.parseBoolean(((String) options.get(property)));
		}
		return already;
	}

	@SuppressWarnings("nls")
	@Nullable
	private Set<String> uuidSet(Map<String, Object> options, String property)
	{
		String uuidStr = (String) options.get(property);
		if( !Check.isEmpty(uuidStr) )
		{
			Set<String> uuidSet = new HashSet<String>();
			String[] uuids = uuidStr.split(",");
			for( String uuid : uuids )
			{
				uuidSet.add(uuid.trim());
			}
			return uuidSet;
		}
		return null;
	}

	@Override
	public void checkIntegrationAllowed() throws AccessDeniedException
	{
		if( aclService.filterNonGrantedPrivileges("INTEGRATION_SELECTION_SESSION").isEmpty() )
		{
			throw new AccessDeniedException(
				CurrentLocale.get("com.tle.web.integration.error.selectionsession.accessdenied"));
		}
	}
}
