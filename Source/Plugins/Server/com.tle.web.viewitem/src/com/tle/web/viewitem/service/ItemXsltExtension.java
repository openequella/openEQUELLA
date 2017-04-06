package com.tle.web.viewitem.service;

import com.dytech.devlib.PropBagEx;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.PreRenderable;

public interface ItemXsltExtension extends PreRenderable
{
	void addXml(PropBagEx xml, SectionInfo info);

}
