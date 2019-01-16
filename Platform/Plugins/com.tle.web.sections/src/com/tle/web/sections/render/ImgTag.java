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

package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;

@SuppressWarnings("nls")
public class ImgTag extends TagRenderer {
  private final String srcUrl;

  public ImgTag(TagState tagState, String srcUrl) {
    super("img", tagState);
    this.srcUrl = srcUrl;
  }

  public ImgTag(String srcUrl) {
    super("img", new TagState());
    this.srcUrl = srcUrl;
  }

  @Override
  protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException {
    Map<String, String> as = super.prepareAttributes(writer);
    as.put("src", srcUrl);
    return as;
  }
}
