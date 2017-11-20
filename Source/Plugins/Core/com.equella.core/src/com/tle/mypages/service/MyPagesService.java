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

package com.tle.mypages.service;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.ScriptContext;
import com.google.common.base.Function;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.mycontent.service.MyContentFields;
import com.tle.mypages.web.MyPagesState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
public interface MyPagesService
{
	WorkflowOperation getEditOperation(MyContentFields fields, String filename, InputStream inputStream,
		boolean removeExistingAttachments, boolean useExistingAttachment);

	MyPagesState newItem(SectionInfo info, ItemDefinition itemDef);

	/**
	 * Load an existing item and start a new wizard state for editing
	 */
	MyPagesState loadItem(SectionInfo info, ItemId id);

	/**
	 * @param info
	 * @param tree
	 * @param sessionId
	 * @return false if the save failed for any reason
	 */
	boolean saveItem(SectionInfo info, SectionTree tree, String sessionId);

	WizardStateInterface getState(SectionInfo info, String sessionId);

	void updateSession(SectionInfo info, WizardStateInterface state);

	/**
	 * Add the given attachment to the state and uploads a blank page to the
	 * repository
	 */
	HtmlAttachment newPage(SectionInfo info, String sessionId, String pageName);

	/**
	 * Retrieves the text of the given attachment and converts it to draft urls.
	 * Also converts any urls which point to itemId, to point to preview urls.
	 */
	String getDraftHtml(WizardStateInterface state, SectionInfo info, HtmlAttachment attachment, ItemKey itemId);

	/**
	 * Stores the given text of the given attachment into the repository
	 */
	void setHtml(SectionInfo info, String sessionId, HtmlAttachment attachment, String html);

	FileAttachment uploadStream(SectionInfo info, String sessionId, String pageUuid, String filename,
		String description, InputStream input);

	void savePage(SectionInfo info, SectionTree tree, String sessionId, String pageUuid);

	void deletePage(SectionInfo info, String sessionId, String pageUuid);

	void deletePageFiles(SectionInfo info, String sessionId, HtmlAttachment attachment);

	/**
	 * @param sessionId Will use this in preference of the itemId
	 * @param itemId If sessionId is null (which it rarely is...) then uses this
	 * @param pageUuid The UUID of the page to get
	 * @return An existing attachment or null if the pageUuid is not found
	 */
	HtmlAttachment getPageAttachment(SectionInfo info, String sessionId, String itemId, String pageUuid);

	List<HtmlAttachment> getPageAttachments(SectionInfo info, String sessionId, String itemId);

	List<HtmlAttachment> getNonDeletedPageAttachments(SectionInfo info, String sessionId, String itemId);

	Attachments getAttachments(SectionInfo info, String sessionId, String itemId);

	ViewableResource cloneMyContent(SectionInfo info, ViewableResource vres, String sessionId, String pageUuid);

	/**
	 * @return The first available page to edit (taking into account
	 *         HtmlAttachment.isDelete())
	 */
	HtmlAttachment getFirstAvailablePage(SectionInfo info, String sessionId);

	/**
	 * If this is not the final page then get the next page. If it is the final
	 * page, get the previous page. If there are no other pages return null.
	 * 
	 * @param sessionId
	 * @param pageUuid
	 * @return
	 */
	HtmlAttachment findNextAvailablePage(SectionInfo info, String sessionId, String pageUuid);

	/**
	 * Commits HtmlAttachments that are in draft. Changes draft URLs into
	 * preview URLs OR item urls, dependent on the value of leaveAsPreview
	 */
	void commitDraft(SectionInfo info, String sessionId);

	/**
	 * Commits HtmlAttachments that are in draft. Changes draft URLs into
	 * preview URLs OR item urls, dependent on the value of leaveAsPreview
	 */
	void commitDraft(WizardStateInterface state, boolean leaveAsPreview, SectionInfo info);

	/**
	 * Goes through HtmlAttachments and if it is a draft item, delete its files
	 * and set it to non-draft
	 */
	void clearDraft(WizardStateInterface state);

	/**
	 * @see clearDraft(WizardStateInterface state);
	 */
	void clearDraft(SectionInfo info, String sessionId);

	/**
	 * Scans the html and removes all /preview/uuid/etc URLs and replaces them
	 * with /items/uuid/etc
	 * 
	 * @param state
	 */
	void convertPreviewUrlsToItemUrls(WizardStateInterface state);

	/**
	 * @param state
	 * @param sourceItem
	 * @param sourcePage
	 * @param draft Put the new page into a draft state
	 * @return
	 */
	HtmlAttachment clonePage(WizardStateInterface state, Item sourceItem, HtmlAttachment sourcePage, boolean draft);

	FileInfo saveHtml(FileHandle handle, String filename, String html);

	/**
	 * Does not close your Reader
	 * 
	 * @param handle
	 * @param filename
	 * @param html
	 * @return
	 * @throws Exception
	 */
	FileInfo saveHtml(FileHandle handle, String filename, Reader html);

	<T> T forFile(FileHandle handle, String filename, Function<Reader, T> withReader);

	/**
	 * Create a script context for HTML editor plugins mainly
	 * 
	 * @param state
	 * @return
	 */
	ScriptContext createScriptContext(WizardStateInterface state);

	void removeFromSession(SectionInfo info, String id);
}
