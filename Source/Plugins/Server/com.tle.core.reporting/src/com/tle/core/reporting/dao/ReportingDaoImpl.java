/*
 * Created on Oct 26, 2005
 */
package com.tle.core.reporting.dao;

import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.entity.report.Report;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentInstitution;

@Bind(ReportingDao.class)
@Singleton
public class ReportingDaoImpl extends AbstractEntityDaoImpl<Report> implements ReportingDao
{
	public ReportingDaoImpl()
	{
		super(Report.class);
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	public Report findByReportFilename(String filename)
	{
		int folder = filename.lastIndexOf('/');
		if( folder != -1 )
		{
			filename = filename.substring(folder + 1);
		}
		List<Report> reportsByName = getHibernateTemplate().findByNamedParam(
			"from Report where (filename = :filename or filename like :filelike) and institution = :inst",
			new String[]{"filename", "filelike", "inst"},
			new Object[]{filename, "%/" + filename, CurrentInstitution.get()});
		if( reportsByName.isEmpty() )
		{
			return null;
		}
		return reportsByName.iterator().next();
	}
}
