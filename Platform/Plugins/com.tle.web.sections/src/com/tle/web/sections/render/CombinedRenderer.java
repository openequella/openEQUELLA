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

import com.tle.common.Check;
import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CombinedRenderer implements SectionRenderable {
  private final List<PreRenderable> results;

  public CombinedRenderer(PreRenderable... results) {
    this(Arrays.asList(results));
  }

  public CombinedRenderer(Collection<? extends PreRenderable> combined) {
    this.results = new ArrayList<PreRenderable>(combined);
  }

  public void addRenderer(PreRenderable result) {
    this.results.add(result);
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(results);
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    for (PreRenderable result : results) {
      if (result instanceof SectionRenderable) {
        ((SectionRenderable) result).realRender(writer);
      }
    }
  }

  public static SectionRenderable combineMultipleResults(PreRenderable... presults) {
    if (presults.length == 0) {
      return null;
    }

    SectionRenderable combineWith = combineResults(presults[0], null);
    for (int i = 1; i < presults.length; i++) {
      combineWith = combineResults(combineWith, presults[i]);
    }
    return combineWith;
  }

  public static SectionRenderable combineMultipleResults(
      Collection<? extends PreRenderable> presults) {
    if (Check.isEmpty(presults)) {
      return null;
    }

    Iterator<? extends PreRenderable> iter = presults.iterator();
    SectionRenderable combineWith = combineResults(iter.next(), null);
    while (iter.hasNext()) {
      combineWith = combineResults(combineWith, iter.next());
    }
    return combineWith;
  }

  public static SectionRenderable combineResults(PreRenderable presult1, PreRenderable presult2) {
    SectionRenderable result1 = null;
    SectionRenderable result2 = null;

    if (presult1 instanceof SectionRenderable) {
      result1 = (SectionRenderable) presult1;
    } else if (presult1 != null) {
      result1 = new PreRenderOnly(presult1);
    }

    if (presult2 instanceof SectionRenderable) {
      result2 = (SectionRenderable) presult2;
    } else if (presult2 != null) {
      result2 = new PreRenderOnly(presult2);
    }

    String name = null;
    CombinedRenderer combined;
    if (result1 == null) {
      return result2;
    }

    if (result2 == null) {
      return result1;
    }

    if (result1 instanceof NamedSectionResult) {
      name = ((NamedSectionResult) result1).getName();
      if (result1 instanceof GenericNamedResult) {
        result1 = ((GenericNamedResult) result1).getInner();
      }
    } else if (result2 instanceof NamedSectionResult) {
      name = ((NamedSectionResult) result2).getName();
      if (result2 instanceof GenericNamedResult) {
        result2 = ((GenericNamedResult) result2).getInner();
      }
    }

    if (result1 instanceof CombinedRenderer) {
      combined = (CombinedRenderer) result1;
      combined.results.add(result2);
    } else {
      combined = new CombinedRenderer(result1, result2);
    }

    if (name != null) {
      return new GenericNamedResult(name, combined);
    }
    return combined;
  }

  @Override
  public String toString() {
    return results.toString();
  }
}
