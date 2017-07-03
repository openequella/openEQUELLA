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

package com.tle.core.item.serializer.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.item.DrmSettings.Party;
import com.tle.beans.item.DrmSettings.Usage;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.Triple;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.DRMEditor;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.edit.NavigationEditor;
import com.tle.core.item.edit.NavigationNodeEditor;
import com.tle.core.item.serializer.AttachmentSerializer;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.api.item.interfaces.beans.DrmBean;
import com.tle.web.api.item.interfaces.beans.DrmNetworkBean;
import com.tle.web.api.item.interfaces.beans.DrmOptionsBean;
import com.tle.web.api.item.interfaces.beans.DrmOptionsBean.DrmRestrictionBean;
import com.tle.web.api.item.interfaces.beans.DrmPartyBean;
import com.tle.web.api.item.interfaces.beans.NavigationNodeBean;
import com.tle.web.api.item.interfaces.beans.NavigationTabBean;
import com.tle.web.api.item.interfaces.beans.NavigationTreeBean;

@Bind
@Singleton
public class StandardDeserializerEditor implements ItemDeserializerEditor
{
	@Inject
	private PluginTracker<AttachmentSerializer> attachmentDeserializers;

	@Override
	public void edit(EquellaItemBean itemBean, ItemEditor editor, boolean importing)
	{
		if( importing )
		{
			editor.editDates(itemBean.getCreatedDate(), itemBean.getModifiedDate());
			editor.editItemStatus(itemBean.getStatus());
			editor.processExportDetails(itemBean);
		}

		String metadata = itemBean.getMetadata();
		if( metadata != null )
		{
			editor.editMetadata(metadata);
		}
		UserBean owner = itemBean.getOwner();
		if( owner != null )
		{
			editor.editOwner(owner.getId());
		}
		editor.editThumbnail(itemBean.getThumbnail());

		if( itemBean.getCollaborators() != null )
		{
			Set<String> collabIds = Sets.newHashSet();
			for( int i = 0; i < itemBean.getCollaborators().size(); ++i )
			{
				collabIds.add(itemBean.getCollaborators().get(i).getId());
			}
			editor.editCollaborators(collabIds);
		}
		Float rating = itemBean.getRating();
		if( rating != null )
		{
			editor.editRating(rating);
		}
		List<AttachmentBean> attachments = itemBean.getAttachments();
		if( attachments != null )
		{
			Map<String, AttachmentSerializer> deserializerMap = attachmentDeserializers.getBeanMap();
			List<String> attachmentUuids = Lists.newArrayList();
			for( AttachmentBean attachmentBean : attachments )
			{
				EquellaAttachmentBean equellaAttachmentBean = (EquellaAttachmentBean) attachmentBean;
				AttachmentSerializer attachmentDeserializer = deserializerMap
					.get(attachmentBean.getRawAttachmentType());
				attachmentUuids.add(attachmentDeserializer.deserialize(equellaAttachmentBean, editor));
			}
			editor.editAttachmentOrder(attachmentUuids);
		}
		NavigationTreeBean navigation = itemBean.getNavigation();
		if( navigation != null )
		{
			NavigationEditor navEditor = editor.getNavigationEditor();
			navEditor.editManualNavigation(navigation.isHideUnreferencedAttachments());
			navEditor.editShowSplitOption(navigation.isShowSplitOption());
			List<NavigationNodeBean> rootNodes = navigation.getNodes();
			List<String> rootUUids = editNodes(navEditor, rootNodes);
			navEditor.editRootNodes(rootUUids);
		}

		final DrmBean drm = itemBean.getDrm();
		if( drm != null )
		{
			DRMEditor drmEditor = editor.getDRMEditor();
			DrmOptionsBean options = drm.getOptions();
			if( options != null )
			{
				drmEditor.editDrmPageUuid(options.getDrmPageUuid());
				drmEditor.editAllowSummary(options.isAllowSummary());
				drmEditor.editAttributionOfOwnership(options.isAttributionOfOwnership());
				drmEditor.editEnforceAttribution(options.isEnforceAttribution());
				drmEditor.editHideLicencesFromOwner(options.isHideLicencesFromOwner());
				drmEditor.editOwnerMustAccept(options.isOwnerMustAccept());
				drmEditor.editPreviewAllowed(options.isPreviewAllowed());
				drmEditor.editRequireAcceptanceFrom(options.getRequireAcceptanceFrom());
				drmEditor.editShowLicenceCount(options.isShowLicenceCount());
				drmEditor.editStudentsMustAcceptIfInCompilation(options.isStudentsMustAcceptIfInCompilation());
				drmEditor.editTermsOfAgreement(options.getTermsOfAgreement());
				drmEditor.editUsages(Lists.transform(options.getUsages(), new Function<String, Usage>()
				{
					@Override
					public Usage apply(String input)
					{
						return Usage.valueOf(input);
					}
				}));

				final List<DrmPartyBean> partyBeans = options.getContentOwners();
				if( partyBeans != null )
				{
					final List<Party> parties = Lists.transform(partyBeans, new Function<DrmPartyBean, Party>()
					{
						@Override
						public Party apply(DrmPartyBean input)
						{
							final Party party = new Party();
							party.setEmail(input.getEmail());
							party.setName(input.getName());
							party.setOwner(input.isOwner());
							party.setUserID(input.getUserId());
							return party;
						}
					});
					drmEditor.editContentOwners(parties);
				}
				else
				{
					drmEditor.editContentOwners(null);
				}

				final DrmRestrictionBean drmRestrictions = options.getRestriction();
				if( drmRestrictions != null )
				{
					drmEditor.editDateRange(drmRestrictions.getStartDate(), drmRestrictions.getEndDate());
					drmEditor.editUsersExpression(drmRestrictions.getUsersExpression());
					drmEditor.editEducationalSector(drmRestrictions.isEducationalSector());
					drmEditor.editMaximumUsage(drmRestrictions.getMaximumUsage());
					List<DrmNetworkBean> networks = drmRestrictions.getNetwork();
					if( networks != null )
					{
						final List<Triple<String, String, String>> networkTrips = Lists.transform(networks,
							new Function<DrmNetworkBean, Triple<String, String, String>>()
							{
								@Override
								public Triple<String, String, String> apply(DrmNetworkBean input)
								{
									return new Triple<String, String, String>(input.getName(), input.getStartAddress(),
										input.getEndAddress());
								}
							});
						drmEditor.editNetworks(networkTrips);
					}
					else
					{
						drmEditor.editNetworks(null);
					}
				}
				else
				{
					drmEditor.editDateRange(null, null);
					drmEditor.editUsersExpression(null);
					drmEditor.editEducationalSector(false);
					drmEditor.editMaximumUsage(0);
					drmEditor.editNetworks(null);
				}
			}
			else
			{
				drmEditor.remove();
			}
		}
	}

	private List<String> editNodes(NavigationEditor navEditor, List<NavigationNodeBean> rootNodes)
	{
		if( Check.isEmpty(rootNodes) )
		{
			return Collections.emptyList();
		}
		List<String> nodeUuids = Lists.newArrayList();
		for( NavigationNodeBean navNode : rootNodes )
		{
			NavigationNodeEditor editor = navEditor.getNavigationNodeEditor(navNode.getUuid());
			editor.editIcon(navNode.getIcon());
			editor.editName(navNode.getName());
			editor.editImsId(navNode.getImsId());
			List<NavigationTabBean> tabs = navNode.getTabs();
			int tabCount = 0;
			if( !Check.isEmpty(tabs) )
			{
				for( NavigationTabBean tab : tabs )
				{
					if( tab.getAttachment() != null )
					{
						editor.editTab(tabCount, tab.getName(), tab.getAttachment().getUuid(), tab.getViewer());
						tabCount++;
					}
				}
			}
			editor.editTabCount(tabCount);
			nodeUuids.add(editor.getUuid());
			List<String> childUuids = editNodes(navEditor, navNode.getNodes());
			editor.editChildrenList(childUuids);
		}
		return nodeUuids;
	}

	@Override
	public void processFiles(Item item, ItemEditor editor, boolean importing)
	{
		// Nothing
	}
}
