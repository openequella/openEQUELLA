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

package com.tle.web.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;

import com.dytech.edge.queries.FreeTextQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.settings.standard.QuickContributeAndVersionSettings;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.equella.AbstractModalSessionServiceImpl;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.selection.event.AllAttachmentsSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.ItemSelectorEvent;
import com.tle.web.selection.event.PackageSelectorEvent;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.selection.section.SelectionCheckoutSection;
import com.tle.web.selection.section.SelectionSummarySection;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind(SelectionService.class)
@Singleton
public class SelectionServiceImpl extends AbstractModalSessionServiceImpl<SelectionSession> implements SelectionService
{
	private static final String RESULTS_RETURNED = "$RESULTS_RETURNED$";
	private static final int MAXIMUM_STORED_RECENT_SELECTIONS = 30;

	@Inject
	private PluginTracker<SelectableInterface> selectables;
	@Inject
	private PluginTracker<SelectableAttachment> selectableAttachments;
	@Inject
	private UserPreferenceService userPreferenceService;
	// Only used for recent selections stuff. Could be changed to use
	// itemResolver, but requires
	// unionItemUuidsWithCollectionUuids and queryItemsByUuids on that service
	@Inject
	private ItemService itemService;
	@Inject
	private FreeTextService searchService;
	@Inject
	private ViewableItemFactory viewableFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private SectionTree selectionTree;
	@Inject
	private ObjectMapperService objectMapperService;
	@Inject
	private ConfigurationService configurationService;

	@Nullable
	@Override
	public SelectionSession getCurrentSession(SectionInfo info)
	{
		return info.getAttribute(SelectionSession.class);
	}

	@Override
	public String getSearchPrivilege(SectionInfo info)
	{
		final SelectionSession session = getCurrentSession(info);
		return (session != null && session.isUseDownloadPrivilege() ? DefaultSearch.PRIV_DOWNLOAD_ITEM
			: DefaultSearch.PRIV_DISCOVER_ITEM);
	}

	@Override
	public void disableSelection(SectionInfo info)
	{
		setupSelectionSession(info, null);
	}

	@Override
	public void forwardToCheckout(SectionInfo info)
	{
		SectionInfo cinfo = info.createForward("/access/selection/checkout.do");
		SelectionCheckoutSection scs = cinfo.lookupSection(SelectionCheckoutSection.class);
		scs.setContinueSelectionsBackTo(cinfo, info);
		info.forward(cinfo);
	}

	@Override
	public void returnFromSession(SectionInfo info)
	{
		SelectionSession session = getCurrentSession(info);
		if( info.isRendered() || info.getAttribute(RESULTS_RETURNED) != null )
		{
			return;
		}
		info.setAttribute(RESULTS_RETURNED, true);

		boolean maintainSelections = false;
		SelectionsMadeCallback callback = session.getSelectionsMadeCallback();
		if( callback != null )
		{
			maintainSelections = callback.executeSelectionsMade(info, session);
		}
		else
		{
			throw new RuntimeException(CurrentLocale.get("com.tle.web.selection.error.callbackrequired"));
		}
		if( !maintainSelections )
		{
			session.clearResources();
		}
	}

	@Override
	public List<SelectionHistory> getRecentSelections(SectionInfo info, final int maximum)
	{
		return getRecentSelections(getCurrentSession(info), maximum);
	}

	@Override
	public List<SelectionHistory> getRecentSelections(SelectionSession session, final int maximum)
	{
		final ObjectMapper mapper = objectMapperService.createObjectMapper();
		List<SelectionHistory> list = getAllSavedRecentSelections(mapper);

		if( !list.isEmpty() )
		{
			filterIfNotAllCollections(session, list);

			filterIfSessionMimeTypes(session, list);

			Collections.sort(list, DATE_COMPARATOR);
			if( maximum > 0 && list.size() > maximum )
			{
				list = list.subList(0, maximum);
			}
		}

		return list;
	}

	// filter out collections, unless All Collections specified
	private void filterIfNotAllCollections(SelectionSession session, List<SelectionHistory> list)
	{
		if( !session.isAllCollections() )
		{
			Set<String> uuids = new HashSet<String>();
			for( SelectionHistory sr : list )
			{
				uuids.add(sr.getUuid());
			}

			uuids = itemService.unionItemUuidsWithCollectionUuids(uuids, session.getCollectionUuids());

			for( Iterator<SelectionHistory> iter = list.iterator(); iter.hasNext(); )
			{
				if( !uuids.contains(iter.next().getUuid()) )
				{
					iter.remove();
				}
			}
		}
	}

	// filter out on mime type if so restricted
	private void filterIfSessionMimeTypes(SelectionSession session, List<SelectionHistory> list)
	{
		Set<String> mimeTypes = session.getMimeTypes();
		if( mimeTypes != null && !mimeTypes.isEmpty() )
		{
			Set<String> uuids = new HashSet<String>();
			for( SelectionHistory sr : list )
			{
				uuids.add(sr.getUuid());
			}
			DefaultSearch search = new DefaultSearch();
			search.addMust(FreeTextQuery.FIELD_UUID, uuids);
			search.setMimeTypes(mimeTypes);
			// FIXME: item specific!!
			List<Item> results = searchService.search(search, 0, -1).getResults();
			Set<String> resultsUuids = new HashSet<String>();
			for( IItem<?> result : results )
			{
				resultsUuids.add(result.getUuid());
			}

			for( Iterator<SelectionHistory> iter = list.iterator(); iter.hasNext(); )
			{

				if( !resultsUuids.contains(iter.next().getUuid()) )
				{
					iter.remove();
				}
			}
		}
	}

	@Override
	public void addSelectedResource(SectionInfo info, SelectedResource selectedResource, boolean forward)
	{
		// do some basic validation (and ease Sonar's cyclomatic complexity)
		validateSelectedResource(selectedResource);

		SelectionSession session = getCurrentSession(info);
		String folderId = selectedResource.getKey().getFolderId();
		if( folderId == null )
		{
			session.addResource(selectedResource);
		}
		else
		{
			TargetFolder folder = findTargetFolder(info, folderId);
			session.addResource(selectedResource, folder);
		}
		if( session.isAddToRecentSelections() )
		{
			addToRecentSelections(selectedResource);
		}

		if( (forward && !session.isSelectMultiple()) )
		{
			if( session.isSkipCheckoutPage() )
			{
				returnFromSession(info);
			}
			else
			{
				forwardToCheckout(info);
			}
		}
	}

	// do some basic validation on selectedResource. Throws RuntimeException on
	// invalidity
	private void validateSelectedResource(SelectedResource selectedResource)
	{
		if( selectedResource.getUuid() == null || selectedResource.getUuid().length() == 0 )
		{
			throw new RuntimeException("SelectedResource uuid cannot be empty");
		}
		if( selectedResource.getType() == SelectedResource.TYPE_ATTACHMENT
			&& (selectedResource.getAttachmentUuid() == null || selectedResource.getAttachmentUuid().length() == 0) )
		{
			throw new RuntimeException("SelectedResource attachmentUuid cannot be empty when type=='a'");
		}
	}

	private void addToRecentSelections(SelectedResource resource)
	{
		final ObjectMapper mapper = objectMapperService.createObjectMapper();
		List<SelectionHistory> list = getAllSavedRecentSelections(mapper);

		// we'll have to remove something then!
		// note: we store 30 but only display 10 to make up for shortfalls
		// when users are only allowed to select from a restricted number of
		// collections.
		if( list.size() >= MAXIMUM_STORED_RECENT_SELECTIONS )
		{
			// need to sort the list so that we always remove the oldest one.
			Collections.sort(list, DATE_COMPARATOR);
			list.remove(list.size() - 1);
		}

		// if the resource already exists in the set, remove it first and then
		// replace it with the latest copy
		// keeping in mind that the compareTo and equals methods in
		// SelectedResource ignores the date field.
		SelectionHistory history = new SelectionHistory(resource);
		list.remove(history);
		list.add(history);

		try
		{
			userPreferenceService.setPreference(UserPreferenceService.RECENT_SELECTIONS,
				mapper.writeValueAsString(mapper.convertValue(list.toArray(), ArrayNode.class)));
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private List<SelectionHistory> getAllSavedRecentSelections(ObjectMapper mapper)
	{
		List<SelectionHistory> recentSel = new ArrayList<SelectionHistory>();
		final String data = userPreferenceService.getPreference(UserPreferenceService.RECENT_SELECTIONS);
		if( data != null )
		{
			try
			{
				recentSel.addAll(Lists.newArrayList(mapper.readValue(data, SelectionHistory[].class)));
			}
			catch( Exception e )
			{
				// Unfortunately we saved SelectedResource objects which should
				// be private
				try
				{
					ArrayNode array = (ArrayNode) mapper.readTree(data);
					for( JsonNode obj : array )
					{
						if( obj.isObject() )
						{
							recentSel.add(new SelectionHistory((ObjectNode) obj));
						}
					}
				}
				catch( Exception e2 )
				{
					throw Throwables.propagate(e2);
				}
			}

			// Removes deleted items every time its viewed/added to
			List<String> uuids = new ArrayList<String>(recentSel.size());

			for( SelectionHistory sh : recentSel )
			{
				uuids.add(sh.getUuid());
			}

			if( !uuids.isEmpty() )
			{
				List<SelectionHistory> sel = new ArrayList<SelectionHistory>();
				List<Item> itemsByUuids = itemService.queryItemsByUuids(uuids);

				for( int i = 0; i < itemsByUuids.size(); i++ )
				{
					if( itemsByUuids.get(i) != null )
					{
						sel.add(recentSel.get(i));
					}
				}
				recentSel = sel;
			}
		}
		return recentSel;
	}

	private boolean isAlwaysLatest()
	{
		final VersionSelection versionSelection = configurationService
			.getProperties(new QuickContributeAndVersionSettings()).getVersionSelection();
		if( versionSelection != null )
		{
			return versionSelection == VersionSelection.FORCE_LATEST
				|| versionSelection == VersionSelection.DEFAULT_TO_LATEST;
		}
		return false;
	}

	@Override
	public SelectedResource createAttachmentSelection(SectionInfo info, ItemKey itemId, IAttachment attachment,
		TargetFolder folder, @Nullable String extensionType)
	{
		SelectedResource selectedResource = new SelectedResource(itemId, attachment, folder, extensionType);
		selectedResource.setLatest(isAlwaysLatest());
		return selectedResource;
	}

	@Override
	public void addSelectedPath(SectionInfo info, ItemKey itemId, String resource, String title, String description,
		TargetFolder folder, @Nullable String extensionType)
	{
		SelectedResource selectedResource = new SelectedResource(itemId, folder, extensionType);
		selectedResource.setUrl(resource);
		selectedResource.setTitle(title);
		selectedResource.setDescription(description);
		selectedResource.setType(SelectedResource.TYPE_PATH);
		selectedResource.setLatest(isAlwaysLatest());
		addSelectedResource(info, selectedResource, true);
	}

	@Override
	public void addSelectedItem(SectionInfo info, IItem<?> item, TargetFolder folder, @Nullable String extensionType)
	{
		addSelectedPath(info, item, "", folder, extensionType);
	}

	@Override
	public void addSelectedPath(SectionInfo info, IItem<?> item, String resource, TargetFolder folder,
		@Nullable String extensionType)
	{
		addSelectedPath(info, item.getItemId(), resource, CurrentLocale.get(item.getName(), item.getUuid()),
			CurrentLocale.get(item.getDescription(), ""), folder, extensionType);
	}

	@Override
	public SelectedResource createItemSelection(SectionInfo info, IItem<?> item, TargetFolder folder,
		@Nullable String extensionType)
	{
		SelectedResource selectedResource = new SelectedResource(item.getItemId(), folder, extensionType);
		selectedResource.setUrl("");
		selectedResource.setTitle(CurrentLocale.get(item.getName(), item.getUuid()));
		selectedResource.setDescription(CurrentLocale.get(item.getDescription(), ""));
		selectedResource.setType(SelectedResource.TYPE_PATH);
		selectedResource.setLatest(isAlwaysLatest());
		return selectedResource;
	}

	@Override
	public SelectedResource createItemSelection(SectionInfo info, IItem<?> item, @Nullable String extensionType)
	{
		return createItemSelection(info, item, null, extensionType);
	}

	@Override
	public List<BaseEntityLabel> filterEntities(List<BaseEntityLabel> labels, Long[] entityIds)
	{
		Set<Long> ids = new HashSet<Long>(Arrays.asList(entityIds));
		for( Iterator<BaseEntityLabel> iter = labels.iterator(); iter.hasNext(); )
		{
			if( !ids.contains(iter.next().getId()) )
			{
				iter.remove();
			}
		}
		return labels;
	}

	@Override
	public List<? extends BaseEntity> filterFullEntities(List<? extends BaseEntity> entities, Set<String> ids)
	{
		for( Iterator<? extends BaseEntity> iter = entities.iterator(); iter.hasNext(); )
		{
			if( !ids.contains(iter.next().getUuid()) )
			{
				iter.remove();
			}
		}
		return entities;
	}

	@Override
	public void forwardToNewSession(SectionInfo info, SelectionSession session, SelectableInterface selectable)
	{
		SectionInfo forward = selectable.createSectionInfo(info, session);
		setupSelectionSession(forward, session);
		info.forwardAsBookmark(forward);
	}

	@Override
	public SelectableInterface getNamedSelectable(String selectableName)
	{
		Extension extension = selectables.getExtension(selectableName);
		if( extension != null )
		{
			SelectableInterface selectable = selectables.getBeanByExtension(extension);
			if( selectable != null )
			{
				return selectable;
			}
		}
		throw new SectionsRuntimeException("No such such selectable: '" + selectableName + "'");
	}

	@Override
	public void forwardToSelectable(SectionInfo info, @Nullable SelectableInterface selectable)
	{
		SelectionSession session = getCurrentSession(info);
		if( selectable == null && session != null )
		{
			String homeSelectable = session.getHomeSelectable();
			if( homeSelectable == null )
			{
				throw new SectionsRuntimeException("No home selectable, but trying to forward to home");
			}

			selectable = getNamedSelectable(homeSelectable);
		}
		if( selectable == null )
		{
			throw new SectionsRuntimeException("No home selectable, neither from caller nor session");
		}
		SectionInfo forward = selectable.createSectionInfo(info, session);
		info.forwardAsBookmark(forward);
	}

	@Override
	public SectionInfo getSelectionSessionForward(SectionInfo info, SelectionSession session,
		SelectableInterface selection)
	{
		SectionInfo forward = selection.createSectionInfo(info, session);
		setupSelectionSession(forward, session);
		return forward;
	}

	@Override
	public void setupSelectionSession(SectionInfo info, SelectionSession session)
	{
		doSetupModalSession(info, session, RootSelectionSection.class, SelectionSession.class);
	}

	@Override
	public JSCallable getSelectFunction(SectionInfo info, String functionName, SelectedResource sampleResource)
	{
		SelectionSummarySection section = info.lookupSection(SelectionSummarySection.class);
		return section.getSelectFunction(info, functionName, sampleResource);

	}

	private static final Comparator<SelectionHistory> DATE_COMPARATOR = new Comparator<SelectionHistory>()
	{
		@Override
		public int compare(SelectionHistory first, SelectionHistory second)
		{
			return Long.valueOf(second.getSelectedDate()).compareTo(first.getSelectedDate());
		}
	};

	@Override
	public ViewableResource createViewableResource(SectionInfo info, SelectedResource resource)
	{
		ViewableItem vitem;
		// FIXME: these are both real item functions...
		if( resource.isPreviewResource() )
		{
			vitem = viewableFactory.createPreviewItem(resource.getUuid(), resource.getStagingId());
		}
		else
		{
			vitem = viewableFactory.createNewViewableItem(resource.createItemId(), resource.isLatest());
		}
		switch( resource.getType() )
		{
			case SelectedResource.TYPE_ATTACHMENT:
				Attachment attachment = (Attachment) UnmodifiableAttachments
					.convertToMapUuid(vitem.getItem().getAttachments()).get(resource.getAttachmentUuid());
				return attachmentResourceService.getViewableResource(info, vitem, attachment);
			case SelectedResource.TYPE_FILE:
				FileAttachment fakeFile = new FileAttachment();
				fakeFile.setFilename(resource.getUrl());
				return attachmentResourceService.getViewableResource(info, vitem, fakeFile);
			case SelectedResource.TYPE_PATH:
				return attachmentResourceService.createPathResource(info, vitem, resource.getUrl(), null);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeSelectedResource(SectionInfo info, SelectedResourceKey key)
	{
		final SelectionSession ss = getCurrentSession(info);
		final String folderId = key.getFolderId();
		final TargetFolder folder;
		if( folderId != null )
		{
			folder = findTargetFolder(info, folderId);
		}
		else
		{
			folder = ss.getStructure();
		}
		folder.removeResource(key);
	}

	@Override
	public boolean canSelectAttachment(SectionInfo info, ViewableItem<?> vitem, @Nullable String attachmentUuid)
	{
		final SelectionSession session = getCurrentSession(info);
		if( session != null && !session.getStructure().isNoTargets() )
		{
			for( SelectableAttachment selectableAtt : selectableAttachments.getBeanList() )
			{
				if( !selectableAtt.isAttachmentSelectable(info, vitem.getItem(), attachmentUuid) )
				{
					return false;
				}
			}
			// Also has to be live
			return checkStatus(vitem, false, false) && session.isSelectAttachments();
		}
		return false;
	}

	@Override
	public boolean canSelectItem(SectionInfo info, ViewableItem<?> vitem)
	{
		SelectionSession session = getCurrentSession(info);
		if( session != null )
		{
			// Is it local, if so, is it live and can we download it?
			if( vitem.getItemExtensionType() == null )
			{
				if( !checkStatus(vitem, session.isSelectScrapbook(), session.isSelectDraft()) )
				{
					return false;
				}

				if( session.isUseDownloadPrivilege() )
				{
					if( !vitem.getPrivileges().contains("DOWNLOAD_ITEM") )
					{
						return false;
					}
				}
			}

			return session.isSelectItem();
		}
		return false;
	}

	private boolean checkStatus(ViewableItem<?> vitem, boolean selectScrapbook, boolean selectDraft)
	{
		if( vitem.getItemExtensionType() == null )
		{
			final WorkflowStatus workflowStatus = vitem.getWorkflowStatus();
			if( workflowStatus != null )
			{
				final ItemStatus status = workflowStatus.getStatusName();
				return status == ItemStatus.LIVE || (status == ItemStatus.PERSONAL && selectScrapbook)
					|| (status == ItemStatus.DRAFT && selectDraft);
			}
		}
		return true;
	}

	@Override
	public boolean isSelected(SectionInfo info, ItemKey itemId, @Nullable String attachmentUuid,
		@Nullable String extensionType, boolean anywhere)
	{
		SelectionSession session = getCurrentSession(info);
		if( session == null )
		{
			return false;
		}

		return session.containsResource(attachmentUuid == null ? new SelectedResourceKey(itemId, extensionType)
			: new SelectedResourceKey(itemId, attachmentUuid, extensionType), anywhere);
	}

	@Override
	public TargetFolder findTargetFolder(SectionInfo info, @Nullable String folderId)
	{
		final SelectionSession session = getCurrentSession(info);
		if( session == null )
		{
			return null;
		}
		if( folderId == null )
		{
			return session.getStructure();
		}
		return session.getStructure().getFolder(folderId);
	}

	@Override
	protected SectionTree createFilterTree()
	{
		return selectionTree;
	}

	@Nullable
	@Override
	public JSCallable getSelectItemFunction(SectionInfo info, ViewableItem<?> vitem)
	{
		if( canSelectItem(info, vitem) )
		{
			ItemSelectorEvent event = new ItemSelectorEvent(vitem.getItemId(), getCurrentSession(info));
			info.processEvent(event);
			return event.getFunction();
		}
		return null;
	}

	@Nullable
	@Override
	public JSCallable getSelectAttachmentFunction(SectionInfo info, ViewableItem<?> vitem,
		@Nullable String attachmentUuid)
	{
		if( canSelectAttachment(info, vitem, attachmentUuid) )
		{
			AttachmentSelectorEvent event = new AttachmentSelectorEvent(vitem.getItemId(), getCurrentSession(info));
			info.processEvent(event);
			return event.getFunction();
		}
		return null;
	}

	@Nullable
	@Override
	public JSCallable getSelectAttachmentFunction(SectionInfo info, ViewableItem<?> vitem)
	{
		return getSelectAttachmentFunction(info, vitem, null);
	}

	@Nullable
	@Override
	public SelectAttachmentHandler getSelectAttachmentHandler(SectionInfo info, ViewableItem<?> vitem,
		@Nullable String attachmentUuid)
	{
		if( canSelectAttachment(info, vitem, attachmentUuid) )
		{
			AttachmentSelectorEvent event = new AttachmentSelectorEvent(vitem.getItemId(), getCurrentSession(info));
			info.processEvent(event);
			return event.getHandler();
		}
		return null;
	}

	@Nullable
	@Override
	public JSCallable getSelectPackageFunction(SectionInfo info, ViewableItem<?> vitem)
	{
		if( allowPackageSelection(info, vitem) )
		{
			PackageSelectorEvent event = new PackageSelectorEvent(vitem.getItem(), getCurrentSession(info));
			info.processEvent(event);
			return event.getFunction();
		}
		return null;
	}

	@Nullable
	@Override
	public JSCallable getSelectAllAttachmentsFunction(SectionInfo info, ViewableItem<?> vitem)
	{
		if( canSelectAttachment(info, vitem, null) )
		{
			AllAttachmentsSelectorEvent event = new AllAttachmentsSelectorEvent(vitem.getItemId(),
				getCurrentSession(info));
			info.processEvent(event);
			return event.getFunction();
		}
		return null;
	}

	private boolean allowPackageSelection(SectionInfo info, ViewableItem<?> vitem)
	{
		final SelectionSession session = getCurrentSession(info);
		if( session == null )
		{
			return false;
		}
		return checkStatus(vitem, session.isSelectScrapbook(), session.isSelectDraft()) && session.isSelectPackage();
	}
}
