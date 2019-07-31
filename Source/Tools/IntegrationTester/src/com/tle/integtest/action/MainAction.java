package com.tle.integtest.action;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.util.TokenGenerator;
import com.tle.integtest.form.MainForm;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

@SuppressWarnings("nls")
public class MainAction extends DispatchAction {
  // Needs to be kept up to date with
  // com.tle.web.integration/resources/spring.xml
  protected static final String[] ACTIONS = {
    "contribute", "searchResources", "selectOrAdd", "searchThin", "structured",
  };

  @Override
  public ActionForward unspecified(
      ActionMapping mapping,
      ActionForm formData,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {
    MainForm form = (MainForm) formData;
    setupCollections(form);
    Properties properties = new Properties();
    properties.load(getClass().getResourceAsStream("/defaults.properties"));
    BeanUtils.populate(form, properties);

    InputStream xml = null;
    try {
      xml = getClass().getResourceAsStream("/defaults.xml");
      if (xml != null) {
        Map<String, String> xmlProps = new HashMap<String, String>();
        PropBagEx pbag = new PropBagEx(xml);
        for (PropBagEx value : pbag.iterateAll("*")) {
          xmlProps.put(value.getNodeName(), value.getSubtree("*").toString());
        }
        BeanUtils.populate(form, xmlProps);
      }
    } finally {
      if (xml != null) {
        xml.close();
      }
    }

    InputStream json = null;
    try {
      json = getClass().getResourceAsStream("/defaults.json");
      if (json != null) {
        Map<String, String> jsonProps = new HashMap<String, String>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode obj = (ObjectNode) objectMapper.readTree(json);
        Iterator<String> fields = obj.fieldNames();
        while (fields.hasNext()) {
          String field = fields.next();
          JsonNode node = obj.get(field);

          jsonProps.put(field, objectMapper.writeValueAsString(node));
        }
        BeanUtils.populate(form, jsonProps);
      }
    } finally {
      if (json != null) {
        json.close();
      }
    }

    form.setMethod("submit");
    return mapping.findForward("main");
  }

  public ActionForward submit(
      ActionMapping mapping,
      ActionForm formData,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {
    MainForm form = (MainForm) formData;
    String secretId = form.getSharedSecretId();
    if (Check.isEmpty(secretId)) secretId = null;
    List<NameValue> nvs = new ArrayList<NameValue>();
    String token =
        TokenGenerator.createSecureToken(
            form.getUsername(), secretId, form.getSharedSecret(), null);
    String meths = form.getIntegrationMethod();
    nvs.add(new NameValue("method", meths));
    if (meths != null && meths.equals("vista")) {
      nvs.add(new NameValue("proxyToolCallbackGUID", "111"));
      nvs.add(new NameValue("addtool", "a%26componentType%3Dx"));
    }
    nvs.add(new NameValue("action", form.getAction()));
    nvs.add(new NameValue("template", form.getTemplate()));
    if (form.getCourseId().length() > 0) {
      nvs.add(new NameValue("courseId", form.getCourseId()));
    }
    if (form.isSelectMultiple()) {
      nvs.add(new NameValue("selectMultiple", Boolean.toString(true)));
    }
    if (form.isUseDownloadPrivilege()) {
      nvs.add(new NameValue("useDownloadPrivilege", Boolean.toString(true)));
    }
    if (form.isForcePost()) {
      nvs.add(new NameValue("forcePost", Boolean.toString(true)));
    }
    if (form.isCancelDisabled()) {
      nvs.add(new NameValue("cancelDisabled", Boolean.toString(true)));
    }
    if (form.isAttachmentUuidUrls()) {
      nvs.add(new NameValue("attachmentUuidUrls", Boolean.toString(true)));
    }
    if (form.getOptions().length() > 0) {
      nvs.add(new NameValue("options", form.getOptions()));
    }
    if (form.isItemonly()) {
      nvs.add(new NameValue("itemonly", Boolean.toString(true)));
    }
    if (form.isAttachmentonly()) {
      nvs.add(new NameValue("attachmentonly", Boolean.toString(true)));
    }
    if (form.isPackageonly()) {
      nvs.add(new NameValue("packageonly", Boolean.toString(true)));
    }
    nvs.add(new NameValue("token", token));
    nvs.add(new NameValue("returnprefix", ""));
    if (form.isMakeReturn()) {
      nvs.add(
          new NameValue(
              "returnurl",
              request.getRequestURL().toString()
                  + '?'
                  + getParameterString(Arrays.asList(new NameValue("method", "showReturn")))));
    }
    String clickUrl = form.getUrl() + '?' + getParameterString(nvs);
    form.setClickUrl(clickUrl);
    setupCollections(form);
    return mapping.findForward("main");
  }

  public ActionForward showReturn(
      ActionMapping mapping,
      ActionForm formData,
      HttpServletRequest request,
      HttpServletResponse response)
      throws Exception {
    MainForm form = (MainForm) formData;
    List<NameValue> returnVals = new ArrayList<NameValue>();
    Set<String> paramNames = request.getParameterMap().keySet();
    for (String name : paramNames) {
      returnVals.add(new NameValue(name, request.getParameter(name)));
    }
    Collections.sort(returnVals, new NumberStringComparator<NameValue>());
    form.setReturnVals(returnVals);
    return mapping.findForward("returned");
  }

  private void setupCollections(MainForm form) {
    List<NameValue> methods = new ArrayList<NameValue>();
    methods.add(new NameValue("lms", "lms"));
    methods.add(new NameValue("vista", "vista"));
    form.setMethods(methods);
    List<NameValue> actions = new ArrayList<NameValue>();
    for (String name : ACTIONS) {
      actions.add(new NameValue(name, name));
    }
    Collections.sort(actions, new NumberStringComparator<NameValue>());
    form.setActions(actions);
    List<NameValue> templates = new ArrayList<NameValue>();
    templates.add(new NameValue("standard", "standard"));
    form.setTemplates(templates);
  }

  public static String getParameterString(List<NameValue> nvs) {
    // This is to "pass" on any parameters that have been passed to this jsp
    StringBuilder parameters = new StringBuilder();

    for (NameValue nv : nvs) {
      if (parameters.length() > 0) {
        parameters.append('&');
      }

      parameters.append(URLUtils.basicUrlEncode(nv.getName()));
      parameters.append('=');
      parameters.append(URLUtils.basicUrlEncode(nv.getValue()));
    }
    return parameters.toString();
  }
}
