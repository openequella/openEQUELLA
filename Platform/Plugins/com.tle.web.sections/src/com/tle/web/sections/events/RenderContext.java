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

package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import java.util.Collection;

@NonNullByDefault
public interface RenderContext extends SectionInfo {
  /**
   * The "Modal-id" is the id of a Section which the Root Template section will render.
   *
   * @return
   */
  String getModalId();

  void setModalId(String modalId);

  /**
   * The "Semi-Modal id" is an application specific Section id, which will generally be used by the
   * Root Application Section, in order to wrap a "Modal" operation with other markup.
   *
   * @return The Semi-Modal id
   */
  String getSemiModalId();

  void setSemiModalId(String semiModalId);

  SectionResult getRenderedBody();

  void setRenderedBody(SectionResult renderedBody);

  RenderResultListener getRootResultListener();

  void setRootResultListener(RenderResultListener rootResultListener);

  /**
   * Get the html result.
   *
   * <p>
   *
   * <pre>
   * HTTP/1.1 200
   *
   * ${result}
   * </pre>
   *
   * @return
   */
  SectionRenderable getRenderedResponse();

  void setRenderedResponse(SectionRenderable renderedResponse);

  HeaderHelper getHelper();

  boolean isRenderHeader();

  FormTag getForm();

  BodyTag getBody();

  @Deprecated
  PreRenderContext getPreRenderContext();

  @Deprecated
  void preRender(Collection<? extends PreRenderable> preRenderers);

  @Deprecated
  void preRender(PreRenderable preRenderer);

  @Deprecated
  void preRender(PreRenderable... preRenderers);
}
