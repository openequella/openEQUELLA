/*
 * Created on 18/04/2006
 */
package com.tle.common.reporting;

import java.io.IOException;
import java.util.List;

import com.tle.beans.entity.report.Report;
import com.tle.core.remoting.RemoteAbstractEntityService;

public interface RemoteReportingService extends RemoteAbstractEntityService<Report>
{
	String ENTITY_TYPE = "REPORT"; //$NON-NLS-1$

	List<String> getReportDesignFiles(String stagingId);

	void processReportDesign(String stagingId, String filename) throws IOException;

	String prepareDownload(Report report, String stagingId, String filename);

	void cleanDownload(String stagingId);
}
