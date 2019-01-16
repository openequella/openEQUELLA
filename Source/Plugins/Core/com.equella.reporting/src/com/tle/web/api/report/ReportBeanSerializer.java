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

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.report.Report;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.reporting.ReportingService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@NonNullByDefault
@Bind
@Singleton
public class ReportBeanSerializer
    extends AbstractEquellaBaseEntitySerializer<Report, ReportBean, ReportEditor> {
  @Inject private ReportingService reportService;
  @Inject private ReportEditorFactory editorFactory;

  @Override
  protected AbstractEntityService<?, Report> getEntityService() {
    return reportService;
  }

  @Override
  protected ReportEditor createNewEditor(Report entity, String stagingUuid, boolean importing) {
    return editorFactory.createNewEditor(entity, stagingUuid, importing);
  }

  @Override
  protected ReportEditor createExistingEditor(
      Report entity, String stagingUuid, String lockId, boolean importing) {
    return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
  }

  @Override
  protected Report createEntity() {
    return new Report();
  }

  @Override
  protected ReportBean createBean() {
    return new ReportBean();
  }

  @Override
  protected Node getNonVirtualNode() {
    return Node.REPORT;
  }

  @Override
  protected void validateCustom(ReportBean bean, boolean create, List<ValidationError> errors) {
    super.validateCustom(bean, create, errors);
    if (Check.isEmpty(bean.getFilename())) {
      errors.add(
          new ValidationError("filename", CurrentLocale.get("report.validation.filenameempty")));
    }
  }

  @Override
  protected void copyCustomFields(Report entity, ReportBean bean, Object data) {
    super.copyCustomFields(entity, bean, data);
  }

  protected void copyCustomLightweightFields(Report entity, ReportBean bean, Object data) {
    bean.setHideReport(entity.isHideReport());
    bean.setFilename(entity.getFilename());
  }
}
