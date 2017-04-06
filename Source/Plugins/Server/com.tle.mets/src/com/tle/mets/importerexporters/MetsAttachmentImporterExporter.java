package com.tle.mets.importerexporters;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.filesystem.FileHandle;
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
