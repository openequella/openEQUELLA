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

package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.standard.model.HtmlPagerState;
import it.uniroma3.mat.extendedset.wrappers.LongSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * For paging of search results |< First < Prev | 1 2 <b>3</b> 4 5 6 7 8 9 | Next > Last >|
 *
 * @author aholland
 */
public class Pager extends AbstractRenderedComponent<HtmlPagerState>
    implements ParametersEventListener, BookmarkEventListener {
  public Pager() {
    super(RendererConstants.PAGER);
  }

  @Override
  public Class<HtmlPagerState> getModelClass() {
    return HtmlPagerState.class;
  }

  public int getCurrentPage(SectionInfo info) {
    return getState(info).getCurrent();
  }

  public void setCurrentPage(SectionInfo info, int page) {
    getState(info).setCurrent(page);
  }

  public int getStartPage(SectionInfo info) {
    return getState(info).getStartPage();
  }

  public int getEndPage(SectionInfo info) {
    return getState(info).getEndPage();
  }

  public int getLastPage(SectionInfo info) {
    return getState(info).getLastPage();
  }

  public List<Long> setupAndGetPage(
      SectionInfo info, LongSet bitSet, int perPage, int maxDisplayed) {
    int availablePages = (int) ((bitSet.size() - 1) / perPage) + 1;
    HtmlPagerState state = getState(info);
    int current = state.getCurrent();
    if (current < 1) {
      current = 1;
    }
    if (current > availablePages) {
      current = availablePages;
    }
    state.setCurrent(current);
    List<Long> newList = new ArrayList<Long>();
    int offset = (current - 1) * perPage;
    Iterator<Long> iter = bitSet.iterator();
    while (iter.hasNext()) {
      Long next = iter.next();
      if (offset > 0) {
        offset--;
      } else {
        newList.add(next);
        perPage--;
        if (perPage == 0) {
          break;
        }
      }
    }
    setup(info, availablePages, maxDisplayed);
    return newList;
  }

  public <T> List<T> setupAndGetPage(
      SectionInfo info, Collection<T> allData, int perPage, int maxDisplayed) {
    int availablePages = ((allData.size() - 1) / perPage) + 1;
    HtmlPagerState state = getState(info);
    int current = state.getCurrent();
    if (current < 1) {
      current = 1;
    }
    if (current > availablePages) {
      current = availablePages;
    }
    state.setCurrent(current);
    List<T> newList = new ArrayList<T>();
    int offset = (current - 1) * perPage;
    Iterator<T> iter = allData.iterator();
    while (iter.hasNext()) {
      T next = iter.next();
      if (offset > 0) {
        offset--;
      } else {
        newList.add(next);
        perPage--;
        if (perPage == 0) {
          break;
        }
      }
    }
    setup(info, availablePages, maxDisplayed);
    return newList;
  }

  public void setup(SectionInfo info, int availablePages, int maxDisplayed) {
    getState(info).setup(availablePages, maxDisplayed);
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) {
    HtmlPagerState state = getState(info);
    if (event.hasParameter(getParameterId())) {
      state.setCurrent(event.getIntParameter(getParameterId(), true));
    }
  }

  @Override
  public void bookmark(SectionInfo info, BookmarkEvent event) {
    if (addToThisBookmark(info, event)) {
      HtmlPagerState state = getState(info);
      if (state.getCurrent() != 0) {
        event.setParam(getParameterId(), Integer.toString(state.getCurrent()));
      }
    }
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    addDocumentedParam(event, getParameterId(), Integer.TYPE.getName());
  }
}
