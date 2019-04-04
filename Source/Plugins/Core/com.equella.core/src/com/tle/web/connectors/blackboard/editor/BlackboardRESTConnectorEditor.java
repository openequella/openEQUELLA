package com.tle.web.connectors.blackboard.editor;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.blackboard.service.BlackboardRESTConnectorService;
import com.tle.core.connectors.service.ConnectorEditingBean;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.guice.Bind;
import com.tle.web.connectors.editor.AbstractConnectorEditorSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import java.util.Map;
import javax.inject.Inject;

@SuppressWarnings("nls")
@Bind
public class BlackboardRESTConnectorEditor
    extends AbstractConnectorEditorSection<
        BlackboardRESTConnectorEditor.BlackboardRESTConnectorEditorModel> {
  @PlugKey("bb.editor.error.testwebservice.mandatory")
  private static Label LABEL_TEST_WEBSERVICE_MANDATORY;

  @PlugKey("editor.error.testwebservice.enteruser")
  private static Label LABEL_TEST_WEBSERVICE_ENTERUSER;

  @Inject private BlackboardRESTConnectorService blackboardService;
  @Inject private EncryptionService encryptionService;

  @PlugKey("editor.button.testwebservice")
  @Component
  private Button testWebServiceButton;

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
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    testWebServiceButton.setClickHandler(
        ajax.getAjaxUpdateDomFunction(
            tree, this, events.getEventHandler("testWebService"), "testdiv"));
  }

  @Override
  protected String getAjaxDivId() {
    return "blackboardsetup";
  }

  @EventHandlerMethod
  public void testWebService(SectionInfo info) {
    // final EntityEditingSession<ConnectorEditingBean, Connector> session = saveToSession(info);
    //
    //
    // final ConnectorEditingBean connector = session.getBean();
    //
    // final String result = blackboardService.testConnection(connector.getServerUrl(), "");
    //	if( result == null )
    //	{
    //	connector.setAttribute(BlackboardRESTConnectorConstants.FIELD_TESTED_WEBSERVICE, true);
    //	getModel(info).setTestWebServiceStatus("ok");
    //	}
    //	else
    //	{
    //	connector.setAttribute(BlackboardRESTConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
    //	getModel(info).setTestWebServiceStatus("fail");
    //	session.getValidationErrors().put("blackboardwebservice", result);
    //	}
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
    // FIXME: actual validation of key and secret
    //	if( !connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_TESTED_WEBSERVICE, false)
    // )
    //	{
    //	errors.put("blackboardwebservice", LABEL_TEST_WEBSERVICE_MANDATORY.getText());
    //	}
  }

  @Override
  protected void customLoad(SectionInfo info, ConnectorEditingBean connector) {
    apiKey.setValue(info, connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_KEY));
    apiSecret.setValue(
        info,
        encryptionService.decrypt(
            connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_API_SECRET)));
    final boolean testedWebservice =
        connector.getAttribute(BlackboardRESTConnectorConstants.FIELD_TESTED_WEBSERVICE, false);
    if (testedWebservice) {
      final BlackboardRESTConnectorEditorModel model = getModel(info);
      // model.setTestWebServiceStatus("ok");
    }
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

  public Button getTestWebServiceButton() {
    return testWebServiceButton;
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
