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

package com.tle.web.sections.result.util;

import com.tle.web.sections.render.Label;
import java.io.Serializable;

public class SubstitutionLabel implements Label, Serializable {
  private static final long serialVersionUID = 1L;

  private final Label base;
  private final String pattern;
  private final String replacement;
  private final boolean useRegex;

  public SubstitutionLabel(Label base, String pattern, String replacement) {
    this(base, pattern, replacement, true);
  }

  public SubstitutionLabel(Label base, String pattern, String replacement, boolean useRegex) {
    this.base = base;
    this.pattern = pattern;
    this.replacement = replacement;
    this.useRegex = useRegex;
  }

  @Override
  public String getText() {
    return useRegex
        ? base.getText().replaceAll(pattern, replacement)
        : base.getText().replace(pattern, replacement);
  }

  @Override
  public boolean isHtml() {
    return base.isHtml();
  }
}
