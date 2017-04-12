package com.tle.core.reporting;

import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;

import com.tle.beans.entity.report.Report;
import com.tle.common.reporting.RemoteReportingService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.web.sections.SectionInfo;

/**
 * @author Nicholas Read
 */
public interface ReportingService extends AbstractEntityService<EntityEditingBean, Report>, RemoteReportingService
{
	// @SecureOnReturn(priv = ReportPrivileges.EXECUTE_REPORT)
	List<Report> enumerateExecutable();

	// @SecureOnCall(priv = ReportPrivileges.EXECUTE_REPORT)
	String executeReport(SectionInfo info, Report report, String designFile, String format,
		IHTMLActionHandler actionHandler, Map<String, String[]> parameters, Map<String, String[]> parameterDisplayText,
		boolean forceExecution);

	String findDesignFile(Report report, String filename);

	Report getReportForFilename(String filename);

	IGetParameterDefinitionTask createReportParametersTask(Report report, String designFile);
}