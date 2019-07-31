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

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;

/**
 * A JSStatements where the contents can be decided at runtime.
 *
 * @author jolz
 */
@NonNullByDefault
public class RuntimeStatement implements JSStatements {
  @Override
  public String getStatements(RenderContext info) {
    return getRealStatements(info).getStatements(info);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(getRealStatements(info));
  }

  protected JSStatements getRealStatements(RenderContext info) {
    JSStatements statements = info.getAttribute(this);
    if (statements == null) {
      statements = createStatements(info);
      info.setAttribute(this, statements);
    }
    return statements;
  }

  public void setStatements(SectionInfo info, JSStatements statements) {
    info.setAttribute(this, statements);
  }

  protected JSStatements createStatements(RenderContext info) {
    throw new SectionsRuntimeException(
        "Statements not set and createStatements() not overridden"); //$NON-NLS-1$
  }
}
