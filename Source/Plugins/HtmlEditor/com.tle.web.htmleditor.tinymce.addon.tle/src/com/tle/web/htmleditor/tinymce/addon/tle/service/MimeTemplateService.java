package com.tle.web.htmleditor.tinymce.addon.tle.service;

import java.util.Collection;

import com.tle.beans.mime.MimeEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author aholland
 */
public interface MimeTemplateService
{
	String getTemplateForMimeType(String mime);

	String getTemplateForMimeEntry(MimeEntry mimeEntry);

	String getPopulatedTemplate(SectionInfo info, ViewableResource vres, String title);

	Collection<String> getEmbeddableMimeTypes();
}
