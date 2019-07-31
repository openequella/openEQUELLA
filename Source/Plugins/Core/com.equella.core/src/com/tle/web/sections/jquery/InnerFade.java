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

package com.tle.web.sections.jquery;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.expression.AbstractExpression;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.TagProcessor;
import com.tle.web.sections.render.TagState;
import java.util.Map;

@SuppressWarnings("nls")
public class InnerFade extends AbstractExpression {
  private static final String THUMBFADE_CLASS = "thumbfade";
  private static final TagProcessor GLOBALFADE =
      new TagProcessor() {
        private final JQueryStatement STATEMENT =
            new JQueryStatement(
                JQuerySelector.Type.CLASS, THUMBFADE_CLASS, new InnerFade("{speed: 750}"));

        @Override
        public void preRender(PreRenderContext info) {
          info.addReadyStatements(STATEMENT);
        }

        @Override
        public void processAttributes(SectionWriter writer, Map<String, String> attrs) {
          // nothing
        }
      };

  private static final String JS_URL =
      ResourcesService.getResourceHelper(InnerFade.class).url("jquerylib/jquery.innerfade.js");

  public static final PreRenderable PRERENDER = new InnerFade();

  private final Object options;

  public InnerFade() {
    this("");
  }

  public InnerFade(String options) {
    this.options = options;
  }

  @Override
  public String getExpression(RenderContext info) {
    return "innerfade(" + options + ")";
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(JQueryCore.PRERENDER);
    info.addJs(JS_URL);
  }

  public static void fade(TagState tag) {
    tag.addClass(THUMBFADE_CLASS);
    tag.addTagProcessor(GLOBALFADE);
  }
}
