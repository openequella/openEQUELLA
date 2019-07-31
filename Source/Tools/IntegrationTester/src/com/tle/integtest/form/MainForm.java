package com.tle.integtest.form;

import com.tle.common.NameValue;
import java.util.List;
import org.apache.struts.action.ActionForm;

public class MainForm extends ActionForm {
  private String method;
  private String integrationMethod;
  private String sharedSecret;
  private String courseId;
  private String url;
  private String username;
  private String template;
  private String action;
  private String sharedSecretId;
  private String clickUrl;
  private String options;
  private boolean makeReturn;
  private boolean selectMultiple;
  private boolean useDownloadPrivilege;
  private boolean forcePost;
  private boolean cancelDisabled;
  private boolean attachmentUuidUrls;
  private boolean itemonly;
  private boolean attachmentonly;
  private boolean packageonly;

  private String itemXml;
  private String powerXml;
  private String structure;

  private List<NameValue> actions;
  private List<NameValue> methods;
  private List<NameValue> templates;

  private List<NameValue> returnVals;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getIntegrationMethod() {
    return integrationMethod;
  }

  public void setIntegrationMethod(String integrationMethod) {
    this.integrationMethod = integrationMethod;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getSharedSecret() {
    return sharedSecret;
  }

  public void setSharedSecret(String sharedSecret) {
    this.sharedSecret = sharedSecret;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<NameValue> getActions() {
    return actions;
  }

  public void setActions(List<NameValue> actions) {
    this.actions = actions;
  }

  public List<NameValue> getMethods() {
    return methods;
  }

  public void setMethods(List<NameValue> methods) {
    this.methods = methods;
  }

  public List<NameValue> getTemplates() {
    return templates;
  }

  public void setTemplates(List<NameValue> templates) {
    this.templates = templates;
  }

  public String getSharedSecretId() {
    return sharedSecretId;
  }

  public void setSharedSecretId(String sharedSecretId) {
    this.sharedSecretId = sharedSecretId;
  }

  public String getClickUrl() {
    return clickUrl;
  }

  public void setClickUrl(String clickUrl) {
    this.clickUrl = clickUrl;
  }

  public boolean isMakeReturn() {
    return makeReturn;
  }

  public void setMakeReturn(boolean makeReturn) {
    this.makeReturn = makeReturn;
  }

  public List<NameValue> getReturnVals() {
    return returnVals;
  }

  public void setReturnVals(List<NameValue> returnVals) {
    this.returnVals = returnVals;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public void setSelectMultiple(boolean selectMultiple) {
    this.selectMultiple = selectMultiple;
  }

  public boolean isSelectMultiple() {
    return selectMultiple;
  }

  public void setUseDownloadPrivilege(boolean useDownloadPrivilege) {
    this.useDownloadPrivilege = useDownloadPrivilege;
  }

  public boolean isUseDownloadPrivilege() {
    return useDownloadPrivilege;
  }

  public String getItemXml() {
    return itemXml;
  }

  public void setItemXml(String itemXml) {
    this.itemXml = itemXml;
  }

  public String getPowerXml() {
    return powerXml;
  }

  public void setPowerXml(String powerXml) {
    this.powerXml = powerXml;
  }

  public String getStructure() {
    return structure;
  }

  public void setStructure(String structure) {
    this.structure = structure;
  }

  public boolean isForcePost() {
    return forcePost;
  }

  public void setForcePost(boolean forcePost) {
    this.forcePost = forcePost;
  }

  public boolean isCancelDisabled() {
    return cancelDisabled;
  }

  public void setCancelDisabled(boolean cancelDisabled) {
    this.cancelDisabled = cancelDisabled;
  }

  public boolean isAttachmentUuidUrls() {
    return attachmentUuidUrls;
  }

  public void setAttachmentUuidUrls(boolean attachmentUuidUrls) {
    this.attachmentUuidUrls = attachmentUuidUrls;
  }

  public boolean isItemonly() {
    return itemonly;
  }

  public void setItemonly(boolean itemonly) {
    this.itemonly = itemonly;
  }

  public boolean isAttachmentonly() {
    return attachmentonly;
  }

  public void setAttachmentonly(boolean attachmentonly) {
    this.attachmentonly = attachmentonly;
  }

  public boolean isPackageonly() {
    return packageonly;
  }

  public void setPackageonly(boolean packageonly) {
    this.packageonly = packageonly;
  }
}
