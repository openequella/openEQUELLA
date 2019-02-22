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

package com.tle.web.sections.registry;

import com.google.common.base.Throwables;
import com.tle.annotation.Nullable;
import com.tle.web.sections.*;
import com.tle.web.sections.events.*;
import com.tle.web.sections.generic.DefaultSectionInfo;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractSectionsController implements SectionsController {

  @Override
  public void execute(SectionInfo info) {
    MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
    if (minfo == null) {
      throw new Error("No MutableSectionInfo attribute in info");
    }
    try {
      minfo.fireBeforeEvents();
      minfo.processQueue();
      renderForward(minfo);
    } catch (Exception ex) {
      handleException(minfo, ex, null);
      return;
    }
  }

  private void renderForward(MutableSectionInfo info) {
    boolean redirect = info.isForceRedirect() || (!info.isForceRender() && isPosted(info));
    info.fireReadyToRespond(redirect);
    if (!info.isRendered()) {
      if (redirect) {
        forwardToUrl(info, info.getPublicBookmark().getHref(), 303);
      } else {
        renderFromRoot(info);
      }
    }
  }

  protected void renderFromRoot(SectionInfo info) {
    RenderContext renderContext = info.getRootRenderContext();
    String rootId = info.getRootId();
    RenderEvent renderEvent =
        new RenderEvent(renderContext, rootId, renderContext.getRootResultListener());
    info.processEvent(renderEvent);
  }

  private boolean isPosted(MutableSectionInfo info) {
    final HttpServletRequest request = info.getRequest();
    if (request != null) {
      return request.getMethod().equalsIgnoreCase("POST");
    }
    return false;
  }

  @Override
  public void forward(SectionInfo original, SectionInfo forward) {
    original.renderNow();
    original.setRendered();
    execute(forward);
  }

  @Override
  public boolean treeExistsForUrlPath(String path) {
    return getTreeForPath(path) != null;
  }

  protected abstract SectionTree getTreeForPath(String path);

  @Override
  public MutableSectionInfo createInfoFromTree(SectionTree tree, SectionInfo info) {
    String path = (String) info.getAttribute(SectionInfo.KEY_PATH);
    if (path == null) {
      throw new Error("No path attribute in info");
    }
    return createInfo(tree, path, info.getRequest(), info.getResponse(), info, null, null);
  }

  @Override
  public MutableSectionInfo createInfo(
      String path,
      @Nullable HttpServletRequest request,
      @Nullable HttpServletResponse response,
      @Nullable SectionInfo info,
      @Nullable Map<String, String[]> params,
      @Nullable Map<Object, Object> attrs) {
    SectionTree tree = getTreeForPath(path);
    if (tree == null) {
      throw new SectionsRuntimeException("There is no tree for:" + path);
    }
    return createInfo(tree, path, request, response, info, params, attrs);
  }

  @Override
  public MutableSectionInfo createUnfilteredInfo(
      SectionTree tree,
      HttpServletRequest request,
      HttpServletResponse response,
      Map<Object, Object> attrs) {
    MutableSectionInfo sectionInfo = new DefaultSectionInfo(this);
    sectionInfo.setRequest(request);
    sectionInfo.setResponse(response);
    if (attrs != null) {
      for (Map.Entry<?, ?> entry : attrs.entrySet()) {
        sectionInfo.setAttribute(entry.getKey(), entry.getValue());
      }
    }
    sectionInfo.addTree(tree);
    sectionInfo.queueTreeEvents(tree);
    return sectionInfo;
  }

  protected abstract List<SectionFilter> getSectionFilters();

  @Override
  public final MutableSectionInfo createInfo(
      SectionTree tree,
      String path,
      @Nullable HttpServletRequest request,
      @Nullable HttpServletResponse response,
      @Nullable SectionInfo info,
      @Nullable Map<String, String[]> params,
      @Nullable Map<Object, Object> attributes) {
    MutableSectionInfo sectionInfo = createUnfilteredInfo(tree, request, response, attributes);
    sectionInfo.setAttribute(SectionInfo.KEY_PATH, path);
    sectionInfo.setAttribute(SectionInfo.KEY_FORWARDFROM, info);
    List<SectionFilter> filters = getSectionFilters();
    for (SectionFilter sectionFilter : filters) {
      sectionFilter.filter(sectionInfo);
      if (sectionInfo.isRendered()) {
        break;
      }
    }
    try {
      if (info != null) {
        info.processEvent(new ForwardEvent(sectionInfo));
      }
      ParametersEvent paramsEvent = new ParametersEvent(params, true);
      sectionInfo.addParametersEvent(paramsEvent);
      sectionInfo.processEvent(paramsEvent);
      return sectionInfo;
    } catch (Exception ex) {
      handleException(sectionInfo, ex, null);
      return sectionInfo;
    }
  }

  @Override
  public SectionInfo createForward(String path) {
    return createInfo(
        path,
        null,
        null,
        null,
        null,
        Collections.singletonMap(SectionInfo.KEY_FOR_URLS_ONLY, true));
  }

  @Override
  public SectionInfo createForward(SectionInfo info, String url) {
    Map<String, String[]> params = SectionUtils.parseParamUrl(url);
    String path = params.remove(SectionInfo.KEY_PATH)[0];
    return createInfo(path, info.getRequest(), info.getResponse(), info, params, null);
  }

  @Override
  public void forwardToUrl(SectionInfo info, String link, int code) {
    info.setRendered();
    try {
      HttpServletResponse response = info.getResponse();
      if (response == null) {
        throw new Error("info not bound to a request/response");
      }
      response.setStatus(code);
      response.setHeader("Location", link);
      response.flushBuffer();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void forwardAsBookmark(SectionInfo original, SectionInfo forward) {
    forward.forceRedirect();
    forward(original, forward);
  }

  public abstract void handleException(
      SectionInfo info, Throwable exception, @Nullable SectionEvent<?> event);
}
