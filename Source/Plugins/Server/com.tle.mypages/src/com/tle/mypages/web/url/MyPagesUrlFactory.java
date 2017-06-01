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

package com.tle.mypages.web.url;

import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author aholland
 */
public interface MyPagesUrlFactory
{
	/**
	 * @param info
	 * @param wizid The current wizard state id
	 * @param page The page to edit. May be null, in which case the first
	 *            available page is displayed.
	 * @param finishedCallback Only used in a modal situation. I.e. you call
	 *            this to close the window.
	 * @return
	 */
	MyPagesEditUrl createEditUrl(SectionInfo info, String wizid, HtmlAttachment page,
		JSCallAndReference finishedCallback);

	/**
	 * @param info
	 * @param vitem
	 * @param finishedCallback Only used in a modal situation. I.e. you call
	 *            this to close the window.
	 * @return
	 */
	ViewItemUrl createViewUrl(SectionInfo info, ViewableItem vitem);

	/**
	 * @param info
	 * @param vitem
	 * @param page Cannot be null
	 * @param finishedCallback Only used in a modal situation. I.e. you call
	 *            this to close the window.
	 * @return
	 */
	ViewItemUrl createViewUrl(SectionInfo info, ViewableItem vitem, HtmlAttachment page);
}
