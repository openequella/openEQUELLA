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

package com.tle.mets.importerexporters;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;

import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;

public interface MetsAttachmentImporterExporter
{
	boolean canExport(Item item, Attachment attachment);

	boolean canImport(File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode);

	List<MetsIDElementInfo<? extends MetsIDElement>> export(SectionInfo info, Item item, Attachment attachment);

	void doImport(Item item, FileHandle staging, String packageFolder, File parentElem, MetsElement elem,
		PropBagEx xmlData, ItemNavigationNode parentNode, AttachmentAdder attachmentAdder);
}
