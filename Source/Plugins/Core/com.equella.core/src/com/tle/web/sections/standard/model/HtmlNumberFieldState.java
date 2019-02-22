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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;

public class HtmlNumberFieldState extends HtmlValueState {
  private Number min;
  private Number max;
  private Number step;
  private boolean anyStep;

  public HtmlNumberFieldState() {
    super(RendererConstants.NUMBERFIELD);
  }

  public Number getMin() {
    return min;
  }

  public void setMin(Number min) {
    this.min = min;
  }

  public Number getMax() {
    return max;
  }

  public void setMax(Number max) {
    this.max = max;
  }

  /** Mutually exclusive with anyStep */
  public Number getStep() {
    return step;
  }

  /**
   * Mutually exclusive with anyStep
   *
   * @param step
   */
  public void setStep(Number step) {
    this.step = step;
  }

  /**
   * If true, mutually exclusive with step. If step is not null it will be used in preference to
   * anyStep.
   *
   * @param anyStep
   */
  public boolean isAnyStep() {
    return anyStep;
  }

  /**
   * If true, mutually exclusive with step. If step is not null it will be used in preference to
   * anyStep.
   *
   * @param anyStep
   */
  public void setAnyStep(boolean anyStep) {
    this.anyStep = anyStep;
  }
}
