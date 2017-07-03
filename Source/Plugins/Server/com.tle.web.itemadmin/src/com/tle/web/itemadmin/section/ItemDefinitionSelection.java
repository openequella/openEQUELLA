package com.tle.web.itemadmin.section;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
public interface ItemDefinitionSelection extends SectionId
{
	ItemDefinition getCollection(SectionInfo info);

	void setCollection(SectionInfo info, ItemDefinition collection);
}
