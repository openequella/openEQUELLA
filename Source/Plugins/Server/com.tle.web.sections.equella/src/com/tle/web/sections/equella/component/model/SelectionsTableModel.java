package com.tle.web.sections.equella.component.model;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface SelectionsTableModel
{
	List<SelectionsTableSelection> getSelections(SectionInfo info);
}
