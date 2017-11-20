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

package com.tle.web.wizard;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.web.sections.SectionInfo;

/**
 * Web specific parts of WizardService that have been ripped out
 * 
 * @author aholland
 */
public interface WebWizardService
{
	void forwardToViewItem(SectionInfo info, WizardState state);

	/**
	 * @param info
	 * @param collectionUuid
	 * @param initialXml Optional. If none is supplied the current selection
	 *            session will be checked for XML.
	 * @param staging Optional. If none is supplied a new staging area will be
	 *            created.
	 * @param cancellable If false then no 'cancel' link will be rendered. (Not
	 *            sure of the use for this...)
	 * @return The info used for the forward (note: this info has already been
	 *         forwarded to)
	 */
	void forwardToNewItemWizard(SectionInfo info, String collectionUuid, PropBagEx initialXml, StagingFile staging,
		boolean cancellable);

	/**
	 * Same as forwardToNewItemWizard(SectionInfo, String, PropBagEx,
	 * StagingFile, boolean) only the forward is not done for you. This is used
	 * in SelectionService.getSelectableForward().
	 * 
	 * @param info
	 * @param collectionUuid
	 * @param initialXml
	 * @param staging
	 * @param cancellable
	 * @param dontForward
	 * @return
	 */
	SectionInfo getNewItemWizardForward(SectionInfo info, String collectionUuid, PropBagEx initialXml,
		StagingFile staging, boolean cancellable);

	void forwardToLoadItemWizard(SectionInfo info, String itemUuid, int itemVersion, boolean edit, boolean redraft,
		boolean newVersion);

	void forwardToLoadWizard(SectionInfo info, String wizardUuid);

	void forwardToCloneItemWizard(SectionInfo info, String newCollectionUuid, String itemUuid, int itemVersion,
		String transform, boolean move, boolean cloneAttachments);
}
