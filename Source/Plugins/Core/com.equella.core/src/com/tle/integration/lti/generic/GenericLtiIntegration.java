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

package com.tle.integration.lti.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.connectors.blackboard.BlackboardRESTConnectorConstants;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.services.user.UserSessionService;
import com.tle.exceptions.BadConfigurationException;
import com.tle.web.integration.AbstractIntegrationService;
import com.tle.web.integration.IntegrationActionInfo;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.integration.guice.IntegrationModule;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.lti.LtiData;
import com.tle.web.lti.usermanagement.LtiUserState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewurl.ViewableResource;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.log4j.Logger;

@Bind
@Singleton
@NonNullByDefault
@SuppressWarnings("nls")
public class GenericLtiIntegration extends AbstractIntegrationService<GenericLtiSessionData> {
  private static final Logger LOGGER = Logger.getLogger(GenericLtiIntegration.class);

  private static final String CONTENT_ITEM_SELECTION_REQUEST = "ContentItemSelectionRequest";

  private static final String FROM_REDIRECT = "from_redirect";

  static {
    PluginResourceHandler.init(GenericLtiIntegration.class);
  }

  @PlugKey("integration.receipt.addedtogenericlti")
  private static String KEY_RECEIPT_ADDED;

  @PlugKey("canvas.error.requireoneconnector")
  private static String KEY_ERROR_NO_SINGLE_CONNECTOR;

  @PlugKey("integration.error.nocourse")
  private static Label LABEL_ERROR_NO_COURSE;

  @PlugKey("integration.error.noapidomain")
  private static Label LABEL_ERROR_NO_API_DOMAIN;

  @Inject private IntegrationService integrationService;
  @Inject private ViewableItemResolver viewableItemResolver;
  @Inject private ReceiptService receiptService;
  @Inject private ConnectorService connectorService;
  @Inject private ConnectorRepositoryService connectorRepoService;
  @Inject private ReplicatedCacheService cacheService;
  @Inject private InstitutionService institutionService;
  @Inject private UserSessionService userSessionService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected String getIntegrationType() {
    return "lti";
  }

  public boolean isItemOnly(GenericLtiSessionData data) {
    return false;
  }

  @Override
  public GenericLtiSessionData createDataForViewing(SectionInfo info) {
    return new GenericLtiSessionData();
  }

  @Override
  public void setupSingleSignOn(SectionInfo info, SingleSignonForm form) {
    GenericLtiSessionData data = null;
    SingleSignonForm formToUse = null;
    // 'SSO' can be from an LTI launch or from a redirect after authenticating
    // the the external system connector (usually via REST).  If it's from a redirect,
    // we need to pull the saved LTI launch details from the user session.
    final String fromRedirect = info.getRequest().getParameter(FROM_REDIRECT);
    if (!Strings.isNullOrEmpty(fromRedirect) && Boolean.valueOf(fromRedirect)) {
      // Coming from a redirect
      data = userSessionService.getAttribute("rehydrate_data");
      formToUse = userSessionService.getAttribute("rehydrate_form");
    } else {
      // Direct LTI Launch
      data = new GenericLtiSessionData(info.getRequest());
      formToUse = form;
      parseCourseValuesFromLtiLaunch(info, form, data);
      parseCurrentContentId(info, form, data);
    }

    IntegrationActionInfo actionInfo = findActionInfo(data, formToUse);

    LOGGER.debug("setupSingleSignOnFromLtiLaunch: about to forward - data=" + data);
    integrationService.standardForward(
        info, convertToForward(actionInfo, formToUse), data, actionInfo, formToUse);
  }

  private void parseCurrentContentId(
      SectionInfo info, SingleSignonForm form, GenericLtiSessionData data) {
    final UserState userState = CurrentUser.getUserState();

    if (userState instanceof LtiUserState) {
      String contentId = "";
      final LtiUserState ltiUserState = (LtiUserState) userState;
      final LtiData ltiData = ltiUserState.getData();
      if (ltiData != null) {
        // custom parameters override standards
        contentId = ltiData.getCustom("custom_content_id");

        // Otherwise, try the next possible location
        if (Strings.isNullOrEmpty(contentId)) {
          contentId = ltiData.getCustom("resource_link_id");
        }

        data.setContentId(contentId);
      }
    }
  }

  private void parseCourseValuesFromLtiLaunch(
      SectionInfo info, SingleSignonForm form, GenericLtiSessionData data) {
    String courseId = null;
    String courseCode = form.getCourseCode();

    final UserState userState = CurrentUser.getUserState();

    if (userState instanceof LtiUserState) {
      final LtiUserState ltiUserState = (LtiUserState) userState;
      final LtiData ltiData = ltiUserState.getData();
      if (ltiData != null) {
        // custom parameters override standards
        courseId = ltiData.getCustom("custom_course_id");

        // Otherwise, try the standard location
        if (Strings.isNullOrEmpty(courseId)) {
          courseId = form.getCourseId();
        }

        // Fallback
        if (Strings.isNullOrEmpty(courseId)) {
          courseId = ltiData.getContextId();
        }

        data.setCourseId(courseId);
        data.setContextTitle(ltiData.getContextTitle());
        if (Strings.isNullOrEmpty(courseCode)) {
          courseCode = ltiData.getContextLabel();
        }
      }
    }

    data.setCourseInfoCode(integrationService.getCourseInfoCode(courseId, courseCode));
  }

  private IntegrationActionInfo findActionInfo(GenericLtiSessionData data, SingleSignonForm form) {

    String formDataAction = form.getAction();
    if (formDataAction == null) {
      formDataAction = IntegrationModule.SELECT_OR_ADD_DEFAULT_ACTION;
    }

    IntegrationActionInfo actionInfo =
        integrationService.getActionInfo(formDataAction, form.getOptions());

    if (actionInfo.getName().equals("unknown")) {
      return integrationService.getActionInfoForUrl('/' + data.getAction());
    }

    if (actionInfo == null) {
      return new IntegrationActionInfo();
    }

    return actionInfo;
  }

  private String convertToForward(IntegrationActionInfo action, SingleSignonForm model) {
    String forward = action.getPath();
    if (forward == null) {
      forward = action.getName();
    }

    if (action.getName().equals("standard")) {
      forward = forward + model.getQuery();
    }

    return forward.substring(1);
  }

  private String convertToRedirect(
      SectionInfo info,
      GenericLtiSessionData data,
      SelectionSession session,
      SingleSignonForm form) {
    userSessionService.setAttribute("rehydrate_data", data);
    userSessionService.setAttribute("rehydrate_form", form);
    return info.getRequest().getRequestURI()
        + "?"
        + info.getRequest().getQueryString()
        + "&"
        + FROM_REDIRECT
        + "=true";
  }

  @Override
  public void forward(SectionInfo info, GenericLtiSessionData data, SectionInfo forward) {
    info.forwardAsBookmark(forward);
  }

  @Nullable
  @Override
  public SelectionSession setupSelectionSession(
      SectionInfo info,
      GenericLtiSessionData data,
      SelectionSession session,
      SingleSignonForm form) {

    final boolean structured = "structured".equals(data.getAction());

    session.setSelectMultiple(true); // all integration points support multiple
    session.setAttachmentUuidUrls(true); // Always
    session.setInitialItemXml(form.getItemXml());
    session.setInitialPowerXml(form.getPowerXml());
    session.setCancelDisabled(form.isCancelDisabled());

    // Setup the structure param before super.setupSelectionSession so the
    // extension can setup the TargetStructure
    if (structured) {
      form.setStructure(initStructure(info, data, session, form));
    }

    return super.setupSelectionSession(info, data, session, form);
  }

  @Nullable
  private String initStructure(
      SectionInfo info,
      GenericLtiSessionData data,
      SelectionSession session,
      SingleSignonForm form) {
    final String courseId = data.getCourseId();
    String structure = form.getStructure();
    if (structure == null) {
      // LMS didn't send the course folder structure with the LTI request.

      // if course ID is empty then there is nothing we can do...
      if (Strings.isNullOrEmpty(courseId)) {
        throw new RuntimeException(LABEL_ERROR_NO_COURSE.getText());
      }

      // Check if the user needs to authenticate
      if (connectorRepoService.isRequiresAuthentication(findConnector(data).get())) {
        Optional<Connector> connector = findConnector(data);
        final String connLmsType = connector.isPresent() ? connector.get().getLmsType() : "UNKNOWN";
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Need to authenticate to the " + connLmsType + " connector repository.");
        }

        if (connector.isPresent()
            && connector
                .get()
                .getLmsType()
                .equals(BlackboardRESTConnectorConstants.CONNECTOR_TYPE)) {
          info.forwardToUrl(
              connectorRepoService.getAuthorisationUrl(
                  findConnector(data).get(), convertToRedirect(info, data, session, form), null));
        } else {
          LOGGER.fatal("Unknown LMS Type to authorize: " + connLmsType);
        }
      }

      // Need to leverage the external system connector (usually REST-based) to populate the list.
      final ObjectNode root = objectMapper.createObjectNode();
      root.put("id", courseId);
      root.put("name", data.getContextTitle());
      root.put("targetable", false);
      final ArrayNode foldersNode = root.putArray("folders");

      final Optional<Connector> connector = findConnector(data);

      if (connector.isPresent()) {
        final List<ConnectorFolder> folders =
            connectorRepoService.getFoldersForCourse(
                connector.get(), CurrentUser.getUsername(), courseId, false);

        folders.stream()
            .forEach(
                folder -> {
                  foldersNode.add(convert(folder, data));
                });
      } else {
        LOGGER.fatal("Unable to build folder structure - connector was not found");
      }

      final PrettyPrinter pp = new MinimalPrettyPrinter();
      try {
        structure = objectMapper.writer().with(pp).writeValueAsString(root);
      } catch (JsonProcessingException e) {
        throw Throwables.propagate(e);
      }
    }

    return structure;
  }

  private ObjectNode convert(ConnectorFolder folder, GenericLtiSessionData data) {
    final ObjectNode folderNode = objectMapper.createObjectNode();
    folderNode.put("id", folder.getId());
    folderNode.put("name", folder.getName());
    folderNode.put("targetable", true);
    if (folder.getId().equals(data.getContentId())) {
      folderNode.put("defaultFolder", true);
      folderNode.put("selected", true);
    }
    if (folder.getFolders() != null) {
      final ArrayNode childrenJsonFolders = folderNode.putArray("folders");

      folder.getFolders().stream()
          .forEach(
              childrenFolder -> {
                childrenJsonFolders.add(convert(childrenFolder, data));
              });
    }
    return folderNode;
  }

  private Optional<Connector> findConnector(GenericLtiSessionData data) {
    Connector connector = null;
    final String connectorUuid = data.getConnectorUuid();
    if (connectorUuid != null) {
      connector = connectorService.getByUuid(connectorUuid);
    }
    if (connector == null) {
      final String apiDomain = data.getApiDomain();
      if (apiDomain != null) {
        final String tcUrl = "://" + apiDomain;

        final List<Connector> connectors = connectorService.enumerateForUrl(tcUrl);
        if (connectors.size() == 1) {
          connector = connectors.get(0);
          data.setConnectorUuid(connector.getUuid());
        } else {
          throw new RuntimeException(
              new KeyLabel(KEY_ERROR_NO_SINGLE_CONNECTOR, connectors.size(), tcUrl).getText());
        }
      } else {
        throw new RuntimeException(LABEL_ERROR_NO_API_DOMAIN.getText());
      }
    }
    return Optional.fromNullable(connector);
  }

  @Override
  public boolean select(SectionInfo info, GenericLtiSessionData data, SelectionSession session) {
    try {
      // TODO: a find a better way to determine if in structured session or not
      if (!session.getLayout().equals(RootSelectionSection.Layout.COURSE)) {
        String lti_message_type = data.getLtiMessageType();
        if (lti_message_type != null
            && lti_message_type.equalsIgnoreCase(CONTENT_ITEM_SELECTION_REQUEST)) {
          info.forward(info.createForward("/lticipreturn.do"));
        } else {
          final SelectedResource resource = getFirstSelectedResource(session);
          final IItem<?> item = getItemForResource(resource);

          final LmsLink link =
              getLinkForResource(
                      info,
                      createViewableItem(item, resource),
                      resource,
                      false,
                      session.isAttachmentUuidUrls())
                  .getLmsLink();

          final String mimeType;
          if (!Check.isEmpty(resource.getAttachmentUuid())) {
            final SelectedResourceKey key = resource.getKey();
            final ViewableItem<?> viewableItem =
                viewableItemResolver.createViewableItem(item, key.getExtensionType());
            final IAttachment attachment =
                viewableItem.getAttachmentByUuid(resource.getAttachmentUuid());
            final ViewableResource viewableResource =
                attachmentResourceService.getViewableResource(info, viewableItem, attachment);
            mimeType = viewableResource.getMimeType();
          } else {
            mimeType = "equella/item";
          }
          final String launchPresentationReturnUrl = data.getLaunchPresentationReturnUrl();
          final StringBuilder retUrl = new StringBuilder(launchPresentationReturnUrl);
          retUrl.append(launchPresentationReturnUrl.contains("?") ? "&" : "?");

          retUrl.append("return_type=lti_launch_url");

          retUrl.append("&url=");
          retUrl.append(URLEncoder.encode(link.getUrl(), "UTF-8"));

          // e.g. <a title="${title}"></a>
          retUrl.append("&title=");
          retUrl.append(URLEncoder.encode(link.getName(), "UTF-8"));

          // e.g. <a>${text}</a>
          retUrl.append("&text=");
          retUrl.append(URLEncoder.encode(link.getName(), "UTF-8"));
          //					}

          info.forwardToUrl(retUrl.toString());
        }

        // maintain the selections so they survive the forwarding
        return true;

      } else {
        final Optional<Connector> connector = findConnector(data);

        if (!connector.isPresent()) {
          throw new BadConfigurationException("No connector found");
        }

        final String courseId = session.getStructure().getId();

        // add resources via REST
        final Collection<SelectedResource> selectedResources = session.getSelectedResources();
        for (SelectedResource resource : selectedResources) {
          final IItem<?> item = getItemForResource(resource);
          final String moduleId = resource.getKey().getFolderId();
          connectorRepoService.addItemToCourse(
              connector.get(), CurrentUser.getUsername(), courseId, moduleId, item, resource);
        }
        final int count = selectedResources.size();
        // clear session
        session.clearResources();

        // provide receipt and stay where we are
        receiptService.setReceipt(new PluralKeyLabel(KEY_RECEIPT_ADDED, count));
      }

      return false;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public String getClose(GenericLtiSessionData data) {
    return data.getLaunchPresentationReturnUrl();
  }

  @Nullable
  @Override
  public String getCourseInfoCode(GenericLtiSessionData data) {
    return data.getCourseInfoCode();
  }

  @Nullable
  @Override
  public NameValue getLocation(GenericLtiSessionData data) {
    return null;
  }

  @Override
  protected boolean canSelect(GenericLtiSessionData data) {
    // can be select_link, embed_content etc
    final String sd = data.getSelectionDirective();
    return sd != null || "ContentItemSelectionRequest".equals(data.getLtiMessageType());
  }

  @Override
  protected <I extends IItem<?>> ViewableItem<I> createViewableItem(
      I item, SelectedResource resource) {
    final ViewableItem<I> vitem =
        viewableItemResolver.createIntegrationViewableItem(
            item,
            resource.isLatest(),
            ViewableItemType.GENERIC,
            resource.getKey().getExtensionType());
    return vitem;
  }

  @Override
  public <I extends IItem<?>> ViewableItem<I> createViewableItem(
      ItemId itemId, boolean latest, @Nullable String itemExtensionType) {
    final ViewableItem<I> vitem =
        viewableItemResolver.createIntegrationViewableItem(
            itemId, latest, ViewableItemType.GENERIC, itemExtensionType);
    return vitem;
  }
}
