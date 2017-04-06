package com.tle.core.item.edit;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.item.edit.attachment.AttachmentEditor;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

public interface ItemEditor
{
	/**
	 * Asks each ItemDeserializerEditor to do edits
	 * 
	 * @param itemBean
	 */
	void doEdits(EquellaItemBean itemBean);

	/**
	 * Also asks each ItemDeserializerEditor to do processFiles
	 * 
	 * @param ensureOnIndexList
	 * @return
	 */
	ItemIdKey finishedEditing(boolean ensureOnIndexList);

	PropBagEx getMetadata();

	void preventSaveScript();

	void editDates(Date dateCreated, Date dateModified);

	void editItemStatus(String status);

	void editOwner(String owner);

	void editCollaborators(Set<String> collaborators);

	void editRating(Float rating);

	void editMetadata(String xml);

	void editMetadata(PropBagEx xml);

	void editThumbnail(String thumbnail);

	<T extends AttachmentEditor> T getAttachmentEditor(String uuid, Class<T> type);

	void editAttachmentOrder(List<String> attachmentUuids);

	NavigationEditor getNavigationEditor();

	DRMEditor getDRMEditor();

	void unlock();

	FileHandle getFileHandle();

	boolean isNewItem();
}
