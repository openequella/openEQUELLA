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

package com.tle.web.api.report;

import com.tle.beans.entity.report.Report;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.security.PrivilegeTree;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.ReportingService;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.staging.interfaces.StagingResource;
import com.tle.web.remoting.rest.service.UrlLinkService;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.Logger;

@SuppressWarnings("nls")
@Bind(ReportResource.class)
@Singleton
public class ReportResourceImpl
    extends AbstractBaseEntityResource<Report, BaseEntitySecurityBean, ReportBean>
    implements ReportResource {

  private static final Logger LOGGER = Logger.getLogger(ReportResourceImpl.class);

  @Inject private ReportingService reportingService;

  @Inject private ReportBeanSerializer reportSerializer;

  @Inject private StagingService stagingService;

  @Inject private FileSystemService fileSystemService;

  @Inject private UrlLinkService urlLinkService;

  @Override
  public Response create(UriInfo uriInfo, ReportBean bean, String staginguuid, String packagename) {
    LOGGER.debug(
        "Beginning to create report - bean=["
            + bean
            + "], stagingUuid=["
            + staginguuid
            + "], packagename=["
            + packagename
            + "]");
    try {
      handlePackageAndReportName(bean, staginguuid, packagename);
    } catch (IOException ioe) {
      logAndThrowError(
          "Failed to create the report [%s] - %s", ioe, bean.getUuid(), ioe.getMessage());
    }
    return super.create(uriInfo, bean, staginguuid);
  }

  @Override
  public Response edit(
      UriInfo uriInfo,
      String uuid,
      ReportBean bean,
      String staginguuid,
      String packagename,
      String lockId,
      boolean keepLocked) {
    LOGGER.debug(
        "Beginning to edit report ["
            + uuid
            + "] - bean=["
            + bean
            + "], stagingUuid=["
            + staginguuid
            + "], packagename=["
            + packagename
            + "], lockId=["
            + lockId
            + "], keepLocked=["
            + keepLocked
            + "]");
    try {
      handlePackageAndReportName(bean, staginguuid, packagename);
    } catch (IOException ioe) {
      logAndThrowError(
          "Failed to edit the report [%s] - %s", ioe, bean.getUuid(), ioe.getMessage());
    }
    return super.edit(uriInfo, uuid, bean, staginguuid, lockId, keepLocked);
  }

  @Override
  public Response packageReportFiles(UriInfo uriInfo, String uuid) {
    // Run the permissions check on this report
    ReportBean bean = this.get(uriInfo, uuid);

    // Gather Report
    Report rpt = reportingService.getByUuid(uuid);
    if (rpt == null) {
      logAndThrowError("Unable to find the report via provided UUID.", null);
    }

    // Create temporary location for packaged report files
    StagingFile stagingArea = stagingService.createStagingArea();
    String targetFilename = "report-files-" + uuid + ".zip";

    // Zip report files as-is from persistent storage to staging area
    try {
      fileSystemService.zipFile(
          new EntityFile(rpt), "", stagingArea, targetFilename, ArchiveType.ZIP);
    } catch (IOException ioe) {
      logAndThrowError("Error packaging the report files for download -%s.", ioe, ioe.getMessage());
    }

    return Response.status(Response.Status.CREATED)
        .location(
            urlLinkService
                .getMethodUriBuilder(StagingResource.class, "getFile")
                .build(stagingArea.getUuid(), targetFilename))
        .build();
  }

  @Override
  protected PrivilegeTree.Node[] getAllNodes() {
    return new PrivilegeTree.Node[] {PrivilegeTree.Node.ALL_REPORTS};
  }

  @Override
  protected BaseEntitySecurityBean createAllSecurityBean() {
    return new BaseEntitySecurityBean();
  }

  @Override
  public ReportingService getEntityService() {
    return reportingService;
  }

  @Override
  protected BaseEntitySerializer<Report, ReportBean> getSerializer() {
    return reportSerializer;
  }

  @Override
  protected Class<?> getResourceClass() {
    return ReportResource.class;
  }

  private void handlePackageAndReportName(ReportBean bean, String staginguuid, String packagename)
      throws IOException {
    // Default packagename is the filename
    if (Check.isEmpty(packagename)) {
      packagename = bean.getFilename();
    }

    // All report files live in the ReportFiles sub directory, but the REST caller shouldn't care
    // about that.
    bean.setFilename(ReportingService.DIR_DESIGN + '/' + bean.getFilename());

    // If the staging area is specified, we need to prepare the contents for storage / execution.
    if (!Check.isEmpty(staginguuid)) {
      reportingService.processReportDesign(staginguuid, packagename);
    }
  }

  private void logAndThrowError(String msg, Exception e, Object... args) throws ReportException {
    String errId = UUID.randomUUID().toString();
    String compiledMsg = "Error ID:[" + errId + "] " + String.format(msg, args);
    LOGGER.error(msg, e);
    throw new ReportException(compiledMsg);
  }
}
