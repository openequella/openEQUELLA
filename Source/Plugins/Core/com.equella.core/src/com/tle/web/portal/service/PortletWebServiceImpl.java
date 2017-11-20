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

package com.tle.web.portal.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.tle.beans.Institution;
import com.tle.common.Triple;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.portal.PortletConstants;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.events.UserSessionLogoutEvent;
import com.tle.core.events.listeners.UserSessionLogoutListener;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.portal.service.PortletService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.portal.editor.PortletEditor;
import com.tle.web.portal.events.PortletsUpdatedEvent;
import com.tle.web.portal.events.PortletsUpdatedEvent.PortletUpdateEventType;
import com.tle.web.portal.events.PortletsUpdatedEventListener;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.portal.renderer.PortletRendererWrapper;
import com.tle.web.portal.section.common.PortletContributionSection;
import com.tle.web.sections.RegistrationController;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionNode;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.DefaultSectionTree;
import com.tle.web.sections.render.SectionRenderable;

@Bind(PortletWebService.class)
@Singleton
public class PortletWebServiceImpl implements PortletWebService, PortletsUpdatedEventListener, UserSessionLogoutListener
{
	private static final String UUID_SECTIONID_MAP_KEY = "$UUID_SECTIONID_MAP$"; //$NON-NLS-1$
	private static final String TOP_PORTLETS_KEY = "$TOP_PORTLET_SECTION_IDS$"; //$NON-NLS-1$
	private static final String LEFT_PORTLETS_KEY = "$LEFT_PORTLET_SECTION_IDS$"; //$NON-NLS-1$
	private static final String RIGHT_PORTLETS_KEY = "$RIGHT_PORTLET_SECTION_IDS$"; //$NON-NLS-1$

	private final Cache<Long, Cache<String, DefaultSectionTree>> sectionCache = CacheBuilder.newBuilder()
		.expireAfterAccess(2, TimeUnit.DAYS).build();

	@Inject
	private Provider<PortletRendererWrapper> rendererFactory;
	@Inject
	private PortletService portletService;
	@Inject
	private EventService eventService;
	@Inject
	private RegistrationController controller;
	@Inject
	private TLEAclManager aclService;

	private PluginTracker<PortletEditor> editorTracker;
	private PluginTracker<PortletContentRenderer<?>> rendererTracker;

	private DefaultSectionTree getTree(String userId)
	{
		long institutionId = CurrentInstitution.get().getUniqueId();
		Cache<String, DefaultSectionTree> instMap = sectionCache.getIfPresent(institutionId);
		if( instMap == null )
		{
			instMap = CacheBuilder.newBuilder().softValues().expireAfterAccess(30, TimeUnit.MINUTES).build();
			sectionCache.put(institutionId, instMap);
		}
		return instMap.getIfPresent(userId);
	}

	private void clearTree(Institution inst, String userId)
	{
		if( userId == null )
		{
			sectionCache.invalidate(inst.getUniqueId());
		}
		else
		{
			Cache<String, DefaultSectionTree> instMap = sectionCache.getIfPresent(inst.getUniqueId());
			if( instMap != null )
			{
				instMap.invalidate(userId);
			}
		}
	}

	@Override
	public boolean canCreate()
	{
		return !CurrentUser.wasAutoLoggedIn() && !aclService
			.filterNonGrantedPrivileges(Arrays.asList(new String[]{PortletConstants.CREATE_PORTLET}), true).isEmpty();
	}

	@Override
	public boolean canAdminister()
	{
		return !aclService
			.filterNonGrantedPrivileges(Collections.singleton(PortletConstants.PRIV_ADMINISTER_PORTLETS), true)
			.isEmpty();
	}

	private PortletContentRenderer<?> getRendererForType(String type)
	{
		// Not bean map, need prototypes
		Extension extension = rendererTracker.getExtensionMap().get(type);
		if( extension == null )
		{
			throw new RuntimeApplicationException("Unsupported portlet rendering type " + type); //$NON-NLS-1$
		}

		return rendererTracker.getBeanByParameter(extension, "class"); //$NON-NLS-1$
	}

	@Override
	public void newPortlet(SectionInfo info, String portletType, boolean admin)
	{
		PortletContributionSection con = info.lookupSection(PortletContributionSection.class);
		con.createNew(info, portletType, admin);
	}

	@Override
	public void editPortlet(SectionInfo info, String portletUuid, boolean admin)
	{
		PortletContributionSection con = info.lookupSection(PortletContributionSection.class);
		Portlet portlet = portletService.getForEdit(portletUuid);
		con.startEdit(info, portletUuid, portlet.getType(), admin);
	}

	@Override
	public void returnFromEdit(SectionInfo info, boolean cancelled, String portletUuid, boolean institutional)
	{
		PortletContributionSection con = info.lookupSection(PortletContributionSection.class);
		con.editingFinished(info);

		if( !cancelled )
		{
			firePortletsChanged(info, CurrentUser.getUserID(), portletUuid, institutional,
				portletUuid == null ? PortletUpdateEventType.CREATED : PortletUpdateEventType.EDITED);
		}
	}

	@Override
	public void close(SectionInfo info, String portletUuid)
	{
		firePortletsChanged(info, CurrentUser.getUserID(), portletUuid, false, PortletUpdateEventType.REMOVED);
		portletService.close(portletUuid);
	}

	@Override
	public void delete(SectionInfo info, String portletUuid)
	{
		// remove this from all user's trees
		firePortletsChanged(info, null, portletUuid, false, PortletUpdateEventType.REMOVED);
		portletService.delete(portletService.getByUuid(portletUuid), true);
	}

	@Override
	public void restore(SectionInfo info, String portletUuid)
	{
		portletService.restore(portletUuid);
		firePortletsChanged(info, CurrentUser.getUserID(), portletUuid, false, PortletUpdateEventType.CREATED);
	}

	@Override
	public void restoreAll(SectionInfo info)
	{
		portletService.restoreAll();

		firePortletsChanged(info, CurrentUser.getUserID(), null, false, PortletUpdateEventType.CREATED);
	}

	@Override
	public void minimise(SectionInfo info, String portletUuid, boolean minimised)
	{
		final Portlet portlet = portletService.getByUuid(portletUuid);
		PortletPreference pref = portletService.getPreference(portlet);
		if( pref == null )
		{
			pref = new PortletPreference(portlet, CurrentUser.getUserID());
		}
		pref.setMinimised(minimised);
		portletService.savePreference(pref);
	}

	/*
	 * The initial idea was that there would only be a preference created for
	 * something you 'did' But then I said "bugger it" and then just create a
	 * preference for anything that doesn't have one as soon as you 'do'
	 * something (e.g. drag and drop a portlet)
	 */
	@Override
	public void move(SectionInfo info, Portlet prev, Portlet toMove, int position)
	{
		final String userId = CurrentUser.getUserID();
		final List<Portlet> portlets = portletService.getViewablePortlets();
		final Map<Portlet, PortletPreference> prefs = portletService.getPreferences(portlets);

		Entry<Portlet, PortletPreference> moveableEntry = null;
		Entry<Portlet, PortletPreference> prevEntry = null;

		// create a preference for anything that doesn't have one (also, find
		// the entry for the portlet we are moving)
		for( Entry<Portlet, PortletPreference> entry : prefs.entrySet() )
		{
			Portlet p = entry.getKey();
			if( p.equals(toMove) )
			{
				moveableEntry = entry;
			}
			else if( prev != null && p.equals(prev) )
			{
				prevEntry = entry;
			}

			if( entry.getValue() == null )
			{
				entry.setValue(new PortletPreference(p, userId));
			}
		}

		if( moveableEntry == null )
		{
			throw new Error("No preference for the portlet being moved!"); //$NON-NLS-1$
		}

		final Triple<List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>> columns = reorderColumns(
			null, prefs);

		final List<Entry<Portlet, PortletPreference>> top = columns.getFirst();
		final List<Entry<Portlet, PortletPreference>> left = columns.getSecond();
		final List<Entry<Portlet, PortletPreference>> right = columns.getThird();

		// pluck toMove out of the list and then insert into the correct list in
		// the appropriate position
		top.remove(moveableEntry);
		left.remove(moveableEntry);
		right.remove(moveableEntry);

		List<Entry<Portlet, PortletPreference>> insertList = null;

		switch( position )
		{
			case PortletPreference.POSITION_TOP:
				insertList = top;
				break;
			case PortletPreference.POSITION_LEFT:
				insertList = left;
				break;
			case PortletPreference.POSITION_RIGHT:
				insertList = right;
				break;
			default:
				insertList = top;
				break;
		}

		insertList.add(prevEntry == null ? 0 : insertList.indexOf(prevEntry) + 1, moveableEntry);
		moveableEntry.getValue().setPosition(position);

		// re-initialise the order numbers
		int newOrder = 0;
		for( Entry<Portlet, PortletPreference> entry : top )
		{
			entry.getValue().setOrder(newOrder++);
		}
		newOrder = 0;
		for( Entry<Portlet, PortletPreference> entry : left )
		{
			entry.getValue().setOrder(newOrder++);
		}
		newOrder = 0;
		for( Entry<Portlet, PortletPreference> entry : right )
		{
			entry.getValue().setOrder(newOrder++);
		}

		portletService.savePreferences(prefs.values());

		firePortletsChanged(info, userId, toMove.getUuid(), toMove.isInstitutional(), PortletUpdateEventType.MOVED);
	}

	@Override
	public SectionTree getPortletRendererTree(SectionInfo info)
	{
		String userId = CurrentUser.getUserID();
		DefaultSectionTree tree = getTree(userId);
		if( tree == null )
		{
			tree = buildRendererTree(info, userId);
			final Cache<String, DefaultSectionTree> instMap = sectionCache
				.getIfPresent(CurrentInstitution.get().getUniqueId());
			instMap.put(userId, tree);
		}
		return tree;
	}

	protected DefaultSectionTree buildRendererTree(SectionInfo info, String userId)
	{
		// IMPORTANT: This prefix is relied upon in portal.js
		final DefaultSectionTree renderTree = new DefaultSectionTree(controller, new SectionNode("p")); //$NON-NLS-1$
		final Map<Portlet, PortletPreference> prefs = portletService
			.getPreferences(portletService.getViewablePortletsForDisplay());
		final Map<String, SectionId> portletUuidToSectionId = new HashMap<String, SectionId>();

		for( Entry<Portlet, PortletPreference> entry : prefs.entrySet() )
		{
			final PortletPreference pref = entry.getValue();

			if( pref == null || !pref.isClosed() )
			{
				final Portlet portlet = entry.getKey();
				final String uuid = portlet.getUuid();

				final PortletContentRenderer<?> delegate = getRendererForType(portlet.getType());
				delegate.setPortlet(portlet);

				final PortletRendererWrapper wrapper = rendererFactory.get();
				wrapper.setPortletUuid(uuid);
				wrapper.setMinimised(pref == null ? false : pref.isMinimised());
				wrapper.setDelegate(delegate);

				renderTree.registerSections(wrapper, null);

				portletUuidToSectionId.put(uuid, wrapper);
			}
		}
		renderTree.setAttribute(UUID_SECTIONID_MAP_KEY, portletUuidToSectionId);

		reorderColumns(renderTree, prefs);

		renderTree.finished();

		return renderTree;
	}

	/**
	 * @param tree If not specified then you will get the two columns back in
	 *            the return result , otherwise you won't (saves on unneccessary
	 *            processing)
	 * @param prefs
	 * @return The left and right column preferences (only if tree not
	 *         specified)
	 */
	private Triple<List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>> reorderColumns(
		DefaultSectionTree tree, Map<Portlet, PortletPreference> prefs)
	{
		final List<Entry<Portlet, PortletPreference>> top = new ArrayList<Entry<Portlet, PortletPreference>>();
		final List<Entry<Portlet, PortletPreference>> left = new ArrayList<Entry<Portlet, PortletPreference>>();
		final List<Entry<Portlet, PortletPreference>> right = new ArrayList<Entry<Portlet, PortletPreference>>();

		for( Entry<Portlet, PortletPreference> entry : prefs.entrySet() )
		{
			PortletPreference pref = entry.getValue();
			if( pref != null && !pref.isClosed() )
			{
				switch( pref.getPosition() )
				{
					case PortletPreference.POSITION_TOP:
						top.add(entry);
						break;
					case PortletPreference.POSITION_LEFT:
						left.add(entry);
						break;
					case PortletPreference.POSITION_RIGHT:
						right.add(entry);
						break;
					default:
						top.add(entry);
						break;
				}
			}
			else if( pref == null ) // !pref.closed()
			{
				top.add(entry);
			}
		}

		Collections.sort(top, new PortletPrefComparator());
		Collections.sort(left, new PortletPrefComparator());
		Collections.sort(right, new PortletPrefComparator());

		if( tree != null )
		{
			final Map<String, SectionId> portletUuidToSectionId = tree.getAttribute(UUID_SECTIONID_MAP_KEY);
			tree.setAttribute(TOP_PORTLETS_KEY,
				Lists.transform(top, new PortletSectionIdTransform(portletUuidToSectionId)));
			tree.setAttribute(LEFT_PORTLETS_KEY,
				Lists.transform(left, new PortletSectionIdTransform(portletUuidToSectionId)));
			tree.setAttribute(RIGHT_PORTLETS_KEY,
				Lists.transform(right, new PortletSectionIdTransform(portletUuidToSectionId)));
		}

		return new Triple<List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>, List<Entry<Portlet, PortletPreference>>>(
			top, left, right);
	}

	@Override
	public SectionRenderable renderPortlets(RenderContext info, SectionTree tree, int position)
	{
		final String key;
		switch( position )
		{
			case PortletPreference.POSITION_TOP:
				key = TOP_PORTLETS_KEY;
				break;
			case PortletPreference.POSITION_LEFT:
				key = LEFT_PORTLETS_KEY;
				break;
			case PortletPreference.POSITION_RIGHT:
				key = RIGHT_PORTLETS_KEY;
				break;
			default:
				key = TOP_PORTLETS_KEY;
				break;
		}
		final List<SectionId> sectionIds = tree.getAttribute(key);
		return SectionUtils.renderSectionsCombined(info, sectionIds);
	}

	@Override
	public boolean hasPortlets(SectionInfo info, SectionTree tree)
	{
		final List<SectionId> topSectionIds = tree.getAttribute(TOP_PORTLETS_KEY);
		final List<SectionId> leftSectionIds = tree.getAttribute(LEFT_PORTLETS_KEY);
		final List<SectionId> rightSectionIds = tree.getAttribute(RIGHT_PORTLETS_KEY);
		return !topSectionIds.isEmpty() || !leftSectionIds.isEmpty() || !rightSectionIds.isEmpty();
	}

	@Override
	public void clearPortletRendererCache(String userId)
	{
		clearTree(CurrentInstitution.get(), userId);
	}

	protected void firePortletsChanged(SectionInfo info, String userId, String portletUuid, boolean institutional,
		PortletUpdateEventType eventType)
	{
		PortletsUpdatedEvent event = new PortletsUpdatedEvent(userId, portletUuid, institutional, eventType);
		portletsUpdated(event, info);

		eventService.publishApplicationEvent(event);
	}

	@Override
	public void portletsUpdated(PortletsUpdatedEvent event, SectionInfo thisInfo)
	{
		final PortletUpdateEventType type = event.getType();
		if( type == PortletUpdateEventType.MOVED )
		{
			// shuffle tree
			final DefaultSectionTree tree = getTree(event.getUserId());
			if( tree != null )
			{
				reorderColumns(tree, portletService.getPreferences(portletService.getViewablePortlets()));
			}
		}
		else
		{
			if( (type == PortletUpdateEventType.CREATED || type == PortletUpdateEventType.EDITED)
				&& event.isInstitutional() )
			{
				//Clear *everyone's* cache for modified institutional portlets
				clearTree(CurrentInstitution.get(), null);
			}
			else
			{
				// Clear this user's cache
				clearTree(CurrentInstitution.get(), event.getUserId());
			}
		}
	}

	@Override
	public void userSessionDestroyedEvent(UserSessionLogoutEvent event)
	{
		UserState userState = event.getUserState();
		clearTree(userState.getInstitution(), userState.getUserBean().getUniqueID());
	}

	protected static class PortletPrefComparator implements Comparator<Entry<Portlet, PortletPreference>>, Serializable
	{
		@Override
		public int compare(Entry<Portlet, PortletPreference> o1, Entry<Portlet, PortletPreference> o2)
		{
			PortletPreference pref1 = o1.getValue();
			PortletPreference pref2 = o2.getValue();
			if( pref1 == null )
			{
				return pref2 == null ? 0 : 100;
			}
			else
			{
				return pref2 == null ? -100 : pref1.getOrder() - pref2.getOrder();
			}
		}
	}

	private static class PortletSectionIdTransform implements Function<Entry<Portlet, PortletPreference>, SectionId>
	{
		private final Map<String, SectionId> portletUuidToSectionId;

		public PortletSectionIdTransform(Map<String, SectionId> portletUuidToSectionId)
		{
			this.portletUuidToSectionId = portletUuidToSectionId;
		}

		@Override
		public SectionId apply(Entry<Portlet, PortletPreference> entry)
		{
			return portletUuidToSectionId.get(entry.getKey().getUuid());
		}
	}

	@Inject
	@SuppressWarnings("nls")
	public void setPluginService(PluginService pluginService)
	{
		editorTracker = new PluginTracker<PortletEditor>(pluginService, "com.tle.web.portal", "portletEditor", "id");
		editorTracker.setBeanKey("class");

		rendererTracker = new PluginTracker<PortletContentRenderer<?>>(pluginService, "com.tle.web.portal",
			"portletRenderer", "id");
		rendererTracker.setBeanKey("class");
	}

	@Override
	public Map<String, PortletEditor> registerEditors(SectionTree tree, String parentId)
	{
		Map<String, PortletEditor> editors = editorTracker.getNewBeanMap();
		for( PortletEditor ed : editors.values() )
		{
			ed.register(tree, parentId);
		}
		return editors;
	}
}
