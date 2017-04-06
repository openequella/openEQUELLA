package com.tle.core.reporting.web;

import java.util.List;
import java.util.Map;

import com.tle.beans.entity.report.Report;
import com.tle.core.reporting.birttypes.AbstractBirtType;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.wizard.page.ControlResult;
import com.tle.web.wizard.page.WizardPage;

public class ReportingForm
{
	@Bookmarked
	private String reportUuid;
	@Bookmarked
	private boolean forceParams;
	@Bookmarked
	private boolean showErrors;
	@Bookmarked(parameter = "df")
	private String designFile;
	@Bookmarked(stateful = false)
	private boolean showWizard;
	@Bookmarked(name = "r")
	private String generatedReportId;

	private boolean showReport;
	private boolean containsParameters;
	private String reportUrl;
	private boolean hasGroups;

	private final CachedData<Report> reportCached = new CachedData<Report>();
	private final CachedData<WizardPage> wizardPage = new CachedData<WizardPage>();
	private List<ControlResult> wizard;
	private Map<String, String[]> parameters;
	private Map<String, String[]> parameterDisplayTexts;
	private List<AbstractBirtType> paramControls;
	private boolean reports;

	public Report getReport()
	{
		return reportCached.getCachedValue();
	}

	public List<ControlResult> getWizard()
	{
		return wizard;
	}

	public void setWizard(List<ControlResult> wizard)
	{
		this.wizard = wizard;
	}

	public String getReportUuid()
	{
		return reportUuid;
	}

	public void setReportUuid(String reportUuid)
	{
		this.reportUuid = reportUuid;
	}

	public Map<String, String[]> getParameters()
	{
		return parameters;
	}

	public Map<String, String[]> getParameterDisplayTexts()
	{
		return parameterDisplayTexts;
	}

	public void setParameters(Map<String, String[]> parameters)
	{
		this.parameters = parameters;
	}

	public void setParameterDisplayTexts(Map<String, String[]> parameterDisplayTexts)
	{
		this.parameterDisplayTexts = parameterDisplayTexts;
	}

	public CachedData<Report> getReportCached()
	{
		return reportCached;
	}

	public CachedData<WizardPage> getWizardPage()
	{
		return wizardPage;
	}

	public boolean isForceParams()
	{
		return forceParams;
	}

	public void setForceParams(boolean forceParams)
	{
		this.forceParams = forceParams;
	}

	public boolean isShowWizard()
	{
		return showWizard;
	}

	public void setShowWizard(boolean showWizard)
	{
		this.showWizard = showWizard;
	}

	public String getReportUrl()
	{
		return reportUrl;
	}

	public void setReportUrl(String reportUrl)
	{
		this.reportUrl = reportUrl;
	}

	public boolean isShowReport()
	{
		return showReport;
	}

	public void setShowReport(boolean showReport)
	{
		this.showReport = showReport;
	}

	public List<AbstractBirtType> getParamControls()
	{
		return paramControls;
	}

	public void setParamControls(List<AbstractBirtType> paramControls)
	{
		this.paramControls = paramControls;
	}

	public boolean isShowErrors()
	{
		return showErrors;
	}

	public void setShowErrors(boolean showErrors)
	{
		this.showErrors = showErrors;
	}

	public boolean isContainsParameters()
	{
		return containsParameters;
	}

	public void setContainsParameters(boolean containsParameters)
	{
		this.containsParameters = containsParameters;
	}

	public String getGeneratedReportId()
	{
		return generatedReportId;
	}

	public void setGeneratedReportId(String generatedReportId)
	{
		this.generatedReportId = generatedReportId;
	}

	public String getDesignFile()
	{
		return designFile;
	}

	public void setDesignFile(String designFile)
	{
		this.designFile = designFile;
	}

	public boolean hasReports()
	{
		return reports;
	}

	public void setReports(boolean reports)
	{
		this.reports = reports;
	}

	public boolean isHasGroups()
	{
		return hasGroups;
	}

	public void setHasGroups(boolean hasGroups)
	{
		this.hasGroups = hasGroups;
	}
}
