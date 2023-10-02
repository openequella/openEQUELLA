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

package com.tle.web.sections.registry;

import com.tle.annotation.Nullable;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.ForwardEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.generic.DefaultSectionInfo;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ServiceUnavailableException;

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
    // The SectionTree working for Cloud Search is dropped in 2022.1.
    if (path.contains("cloudsearch.do")) {
      throw new ServiceUnavailableException("Cloud search is no longer supported");
    } else if (tree == null) {
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
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void forwardAsBookmark(SectionInfo original, SectionInfo forward) {
    forward.forceRedirect();
    forward(original, forward);
  }

  protected abstract Collection<ExceptionHandlerMatch> getExceptionHandlers();

  public void handleException(
      SectionInfo info, Throwable exception, @Nullable SectionEvent<?> event) {
    if (exception instanceof SectionsRuntimeException) {
      if (exception.getCause() != null) {
        exception = exception.getCause();
      }
    }
    Collection<ExceptionHandlerMatch> handlers = getExceptionHandlers();
    for (ExceptionHandlerMatch handlerMatch : handlers) {
      boolean handle;
      SectionsExceptionHandler handler = null;
      String classMatch = handlerMatch.getClassMatch();
      if (classMatch != null) {
        handle = exception.getClass().getName().equals(classMatch);
      } else {
        handler = handlerMatch.getHandler();
        handle = handler.canHandle(info, exception, event);
      }
      if (handle) {
        if (handler == null) {
          handler = handlerMatch.getHandler();
        }
        // we don't expect to have a non-null handler, but to be sure ..
        if (handler != null) {
          handler.handle(exception, info, this, event);
          return;
        }
        // else continue until we either find a non-null handler, or
        // exit loop and call SectionUtils.throwRuntime
      }
    }
    SectionUtils.throwRuntime(exception);
  }
}
