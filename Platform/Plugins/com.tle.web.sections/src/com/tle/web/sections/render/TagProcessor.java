package com.tle.web.sections.render;

import java.util.Map;

import com.tle.web.sections.SectionWriter;

public interface TagProcessor extends PreRenderable
{
	void processAttributes(SectionWriter writer, Map<String, String> attrs);
}
