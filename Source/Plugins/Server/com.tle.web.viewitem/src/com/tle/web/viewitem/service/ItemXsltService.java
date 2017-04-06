package com.tle.web.viewitem.service;

import com.dytech.devlib.PropBagEx;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewurl.ItemSectionInfo;

public interface ItemXsltService
{
	PropBagEx getXmlForXslt(SectionInfo info, ItemSectionInfo itemInfo);

	String renderSimpleXsltResult(RenderContext info, ItemSectionInfo itemInfo, String xslt);
}
