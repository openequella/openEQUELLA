/*
 * Created on Oct 26, 2005
 */
package com.tle.core.reporting.dao;

import com.tle.beans.entity.report.Report;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author Nicholas Read
 */
public interface ReportingDao extends AbstractEntityDao<Report>
{
	Report findByReportFilename(String filename);
}
