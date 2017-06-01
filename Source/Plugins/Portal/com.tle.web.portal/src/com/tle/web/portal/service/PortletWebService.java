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

import java.util.Map;

import com.tle.common.portal.entity.Portlet;
import com.tle.web.portal.editor.PortletEditor;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
public interface PortletWebService
{
	boolean canCreate();

	boolean canAdminister();

	void newPortlet(SectionInfo info, String portletType, boolean admin);

	void editPortlet(SectionInfo info, String portletUuid, boolean admin);

	void returnFromEdit(SectionInfo info, boolean cancelled, String portletUuid, boolean institutional);

	/**
	 * Deletes the portlet if this is a user defined portlet, otherwise creates
	 * a preference to say that the portlet is closed.
	 * 
	 * @param info
	 * @param portletUuid
	 */
	void close(SectionInfo info, String portletUuid);

	/**
	 * Unlike close(SectionInfo, String, String), this will always delete the
	 * portlet (subject to permissions of course)
	 * 
	 * @param info
	 * @param portletUuid
	 */
	void delete(SectionInfo info, String portletUuid);

	/**
	 * Restores an institutional portlet that someone has previously decided to
	 * close
	 * 
	 * @param info
	 * @param portletUuid
	 */
	void restore(SectionInfo info, String portletUuid);

	/**
	 * Restores all institutional portlets that someone has previously decided
	 * to close
	 * 
	 * @param info
	 */
	void restoreAll(SectionInfo info);

	/**
	 * @param info
	 * @param portlet
	 * @param minimised
	 */
	void minimise(SectionInfo info, String portletUuid, boolean minimised);

	/**
	 * @param info
	 * @return
	 */
	SectionTree getPortletRendererTree(SectionInfo info);

	/**
	 * @param info
	 * @param tree Must have been created by buildRendererTree
	 * @param position PortletPreference.POSITION_LEFT or
	 *            PortletPreference.POSITION_RIGHT
	 * @return A combined result of all left portlets
	 */
	SectionRenderable renderPortlets(RenderContext info, SectionTree tree, int position);

	/**
	 * Determine if the user has any visible portlets. If not, then the 'welcome
	 * to EQUELLA screen will show'
	 * 
	 * @param info
	 * @param tree
	 * @return
	 */
	boolean hasPortlets(SectionInfo info, SectionTree tree);

	/**
	 * @param info
	 * @param prev
	 * @param toMove
	 * @param next
	 * @param position PortletPreference.POSITION_LEFT or
	 *            PortletPreference.POSITION_RIGHT (or 0 for auto)
	 */
	void move(SectionInfo info, Portlet prev, Portlet toMove, int position);

	// debugging only
	void clearPortletRendererCache(String userId);

	Map<String, PortletEditor> registerEditors(SectionTree tree, String parentId);
}
