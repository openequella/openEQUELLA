package com.tle.web.inplaceeditor.service;

import com.tle.beans.item.ItemId;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSStatements;

/**
 * @author Aaron
 */
public interface InPlaceEditorWebService
{
	/**
	 * @param divSelector The div surrounding the two links and applet
	 * @param openWithLinkSelector The 'open with another editor' link
	 * @return
	 */
	JSStatements createHideLinksStatements(JQuerySelector divSelector, JQuerySelector openWithLinkSelector);

	/**
	 * @param appletId
	 * @param itemId
	 * @param stagingId
	 * @param filename
	 * @param openWith
	 * @param service
	 * @param divSelector
	 * @param height
	 * @param width
	 * @return
	 */
	JSCallable createAppletFunction(String appletId, ItemId itemId, String stagingId, String filename,
		boolean openWith, String service, JQuerySelector divSelector, String width, String height);

	/**
	 * @param appletId
	 * @param openWith
	 * @param noAppletCallback A function to load the applet if it is not found
	 * @return
	 */
	JSHandler createOpenHandler(String appletId, boolean openWith, JSFunction noAppletCallback);

	/**
	 * @param appletId
	 * @param doneUploadingCallback A function that is performed when the applet
	 *            is done uploading or has no changes to upload
	 * @return
	 */
	JSHandler createUploadHandler(String appletId, JSFunction doneUploadingCallback);
}
