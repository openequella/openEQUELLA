/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.reporting.web;

import com.tle.beans.entity.report.Report;
import com.tle.core.reporting.birttypes.AbstractBirtType;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.wizard.page.ControlResult;
import com.tle.web.wizard.page.WizardPage;
import java.util.List;
import java.util.Map;

public class ReportingForm {
  @Bookmarked private String reportUuid;
  @Bookmarked private boolean forceParams;

  @Bookmarked(parameter = "df")
  private String designFile;

  @Bookmarked(name = "r")
  private String generatedReportId;

  private boolean parametersValid;
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

  public Report getReport() {
    return reportCached.getCachedValue();
  }

  public List<ControlResult> getWizard() {
    return wizard;
  }

  public void setWizard(List<ControlResult> wizard) {
    this.wizard = wizard;
  }

  public String getReportUuid() {
    return reportUuid;
  }

  public void setReportUuid(String reportUuid) {
    this.reportUuid = reportUuid;
  }

  public Map<String, String[]> getParameters() {
    return parameters;
  }

  public Map<String, String[]> getParameterDisplayTexts() {
    return parameterDisplayTexts;
  }

  public void setParameters(Map<String, String[]> parameters) {
    this.parameters = parameters;
  }

  public void setParameterDisplayTexts(Map<String, String[]> parameterDisplayTexts) {
    this.parameterDisplayTexts = parameterDisplayTexts;
  }

  public CachedData<Report> getReportCached() {
    return reportCached;
  }

  public CachedData<WizardPage> getWizardPage() {
    return wizardPage;
  }

  public boolean isForceParams() {
    return forceParams;
  }

  public void setForceParams(boolean forceParams) {
    this.forceParams = forceParams;
  }

  public String getReportUrl() {
    return reportUrl;
  }

  public void setReportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
  }

  public boolean isShowReport() {
    return showReport;
  }

  public void setShowReport(boolean showReport) {
    this.showReport = showReport;
  }

  public List<AbstractBirtType> getParamControls() {
    return paramControls;
  }

  public void setParamControls(List<AbstractBirtType> paramControls) {
    this.paramControls = paramControls;
  }

  public boolean isContainsParameters() {
    return containsParameters;
  }

  public void setContainsParameters(boolean containsParameters) {
    this.containsParameters = containsParameters;
  }

  public String getGeneratedReportId() {
    return generatedReportId;
  }

  public void setGeneratedReportId(String generatedReportId) {
    this.generatedReportId = generatedReportId;
  }

  public String getDesignFile() {
    return designFile;
  }

  public void setDesignFile(String designFile) {
    this.designFile = designFile;
  }

  public boolean hasReports() {
    return reports;
  }

  public void setReports(boolean reports) {
    this.reports = reports;
  }

  public boolean isHasGroups() {
    return hasGroups;
  }

  public void setHasGroups(boolean hasGroups) {
    this.hasGroups = hasGroups;
  }

  public boolean isParametersValid() {
    return parametersValid;
  }

  public void setParametersValid(boolean parametersValid) {
    this.parametersValid = parametersValid;
  }

  public boolean isShowWizard() {
    return !parametersValid || forceParams;
  }
}
