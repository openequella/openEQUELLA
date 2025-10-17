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

package com.tle.web.portal.renderer;

import static com.tle.web.sections.equella.js.StandardExpressions.FORM_NAME;

import com.tle.common.portal.entity.Portlet;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.equella.js.StandardExpressions;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.header.MutableHeaderHelper;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.template.RenderNewTemplate;

public abstract class PortletContentRenderer<M> extends AbstractPrototypeSection<M>
    implements HtmlRenderer, ViewableChildInterface {
  protected Portlet portlet;

  public void setPortlet(Portlet portlet) {
    this.portlet = portlet;
    // Dodgy call to prevent #5714
    this.portlet.getAttributes();
  }

  /**
   * Sets up the context required to support correctly rendering a Legacy portlet Section in the new
   * Dashboard, including:
   * <li>Setting a unique form ID for the target portlet;
   * <li>Preparing the JavaScript function used to access the form;
   * <li>Preparing the JavaScript functions used to submit the form;
   *
   *     <p>IMPORTANT: This method must be called whenever a Legacy portlet Section is used in the
   *     new Dashboard. This includes two use cases:
   * <li>Retrieving the content of a portlet;
   * <li>Triggering an event defined in any portlet;
   *
   *     <p>Note: It might be possible to use a certain Legacy event listener to do this
   *     automatically, but this approach has many unknowns and caused incorrect set up during the
   *     investigation stage. Also, explicitly calling this method where necessary will help void
   *     potential confusion in the future.
   */
  public void setupForNewDashboard(RenderContext ctx) {
    if (RenderNewTemplate.isNewUIEnabled()) {
      String formId = FORM_NAME + '-' + portlet.getUuid();
      ctx.getForm().setId(formId);

      MutableHeaderHelper helper = (MutableHeaderHelper) ctx.getHelper();
      // `ELEMENT_FUNCTION` refers to the JS function `_e` defined in `standard.js`.
      helper.setFormExpression(
          new FunctionCallExpression(StandardExpressions.ELEMENT_FUNCTION, formId));
      // There are four types of submit functions where:
      // - The first one refers to standard form submission with form validation;
      // - The second one is similar to the first one but without form validation;
      // - The use of third and the fourth one is unclear but they seem to be exactly the same as
      // the first and
      //   second one, according to `LegacyContentApi`;
      // And these functions are defined as part of a JS object named `EQ-<portlet-uuid>`.
      String eqObject = String.format("window['EQ-%s']", portlet.getUuid());
      helper.setSubmitFunctions(
          new ExternallyDefinedFunction(eqObject + ".event"),
          new ExternallyDefinedFunction(eqObject + ".eventnv"),
          new ExternallyDefinedFunction(eqObject + ".event"),
          new ExternallyDefinedFunction(eqObject + ".eventnv"));
    }
  }
}
