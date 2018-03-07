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

package com.tle.web.api.report;

import com.tle.beans.entity.report.Report;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.SecurityConstants;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.ReportingService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.staging.interfaces.StagingResource;
import com.tle.web.remoting.rest.service.UrlLinkService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@SuppressWarnings("nls")
@Bind(ReportResource.class)
@Singleton
public class ReportResourceImpl extends
        AbstractBaseEntityResource<Report, BaseEntitySecurityBean, ReportBean> implements ReportResource {

    @Inject
    private ReportingService reportingService;

    @Inject
    private ReportBeanSerializer reportSerializer;

    @Inject
    private StagingService stagingService;

    @Inject
    private FileSystemService fileSystemService;

    @Inject
    private UrlLinkService urlLinkService;

    @Override
    public SearchBean<ReportBean> list(UriInfo uriInfo)
    {
        return super.list(uriInfo);
    }

    @Override
    public Response packageReportFiles(UriInfo uriInfo, String uuid) {
        // Run the permissions check on this report
        ReportBean bean = this.get(uriInfo, uuid);

        //Gather Report
        Report rpt = reportingService.getByUuid(uuid);
        if(rpt == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        //Create temporary location for packaged report files
        StagingFile stagingArea = stagingService.createStagingArea();
        String targetFilename = "report-files-"+uuid+".zip";

        // Zip report files as-is from persistent storage to staging area
        try {
            fileSystemService.zipFile(new EntityFile(rpt), "", stagingArea, targetFilename, ArchiveType.ZIP);
        } catch (IOException ioe) {
            throw new ReportException("Error packaging the report files for download.");
        }

        return Response.status(Response.Status.CREATED).location(urlLinkService.getMethodUriBuilder(StagingResource.class, "getFile").build(stagingArea.getUuid(), targetFilename)).build();
    }

    @Override
    protected PrivilegeTree.Node[] getAllNodes() {
        return new PrivilegeTree.Node[]{PrivilegeTree.Node.ALL_REPORTS};
    }

    @Override
    protected BaseEntitySecurityBean createAllSecurityBean() {
        return new BaseEntitySecurityBean();
    }

    @Override
    protected ReportingService getEntityService() {
        return reportingService;
    }

    @Override
    protected int getSecurityPriority() {
        return SecurityConstants.PRIORITY_ALL_REPORTS;
    }

    @Override
    protected BaseEntitySerializer<Report, ReportBean> getSerializer() {
        return reportSerializer;
    }

    @Override
    protected Class<?> getResourceClass() {
        return ReportResource.class;
    }
}