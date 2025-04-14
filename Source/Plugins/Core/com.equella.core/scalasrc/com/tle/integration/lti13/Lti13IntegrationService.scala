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

package com.tle.integration.lti13

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.tle.core.guice.Bind
import com.tle.core.lti13.service.LtiPlatformService
import com.tle.core.webkeyset.service.WebKeySetService
import com.tle.integration.oidc.OpenIDConnectParams
import com.tle.web.integration.guice.IntegrationModule
import com.tle.web.integration.service.IntegrationService
import com.tle.web.integration.{
  AbstractIntegrationService,
  IntegrationSessionData,
  SingleSignonForm
}
import com.tle.web.sections.equella.{AbstractScalaSection, ModalSession}
import com.tle.web.sections.generic.DefaultSectionTree
import com.tle.web.sections.header.{FormTag, SimpleFormAction}
import com.tle.web.sections.jquery.{JQuerySelector, JQueryStatement}
import com.tle.web.sections.registry.TreeRegistry
import com.tle.web.sections.render.HiddenInput
import com.tle.web.sections.{SectionInfo, SectionNode, SectionsController}
import com.tle.web.selection.section.RootSelectionSection
import com.tle.web.selection.{
  SelectedResource,
  SelectionSession,
  SelectionsMadeCallback,
  TreeLookupSelectionCallback
}
import com.tle.web.template.{Decorations, RenderNewTemplate}
import com.tle.web.viewable.ViewItemLinkFactory

import java.time.Instant
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

/** Data required to support the LTI 1.3 content selection workflow.
  */
case class Lti13IntegrationSessionData(
    deepLinkingSettings: LtiDeepLinkingSettings,
    context: Option[LtiDeepLinkingContext]
) extends IntegrationSessionData {
  override def isForSelection: Boolean    = true
  override def getIntegrationType: String = "lti13"
}

object Lti13IntegrationSessionData {
  def apply(request: LtiDeepLinkingRequest): Lti13IntegrationSessionData = {
    Lti13IntegrationSessionData(request.deepLinkingSettings, request.context)
  }
}

/** This Integration Service is dedicated to the integration established by LTI 1.3. It provides
  * core functions that are used in the context of Selection Session, such as launching Selection
  * Session and communicating with LMS to complete content selections.
  */
@Bind
@Singleton
class Lti13IntegrationService extends AbstractIntegrationService[Lti13IntegrationSessionData] {
  private var integrationService: IntegrationService = _
  private var sectionsController: SectionsController = _
  private var treeRegistry: TreeRegistry             = _
  private var webKeySetService: WebKeySetService     = _
  private var ltiPlatformService: LtiPlatformService = _
  private var linkFactory: ViewItemLinkFactory       = _
  private val LTI13_INTEGRATION_CALLBACK             = "$LTI13$INTEG$RETURNER"

  @Inject
  def this(
      integrationService: IntegrationService,
      webKeySetService: WebKeySetService,
      ltiPlatformService: LtiPlatformService,
      sectionsController: SectionsController,
      treeRegistry: TreeRegistry,
      linkFactory: ViewItemLinkFactory
  ) = {
    this()
    this.treeRegistry = treeRegistry
    this.integrationService = integrationService
    this.sectionsController = sectionsController
    this.webKeySetService = webKeySetService
    this.ltiPlatformService = ltiPlatformService
    this.linkFactory = linkFactory
  }

  // The value of each ContentItem must be constructed as a `Map` so that it will be accepted by `com.auth0.jwt.JWTCreator`.
  private def buildDeepLinkingContentItems(
      info: SectionInfo,
      session: SelectionSession
  ): java.util.List[java.util.Map[String, Object]] = {
    def buildSelectedContent(resource: SelectedResource): java.util.Map[String, Object] = {
      val item = getItemForResource(resource)

      def buildUrl: String =
        getLinkForResource(
          info,
          createViewableItem(item, resource),
          resource,
          false,
          session.isAttachmentUuidUrls
        ).getLmsLink.getUrl

      def buildIcon: Option[java.util.Map[String, Any]] = {
        val itemID = resource.createItemId()

        // If the selected resource is an Attachment or an Item which has attachments, build
        // the thumbnail link for the resource.
        val thumbnailLink = resource.getType match {
          case SelectedResource.TYPE_PATH =>
            Option(item.getAttachments)
              .filterNot(_.isEmpty)
              .map(_ => linkFactory.createThumbnailAttachmentLink(itemID, null))
          case SelectedResource.TYPE_ATTACHMENT =>
            Some(linkFactory.createThumbnailAttachmentLink(itemID, resource.getAttachmentUuid))
          case _ => None
        }

        thumbnailLink.map(link => Map("url" -> link.getHref, "width" -> 48, "height" -> 48).asJava)
      }

      val selectedContent = Map(
        "type"  -> "ltiResourceLink",
        "title" -> resource.getTitle,
        "url"   -> buildUrl,
        "text"  -> resource.getDescription
      )

      buildIcon
        .map(icon => selectedContent ++ Map("icon" -> icon))
        .getOrElse(selectedContent)
        .asJava
    }

    session.getSelectedResources.asScala
      .map(buildSelectedContent)
      .toList
      .asJava
  }

  /** According to the spec for LTI 1.3 workflow
    * <https://www.imsglobal.org/spec/lti-dl/v2p0#redirection-back-to-the-platform>, we (tool
    * provider) MUST redirect the workflow to the return URL provided in the Deep linking setting
    * once the user has completed the selection or creation portion of the overall flow. To do this,
    * we MUST always perform this redirection using an auto-submitted form as an HTTP POST request
    * using the JWT parameter.
    *
    * In New UI, we have to rely on `LegacyContentApi` to return the form back to the front-end. So
    * we need to build a `FormTag` and add it to the render context of `SectionInfo`. But in Old UI,
    * we can directly output the form and a script to submit the form in the response.
    *
    * @param deepLinkReturnUrl
    *   The URL redirected to to complete a selection
    * @param jwt
    *   JWT generated based on the Deep linking response to be sent back to platform.
    * @param info
    *   The Legacy SectionInfo used to help submit the form in New UI.
    * @param response
    *   HTTP servlet response used to help submit the form in Old UI.
    */
  private def submitForm(
      deepLinkReturnUrl: String,
      jwt: String,
      info: SectionInfo,
      response: HttpServletResponse
  ): Unit = {
    val formId = "deep_linking_response"

    if (RenderNewTemplate.isNewUIEnabled) {
      // Setting this flag to `true` is important. It will make sure this form is available in the page.
      // Otherwise, this form would be nested under form `eqForm`, which is invalid and the browser will remove this form. So the submit will fail.
      // Check the usage of `LegacyContent#noForm` for details.
      Decorations.getDecorations(info).setExcludeForm(true)
      val form = new FormTag
      form.setId(formId)
      form.setAction(new SimpleFormAction(deepLinkReturnUrl))
      form.addHidden(new HiddenInput("JWT", jwt))
      form.addReadyStatements(new JQueryStatement(JQuerySelector.Type.ID, formId, "submit()"))

      info.getRootRenderContext.setRenderedBody(form)
    } else {
      val formHtml =
        s"""
           |<form method="POST" action="${deepLinkReturnUrl}" id="$formId">
           |  <input type="hidden" name="JWT" value="$jwt">
           |</form>
           |<script>document.getElementById("$formId").submit();</script>
           |""".stripMargin

      response.setContentType("text/html")

      val p = response.getWriter
      p.write(formHtml)
      p.flush()
    }
  }

  // Build a custom call back which will be fired when a selection is either confirmed or cancelled.
  private def buildSelectionMadeCallback(
      deepLinkingRequest: LtiDeepLinkingRequest,
      platformDetails: PlatformDetails,
      response: HttpServletResponse
  ): SelectionsMadeCallback =
    new SelectionsMadeCallback {
      override def executeSelectionsMade(info: SectionInfo, session: SelectionSession): Boolean =
        ltiPlatformService.getPrivateKeyForPlatform(platformDetails.platformId) match {
          case Right((keyId, privateKey)) =>
            val deepLinkingResponse = JWT
              .create()
              .withIssuer(deepLinkingRequest.aud)
              .withAudience(deepLinkingRequest.iss)
              .withIssuedAt(Instant.now)
              .withNotBefore(Instant.now)
              .withExpiresAt(Instant.now.plusSeconds(60))
              .withKeyId(keyId)
              .withClaim(Lti13Claims.MESSAGE_TYPE, LtiMessageType.LtiDeepLinkingResponse.toString)
              .withClaim(Lti13Claims.VERSION, deepLinkingRequest.version)
              .withClaim(OpenIDConnectParams.NONCE, deepLinkingRequest.nonce)
              .withClaim(Lti13Claims.DEPLOYMENT_ID, deepLinkingRequest.deploymentId)
              .withClaim(
                Lti13Claims.CONTENT_ITEMS,
                buildDeepLinkingContentItems(info, session)
              )

            // If `data` is present in the Deep linking settings, include it in the response.
            for {
              d <- deepLinkingRequest.deepLinkingSettings.data
            } yield deepLinkingResponse.withClaim(Lti13Claims.DATA, d)

            val token = deepLinkingResponse.sign(Algorithm.RSA256(privateKey))

            submitForm(
              deepLinkingRequest.deepLinkingSettings.deepLinkReturnUrl.toString,
              token,
              info,
              response
            )
            false // Return `false` so selections are not maintained, which is what `IntegrationSection` does.
          case Left(error) =>
            throw new RuntimeException(
              s"Failed to process selections as unable to find details for provided platform: $error"
            )
        }

      override def executeModalFinished(info: SectionInfo, session: ModalSession): Unit =
        throw new UnsupportedOperationException
    }

  override protected def canSelect(data: Lti13IntegrationSessionData): Boolean = data.isForSelection

  override protected def getIntegrationType = "lti13"

  // Usually, the implementation of this method created in `GenericIntegrationService` will be used because the viewing type
  // is `integ/gen`. So here we don't really need to implement it.
  override def createDataForViewing(info: SectionInfo): Lti13IntegrationSessionData =
    throw new UnsupportedOperationException

  // This method should not be used in the context of LTI 1.3 integration because of the custom selection callback.
  override def getClose(data: Lti13IntegrationSessionData): String =
    throw new UnsupportedOperationException

  override def getCourseInfoCode(data: Lti13IntegrationSessionData): String =
    data.context.map(_.id).orNull

  // This method should not be used in the context of LTI 1.3 integration because of the custom selection callback.
  override def select(
      info: SectionInfo,
      data: Lti13IntegrationSessionData,
      session: SelectionSession
  ): Boolean = throw new UnsupportedOperationException

  override def setupSelectionSession(
      info: SectionInfo,
      data: Lti13IntegrationSessionData,
      session: SelectionSession,
      model: SingleSignonForm
  ): SelectionSession = {
    session.setSelectMultiple(data.deepLinkingSettings.acceptMultiple.getOrElse(true))
    super.setupSelectionSession(info, data, session, model)
  }

  /** Launch Selection Session for LTI 1.3. This is achieved by
    *   1. Build a temporary SectionInfo; 2. Use LTI deep linking request details to build an
    *      IntegrationData and IntegrationActionInfo. 3. Use `IntegrationService#standardForward` to
    *      navigate the page to Selection Session.
    *
    * @param deepLinkingRequest
    *   Deep linking request details providing claims to be used to configure Selection Session.
    * @param platformDetails
    *   Details of the LTI platform to be used to build a JWT.
    * @param req
    *   HTTP Servlet request to be used to build a SectionInfo.
    * @param resp
    *   HTTP Servlet response to be used to build a SectionInfo.
    */
  def launchSelectionSession(
      deepLinkingRequest: LtiDeepLinkingRequest,
      platformDetails: PlatformDetails,
      req: HttpServletRequest,
      resp: HttpServletResponse
  ): Unit = {

    def buildSectionInfo = {
      val section = new AbstractScalaSection {
        // We are not really using any Section so it's fine to use Unit as the model type.
        override type M = Unit
        override def newModel: SectionInfo => Unit = _ => ()
      }
      val sectionNode = new SectionNode("LTI13SelectionSession", section)
      val blankTree   = new DefaultSectionTree(treeRegistry, sectionNode)

      val info = sectionsController.createInfo(
        blankTree,
        "/",
        req,
        resp,
        null,
        Map.empty[String, Array[String]].asJava,
        null
      )

      // For those wondering why saving the callback in Section Tree, the reason being that in cluster environment,
      // the callback, which is an anonymous class, will be serialised as part of `SelectionSession`.
      // This means many things like this Service itself and LtiPlatformService will need to support serialisation.
      // In order not to do this, we must use `TreeLookupSelectionCallback`, which basically is like serialising
      // the index of our callback and using the index to find the real callback when needed.
      // Because this callback is used when a selection is made, we save it in `RootSelectionSection`.
      Option(
        info.lookupSection[RootSelectionSection, RootSelectionSection](
          classOf[RootSelectionSection]
        )
      )
        .flatMap(section => Option(section.getTree)) match {
        case Some(tree) =>
          tree.setAttribute(
            LTI13_INTEGRATION_CALLBACK,
            buildSelectionMadeCallback(deepLinkingRequest, platformDetails, resp)
          )
        case None =>
          throw new RuntimeException(
            s"Missing RootSelectionSection to create a callback for making selections in LTI 1.3 integration."
          )
      }

      info.fireBeforeEvents()
      info
    }

    val integrationData = Lti13IntegrationSessionData(deepLinkingRequest)

    // When Selection Session is launched with LTI 1.3, the display mode is `selectOrAdd` Only.
    // This is because there is no standard way with Deep Linking response to return links targeting
    // different course sections like is done with structured selection sessions.
    integrationService.standardForward(
      buildSectionInfo,
      "",
      integrationData,
      integrationService
        .getActionInfo(IntegrationModule.SELECT_OR_ADD_DEFAULT_ACTION, null),
      new SingleSignonForm,
      new TreeLookupSelectionCallback(LTI13_INTEGRATION_CALLBACK)
    )
  }
}
