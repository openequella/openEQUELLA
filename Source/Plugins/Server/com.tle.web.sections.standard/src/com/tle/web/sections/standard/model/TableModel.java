package com.tle.web.sections.standard.model;

import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.TableState.TableRow;

/**
 * @author Aaron
 */
public interface TableModel
{
	List<TableRow> getRows(SectionInfo info);
}
