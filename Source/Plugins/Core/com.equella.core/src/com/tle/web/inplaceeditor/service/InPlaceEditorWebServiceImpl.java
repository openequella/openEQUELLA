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

package com.tle.web.inplaceeditor.service;

import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.appletcommon.AppletWebCommon;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

/** @author Aaron */
@SuppressWarnings("nls")
@Bind(InPlaceEditorWebService.class)
@Singleton
public class InPlaceEditorWebServiceImpl implements InPlaceEditorWebService {
  private static final PluginResourceHelper resources =
      ResourcesService.getResourceHelper(InPlaceEditorWebService.class);

  // com.tle.applet.inplaceeditor provides the codebase ...
  private static String JNLP_URL =
      resources.plugUrl("com.tle.web.inplaceeditor", "inplaceEditor.jnlp");

  private static final String INPLACEEDIT_APPLET_JARBASE_URL =
      resources.plugUrl("com.tle.web.inplaceeditor", "");
  private static final String INPLACEEDIT_APPLET_JAR = "inplaceedit.jar";

  // private static final String INPLACEEDIT_APPLET_JAR_URL =
  // resources.plugUrl("com.tle.applet.inplaceeditor",
  // "inplaceedit.jar");
  public static final String CROSSDOMAINXML_URL = resources.url("crossdomain.xml");

  private static final IncludeFile INPLACE_INCLUDE =
      new IncludeFile(resources.url("scripts/inplaceedit.js"), AppletWebCommon.INCLUDE);

  /**
   * @param $placeholder
   * @param width
   * @param height
   * @param parameters
   * @param id
   */
  private static final JSCallable CREATE_FUNCTION =
      new ExternallyDefinedFunction("inPlaceCreateApplet", INPLACE_INCLUDE);

  private static final JSCallable OPEN_FUNCTION =
      new ExternallyDefinedFunction("inPlaceOpen", INPLACE_INCLUDE);
  /**
   * @param appletId
   * @param submitCallback
   * @param beingUploadedMessageCallback
   * @param changesDetectedConfirmationMessageCallback
   */
  private static final JSCallable CHECK_SYNCED_FUNCTION =
      new ExternallyDefinedFunction("inPlaceCheckSynced", INPLACE_INCLUDE);
  /**
   * @param $editLinks
   * @param $openWithLink
   */
  private static final JSCallable INIT_INPLACE_FUNCTION =
      new ExternallyDefinedFunction("initInPlace", INPLACE_INCLUDE);

  @Inject private InstitutionService institutionService;
  @Inject private ConfigurationService configService;
  @Inject private MimeTypeService mimeTypeService;

  private final JSFunction beingUploadedMessageCallback =
      new RuntimeFunction() {
        @Override
        protected JSCallable createFunction(RenderContext info) {
          return CallAndReferenceFunction.get(
              Js.function(new ReturnStatement(resources.getString("alert.beinguploaded"))),
              new SimpleElementId("bup"));
        }
      };
  private final JSFunction changesDetectedConfirmationMessageCallback =
      new RuntimeFunction() {
        @Override
        protected JSCallable createFunction(RenderContext info) {
          return CallAndReferenceFunction.get(
              Js.function(new ReturnStatement(resources.getString("confirm.changesdetected"))),
              new SimpleElementId("cdt"));
        }
      };

  @Override
  public JSStatements createHideLinksStatements(
      JQuerySelector divSelector, JQuerySelector openWithLinkSelector) {
    return Js.call_s(INIT_INPLACE_FUNCTION, divSelector, openWithLinkSelector);
  }

  @Override
  public JSCallable createAppletFunction(
      String appletId,
      ItemId itemId,
      String stagingId,
      String filename,
      boolean openWith,
      String service,
      JQuerySelector divSelector,
      String width,
      String height) {
    final ObjectExpression options = new ObjectExpression();
    options.put(AppletWebCommon.PARAMETER_PREFIX + "SERVICE", service);
    options.put(AppletWebCommon.PARAMETER_PREFIX + "STAGINGID", stagingId);
    if (itemId != null) {
      options.put(AppletWebCommon.PARAMETER_PREFIX + "ITEMUUID", itemId.getUuid());
      options.put(AppletWebCommon.PARAMETER_PREFIX + "ITEMVERSION", itemId.getVersion());
    }
    options.put(AppletWebCommon.PARAMETER_PREFIX + "OPENWITH", openWith);
    options.put(AppletWebCommon.PARAMETER_PREFIX + "FILENAME", filename);
    options.put(
        AppletWebCommon.PARAMETER_PREFIX + "MIMETYPE",
        mimeTypeService.getMimeTypeForFilename(filename));
    options.put(AppletWebCommon.PARAMETER_PREFIX + "DEBUG", configService.isDebuggingMode());
    options.put(
        AppletWebCommon.PARAMETER_PREFIX + "CROSSDOMAIN",
        institutionService.institutionalise(CROSSDOMAINXML_URL));
    options.put(AppletWebCommon.PARAMETER_PREFIX + "LOCALE", CurrentLocale.getLocale().toString());
    options.put(
        AppletWebCommon.PARAMETER_PREFIX + "ENDPOINT",
        institutionService.getInstitutionUrl().toString());
    options.put(AppletWebCommon.PARAMETER_PREFIX + "INSTANCEID", UUID.randomUUID().toString());

    options.put("jnlp_href", institutionService.institutionalise(JNLP_URL));
    options.put("code", "com.tle.web.inplaceeditor.InPlaceEditAppletLauncher");

    final String base = institutionService.institutionalise(INPLACEEDIT_APPLET_JARBASE_URL);
    options.put("codebase", base);
    options.put("archive", base + INPLACEEDIT_APPLET_JAR);

    return CallAndReferenceFunction.get(
        Js.function(Js.call_s(CREATE_FUNCTION, divSelector, width, height, options, appletId)),
        new SimpleElementId("cap"));
  }

  @Override
  public JSHandler createOpenHandler(
      String appletId, boolean openWith, JSFunction noAppletCallback) {
    return new OverrideHandler(OPEN_FUNCTION, appletId, noAppletCallback, openWith);
  }

  @Override
  public JSHandler createUploadHandler(String appletId, JSFunction doneUploadingCallback) {
    return new OverrideHandler(
        CHECK_SYNCED_FUNCTION,
        appletId,
        doneUploadingCallback,
        beingUploadedMessageCallback,
        changesDetectedConfirmationMessageCallback);
  }
}
