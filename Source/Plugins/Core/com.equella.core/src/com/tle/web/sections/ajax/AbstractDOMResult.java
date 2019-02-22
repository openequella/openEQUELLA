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

package com.tle.web.sections.ajax;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractDOMResult {
  private String script;
  private Collection<String> css;
  private Collection<String> js;
  private Map<String, Object> formParams;

  public AbstractDOMResult() {
    // nothing
  }

  public AbstractDOMResult(AbstractDOMResult result) {
    setCss(result.getCss());
    setJs(result.getJs());
    setScript(result.getScript());
    setFormParams(result.getFormParams());
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public Collection<String> getCss() {
    return css;
  }

  public void setCss(Collection<String> css) {
    this.css = css;
  }

  public Collection<String> getJs() {
    return js;
  }

  public void setJs(Collection<String> js) {
    this.js = js;
  }

  public Map<String, Object> getFormParams() {
    return formParams;
  }

  public void setFormParams(Map<String, Object> formParams) {
    this.formParams = formParams;
  }
}
