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

package com.tle.core.connectors.service;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.connectors.ConnectorTypeDescriptor;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.RemoteConnectorService;
import com.tle.core.entity.service.AbstractEntityService;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConnectorService
    extends AbstractEntityService<ConnectorEditingBean, Connector>, RemoteConnectorService {
  String ENTITY_TYPE = "CONNECTOR"; // $NON-NLS-1$

  Collection<ConnectorTypeDescriptor> listAllAvailableTypes();

  List<BaseEntityLabel> listExportable();

  List<BaseEntityLabel> listForViewing();

  Map<String, ConnectorTypeDescriptor> mapAllAvailableTypes();

  Connector getForEdit(String connectorUuid);

  boolean canViewContent(BaseEntityLabel connector);

  boolean canViewContent(Connector connector);

  boolean canExport(Connector connector);

  List<Connector> enumerateForUrl(String url);
}
