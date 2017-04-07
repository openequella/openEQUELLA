package com.tle.web.sections.convert;

import net.entropysoft.transmorph.IConverter;
import net.entropysoft.transmorph.type.TypeReference;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.util.PropertyAccessor;

public interface SectionsConverter extends IConverter
{
	boolean supports(String convertId);

	void registerBookmark(SectionTree tree, SectionId sectionId, PropertyAccessor readAccessor,
		PropertyAccessor writeAccessor, TypeReference<?> typeRef);
}
