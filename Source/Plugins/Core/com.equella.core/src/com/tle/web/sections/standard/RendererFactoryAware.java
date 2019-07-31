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

package com.tle.web.sections.standard;

/**
 * Renderers that need the render factory to render child components can implement this. A good
 * example of this is the Shuffle Box control that needs to render buttons in the middle; however,
 * the default button renderer may be over-ridden, so it asks the RenderFactory to do the work for
 * it.
 *
 * @author Nick
 */
public interface RendererFactoryAware {
  void setRenderFactory(RendererFactory rendererFactory);
}
