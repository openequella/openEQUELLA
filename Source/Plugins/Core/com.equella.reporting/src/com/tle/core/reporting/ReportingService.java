/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.reporting;

import com.tle.beans.entity.report.Report;
import com.tle.common.reporting.RemoteReportingService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.web.sections.SectionInfo;
import java.util.List;
import java.util.Map;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;

/** @author Nicholas Read */
public interface ReportingService
    extends AbstractEntityService<EntityEditingBean, Report>, RemoteReportingService {
  public static final String DIR_DESIGN = "reportFiles";

  // @SecureOnReturn(priv = ReportPrivileges.EXECUTE_REPORT)
  List<Report> enumerateExecutable();

  // @SecureOnCall(priv = ReportPrivileges.EXECUTE_REPORT)
  String executeReport(
      SectionInfo info,
      Report report,
      String designFile,
      String format,
      IHTMLActionHandler actionHandler,
      Map<String, String[]> parameters,
      Map<String, String[]> parameterDisplayText,
      boolean forceExecution);

  String findDesignFile(Report report, String filename);

  Report getReportForFilename(String filename);

  IGetParameterDefinitionTask createReportParametersTask(Report report, String designFile);
}
