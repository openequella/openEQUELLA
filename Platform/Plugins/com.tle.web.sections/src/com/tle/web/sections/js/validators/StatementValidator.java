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

package com.tle.web.sections.js.validators;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;

public class StatementValidator implements JSValidator {
  private final JSStatements validatorStatements;
  private boolean returnFalse;

  public StatementValidator(JSStatements statements) {
    this.validatorStatements = statements;
  }

  public boolean isReturnFalse() {
    return returnFalse;
  }

  public StatementValidator setReturnFalse(boolean returnFalse) {
    this.returnFalse = returnFalse;
    return this;
  }

  @Override
  public JSValidator setFailureStatements(JSStatements statements) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getStatements(RenderContext info) {
    return validatorStatements.getStatements(info)
        + (returnFalse ? "return false;" : ""); // $NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void preRender(PreRenderContext info) {
    SectionUtils.preRender(info, validatorStatements);
  }
}
