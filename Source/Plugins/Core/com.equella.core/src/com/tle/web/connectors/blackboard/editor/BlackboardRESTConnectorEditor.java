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

package com.tle.web.connectors.blackboard.editor;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.guice.Bind;
import com.tle.web.connectors.editor.AbstractConnectorEditorSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import java.util.Map;
import javax.inject.Inject;
import org.apache.log4j.Logger;

@SuppressWarnings("nls")
@Bind
public class BlackboardRESTConnectorEditor
    extends AbstractConnectorEditorSection<
        BlackboardRESTConnectorEditor.BlackboardRESTConnectorEditorModel> {
  private static final Logger LOGGER = Logger.getLogger(BlackboardRESTConnectorEditor.class);

  @Inject private EncryptionService encryptionService;

  @Component(name = "ak", stateful = false)
  private TextField apiKey;

  @Component(name = "as", stateful = false)
  private TextField apiSecret;

  @ViewFactory private FreemarkerFactory view;
  @EventFactory private EventGenerator events;

  @Override
  protected SectionRenderable renderFields(
      RenderEventContext context, EntityEditingSession<ConnectorEditingBean, Connector> session) {
    return view.createResult("blackboardrestconnector.ftl", context);
  }

  @Override
  protected String getAjaxDivId() {
    return "blackboardrestsetup";
  }

  @Override
  public SectionRenderable renderHelp(RenderContext context) {
    return null;
  }

  @Override
  protected Connector createNewConnector() {
    return new Connector(BlackboardRESTConnectorConstants.CONNECTOR_TYPE);
  }

  @Override
  protected void customValidate(
      SectionInfo info, ConnectorEditingBean connector, Map<String, Object> errors) {
    // no op
  }

  @Override
  protected void customLoad(SectionInfo info, ConnectorEditingBean connector) {
    apiKey.setValue(info, connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY));
    apiSecret.setValue(
        info,
        encryptionService.decrypt(
            connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_SECRET)));
  }

  @Override
  protected void customSave(SectionInfo info, ConnectorEditingBean connector) {
    connector.setAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY, apiKey.getValue(info));
    connector.setAttribute(
        BlackboardRESTConnectorConstants.FIELD_API_SECRET,
        encryptionService.encrypt(apiSecret.getValue(info)));
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new BlackboardRESTConnectorEditorModel();
  }

  public TextField getApiKey() {
    return apiKey;
  }

  public TextField getApiSecret() {
    return apiSecret;
  }

  public class BlackboardRESTConnectorEditorModel
      extends AbstractConnectorEditorSection<
              BlackboardRESTConnectorEditor.BlackboardRESTConnectorEditorModel>
          .AbstractConnectorEditorModel {}
}
