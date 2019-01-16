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

package com.tle.web.sections;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.Utils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.events.RenderResultListener;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.registry.handler.AnnotatedBookmarkScanner;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionIdRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.SingleResultCollector;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.render.WrappedLabelRenderer;

/**
 * A class containing utility methods for common operations.
 *
 * <p>Some of these methods such as {@link #preRender(RenderContext, Collection)} and the other
 * {@code preRender} methods probably could be moved into a more relevant place in the future.
 *
 * @author jmaginnis
 */
@NonNullByDefault
@SuppressWarnings("nls")
public final class SectionUtils {
  private static final String UNIQUEIDSS_KEY = "$UNIQUE_ID$";
  private static final String RENDEREDIDS_KEY = "$RENDERED_IDS$";
  private static final String CHARSET_ENCODING = "UTF-8";

  // private static final Pattern URL_ENCODE_ANCHORS = Pattern.compile("^(.*)%23([^/]*?)$");
  // //$NON-NLS-1$

  /**
   * Create an XML/HTML attribute string from a map of strings.
   *
   * <p>E.g. With a map such as:
   *
   * <pre>
   * map.put("class", "info");
   * map.put("id", "myid");
   *
   * SectionUtils.mapToAttributes(map)
   * </pre>
   *
   * will return:
   *
   * <pre>
   * class="info" id="myid"
   * </pre>
   *
   * @param attributes The attribute map
   * @return The string representing the map
   */
  public static String mapToAttributes(Map<String, String> attributes) {
    StringBuilder sbuf = new StringBuilder();
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      String value = entry.getValue();
      if (value != null) {
        sbuf.append(' ');
        sbuf.append(Utils.ent(entry.getKey()));
        sbuf.append("=\"");
        sbuf.append(Utils.ent(value));
        sbuf.append('"');
      }
    }
    return sbuf.toString();
  }

  /**
   * Dispatch to a given method on a section and return the result of the call.
   *
   * <p>The method must take a single parameter of type {@code SectionContext}.
   *
   * @param method The method name
   * @param section The section instance to find the method on
   * @param context The context
   * @return The return value from the resulting method call
   * @throws Exception
   */
  @Nullable
  public static Object dispatchToMethod(String method, Section section, SectionContext context)
      throws Exception {
    try {
      Method dispMethod = section.getClass().getMethod(method, SectionContext.class);
      return dispMethod.invoke(section, context);
    } catch (NoSuchMethodException e) {
      if (!method.equals("unimplemented")) {
        return dispatchToMethod("unimplemented", section, context);
      }
      // else
      throw e;
    } catch (InvocationTargetException e) {
      Throwable t = e.getCause();
      if (t instanceof Exception) {
        throw (Exception) t;
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * Dispatch to a given method on an object (usually a {@code Section}) and return the result of
   * the call.
   *
   * <p>The method must take a single parameter of type {@code SectionInfo}.
   *
   * @param method The method name
   * @param section The object to find the method on, usually this is a {@code Section}
   * @param info The current info
   * @return The return value from the resulting method call
   */
  @Nullable
  public static Object dispatchToMethod(String method, Object section, SectionInfo info) {
    try {
      Method dispMethod;
      try {
        dispMethod = section.getClass().getMethod(method, SectionInfo.class);
      } catch (NoSuchMethodException nsme) {
        dispMethod = section.getClass().getMethod(method, RenderContext.class);
      }
      return dispMethod.invoke(section, info);
    } catch (Exception e) {
      SectionUtils.throwRuntime(e);
    }
    return null;
  }

  public static boolean isDefaultValue(Object obj) {
    if (obj instanceof Boolean) {
      return !((Boolean) obj).booleanValue();
    }
    if (obj instanceof Number) {
      return ((Number) obj).longValue() == 0;
    }
    return false;
  }

  public static List<NameValue> getParameterNameValues(Map<?, ?> map, boolean ignoreDefaults) {
    List<NameValue> nvs = new ArrayList<NameValue>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String strName = entry.getKey().toString();
      Collection<?> values = Collections.emptyList();

      Object obj = entry.getValue();
      if (obj != null && (!ignoreDefaults || !isDefaultValue(obj))) {
        if (obj instanceof Object[]) {
          values = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
          values = (Collection<?>) obj;
        } else {
          values = Collections.singleton(obj);
        }
      }

      for (Object val : values) {
        if (val != null) {
          String element = val.toString();
          nvs.add(new NameValue(strName, element));
        }
      }
    }
    return nvs;
  }

  /**
   * Render a {@link SectionRenderable} to a given {@code Writer}.
   *
   * @param info The current info
   * @param renderer The {@code SectionRenderable} to render
   * @param writer The writer to use
   * @throws IOException
   */
  public static void renderToWriter(
      RenderContext info, @Nullable SectionRenderable renderer, Writer writer) throws IOException {
    if (renderer != null) {
      SectionWriter sWriter;
      if (writer instanceof SectionWriter) {
        sWriter = (SectionWriter) writer;
      } else {
        sWriter = new SectionWriter(writer, info);
      }
      sWriter.preRender(renderer);
      renderer.realRender(sWriter);
    }
  }

  /**
   * Render a {@code SectionRenderable} and return the result as a String.
   *
   * @param info The current info
   * @param renderer The {@code SectionRenderable} to render
   * @return The result as a string
   */
  public static String renderToString(RenderContext info, @Nullable SectionRenderable renderer) {
    StringWriter sWriter = new StringWriter();
    try {
      renderToWriter(info, renderer, sWriter);
    } catch (IOException e) {
      throw new SectionsRuntimeException(e);
    }
    return sWriter.toString();
  }

  /**
   * Create a query string from a list of {@code NameValue}s.
   *
   * <p>E.g.
   *
   * <pre>
   * nvs.add(new NameValue("param1", "value");
   * nvs.add(new NameValue("param2", "value2");
   * nvs.add(new NameValue("param1", "value3");
   *
   * SectionUtils.getParameterString(nvs)
   * </pre>
   *
   * returns <code>param1=value&amp;param2=value2&amp;param1=value3</code>
   *
   * @param nvs The name/value list
   * @return The query string
   */
  public static String getParameterString(List<NameValue> nvs) {
    StringBuilder parameters = new StringBuilder();

    boolean first = true;
    for (NameValue nv : nvs) {
      if (!first) {
        parameters.append("&");
      } else {
        first = false;
      }

      try {
        String encName = URLEncoder.encode(nv.getName(), CHARSET_ENCODING);
        String encValue = URLEncoder.encode(nv.getValue(), CHARSET_ENCODING);
        parameters.append(encName);
        parameters.append('=');
        parameters.append(encValue);
      } catch (UnsupportedEncodingException ex) {
        // This should never happen.... ever.
        // We can ensure that CHARSET_ENCODING will be supported.
        throw new RuntimeException("Problem encoding URLs as " + CHARSET_ENCODING);
      }
    }
    return parameters.toString();
  }

  /**
   * Parse a full/partial url and return it as a map.
   *
   * <p>Anything before the query string will go into the map using the special key {@link
   * SectionInfo#KEY_PATH}.
   *
   * <p>E.g.
   *
   * <pre>
   * Map&lt;String, String[]&gt; map = SectionUtils.parseParamUrl(&quot;/access/test.do?method=hello&quot;);
   * String path = map.get(SectionInfo.PATH_KEY)[0];
   * String method = map.get(&quot;method&quot;)[0];
   * </pre>
   *
   * <code>path</code> will be <code>"/access/test.do"</code><br>
   * <code>method</code> will be <code>"hello"</code>
   *
   * @see #parseParamString(String)
   * @param url The url to parse
   * @return The map of name to values
   */
  public static Map<String, String[]> parseParamUrl(String url) {
    return parseParamUrl(url, false);
  }

  /**
   * Some URLs in the system (youtube URLs being among the culprits) are non-standard, in that there
   * is no '?' between the url path and the query parameters, simply a leading '&', eg
   * http://www.youtube.com&version=1&youcantbe=serious
   */
  public static Map<String, String[]> parseParamUrl(String url, boolean tolerateAmphersandOnly) {
    Map<String, String[]> params;
    int qInd = url.indexOf('?');
    if (qInd == -1 && tolerateAmphersandOnly) {
      // look for first index of '&'
      qInd = url.indexOf('&');
    }

    if (qInd != -1) {
      String query = url.substring(qInd + 1);
      params = parseParamString(query);
      url = url.substring(0, qInd);
    } else {
      params = new LinkedHashMap<String, String[]>();
    }
    params.put(SectionInfo.KEY_PATH, new String[] {url});
    return params;
  }

  /**
   * Parse a query string and return a map of parameters.
   *
   * <p>Each unique parameter has an array of values stored in the map.
   *
   * <p>E.g.
   *
   * <pre>
   * Map&lt;String, String[]&gt; map = parseParamString(&quot;param1=value1&lt;param2=value2&amp;param1=value3&quot;);
   * </pre>
   *
   * will return a map containing:
   *
   * <pre>
   * param1 -> [value1, value3]
   * param2 -> [value2]
   * </pre>
   *
   * @param query The query string to parse
   * @return A map of name to value arrays
   */
  public static Map<String, String[]> parseParamString(String query) {
    Map<String, String[]> paramMap = new LinkedHashMap<String, String[]>();

    if (!Check.isEmpty(query)) {
      try {
        String[] qparams = query.split("&");
        for (String qparam : qparams) {
          String[] nameVal = qparam.split("=");
          String name = URLDecoder.decode(nameVal[0], CHARSET_ENCODING);
          String value;
          if (nameVal.length > 1) {
            value = URLDecoder.decode(nameVal[1], CHARSET_ENCODING);
          } else {
            value = "";
          }
          String[] current = paramMap.get(name);
          if (current != null) {
            String[] newstrs = new String[current.length + 1];
            System.arraycopy(current, 0, newstrs, 0, current.length);
            newstrs[current.length] = value;
            current = newstrs;
          } else {
            current = new String[] {value};
          }
          paramMap.put(name, current);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return paramMap;
  }

  /**
   * Escape a key.
   *
   * <p>Current this escapes the following characters: "()[]\"
   *
   * @param key The key to escape
   * @return The escaped key
   */
  public static String keyEscape(String key) {
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (c == ')') {
        sbuf.append("\\1");
      } else if (c == '[') {
        sbuf.append("\\2");
      } else if (c == ']') {
        sbuf.append("\\3");
      } else if (c == '\\') {
        sbuf.append("\\\\");
      } else if (c == '(') {
        sbuf.append("\\4");
      } else {
        sbuf.append(c);
      }
    }
    return sbuf.toString();
  }

  /**
   * Unescape a key, escaped with {@link #keyEscape(String)}.
   *
   * @param key The escaped key
   * @return The unescaped key
   */
  public static String keyUnEscape(String key) {
    StringBuilder sbuf = new StringBuilder();
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (c == '\\') {
        c = key.charAt(++i);
        if (c == '1') {
          c = ')';
        } else if (c == '2') {
          c = '[';
        } else if (c == '3') {
          c = ']';
        } else if (c == '4') {
          c = '(';
        }
      }
      sbuf.append(c);
    }
    return sbuf.toString();
  }

  /**
   * Entity encode a string.
   *
   * <p>Called "ent" for historical reason.
   *
   * @param szStr The string to encode
   * @return The encoded string
   */
  public static String ent(@Nullable String szStr) {
    StringBuilder szOut = new StringBuilder();
    if (szStr == null) return szOut.toString();
    for (int i = 0; i < szStr.length(); i++) {
      char ch = szStr.charAt(i);
      switch (ch) {
        case '<':
          szOut.append("&lt;");
          break;

        case '>':
          szOut.append("&gt;");
          break;

        case '&':
          szOut.append("&amp;");
          break;

        case '"':
          szOut.append("&quot;");
          break;

        default:
          if (ch < 128) {
            szOut.append(ch);
          } else {
            szOut.append("&#");
            szOut.append((int) ch);
            szOut.append(';');
          }
          break;
      }
    }

    return szOut.toString();
  }

  /**
   * "Pre"-renders a {@code Collection} of {@code PreRenderable}s.
   *
   * @see #preRender(RenderContext, PreRenderable)
   * @param info The current info
   * @param preRenderers The Collection of {@code PreRenderable}s
   */
  @Deprecated
  public static void preRender(
      RenderContext info, Collection<? extends PreRenderable> preRenderers) {
    info.preRender(preRenderers);
  }

  /**
   * "Pre"-renders a {@link PreRenderable}, provided it hasn't already been.
   *
   * <p>A check is done with the {@link HeaderHelper} to see if the given {@code PreRenderable} has
   * already been "Pre"-rendered, and if not it calls {@link
   * PreRenderable#preRender(PreRenderContext)}.
   *
   * @param info The current info
   * @param preRenderer The {@code PreRenderable} to "Pre"-render
   */
  @Deprecated
  public static void preRender(RenderContext info, PreRenderable preRenderer) {
    info.preRender(preRenderer);
  }

  /**
   * "Pre"-renders an array of {@code PreRenderable}s.
   *
   * @see #preRender(RenderContext, PreRenderable)
   * @param info The current info
   * @param preRenderers The {@code PreRenderable}s to "Pre"-render
   */
  @Deprecated
  public static void preRender(RenderContext info, PreRenderable... preRenderers) {
    info.preRender(preRenderers);
  }

  /**
   * Given a throwable, throw a runtime exception.
   *
   * <p>If the exception is an {@code InvocationTargetException} it will be unwrapped first. If the
   * exception is a {@code RuntimeException}, it is simply thrown, otherwise the Throwable is
   * wrapped in a {@code SectionsRuntimeException}.
   *
   * @param e The throwable that occurred
   */
  public static void throwRuntime(Throwable e) {
    throw runtime(e);
  }

  public static RuntimeException runtime(Throwable e) {
    if (e instanceof InvocationTargetException) {
      e = ((InvocationTargetException) e).getTargetException();
    }

    if (e instanceof RuntimeException) {
      return (RuntimeException) e;
    }
    // else
    return new SectionsRuntimeException(e);
  }

  /**
   * Register a particular {@code Section} as having been rendered.
   *
   * @see AnnotatedBookmarkScanner
   * @param info The current info
   * @param sectionId The id to register
   */
  public static void registerRendered(SectionInfo info, String sectionId) {
    Set<String> renderedIds = info.getAttribute(RENDEREDIDS_KEY);
    if (renderedIds == null) {
      renderedIds = new HashSet<String>();
      info.setAttribute(RENDEREDIDS_KEY, renderedIds);
    }
    renderedIds.add(sectionId);
  }

  /**
   * Check if a particular {@code Section} has been rendered.
   *
   * @param info The current info
   * @param sectionId The id to check
   * @return Whether or not it has been rendered
   */
  public static boolean hasBeenRendered(SectionInfo info, String sectionId) {
    Set<String> renderedIds = info.getAttribute(RENDEREDIDS_KEY);
    if (renderedIds != null) {
      return renderedIds.contains(sectionId);
    }
    return false;
  }

  /**
   * Send a {@code RenderEvent} to a particular {@code Section}.
   *
   * @param info The current info
   * @param sectionId The id of the {@code Section}
   * @param listener The {@code RenderResultListener} to listen for results
   */
  public static void renderSection(
      RenderContext info, SectionId sectionId, RenderResultListener listener) {
    RenderEvent renderEvent = new RenderEvent(info, sectionId, listener);
    info.processEvent(renderEvent);
  }

  /**
   * Send a {@code RenderEvent} to a particular {@code Section} and return result as a {@code
   * SectionRenderable}.
   *
   * <p>It uses the {@link ResultListCollector} in order to ensure the result is a {@code
   * SectionRenderable}.
   *
   * @param info The current info
   * @param sectionId The id of the {@code Section}
   * @return The result as a {@code SectionRenderable}
   */
  @Deprecated
  public static SectionRenderable renderSection(RenderContext info, String sectionId) {
    ResultListCollector results = new ResultListCollector(true);
    RenderEvent renderEvent = new RenderEvent(info, sectionId, results);
    info.processEvent(renderEvent);
    return results.getFirstResult();
  }

  @Nullable
  public static SectionRenderable renderSection(RenderContext info, SectionId sectionId) {
    ResultListCollector results = new ResultListCollector(true);
    RenderEvent renderEvent = new RenderEvent(info, sectionId, results);
    info.processEvent(renderEvent);
    return results.getFirstResult();
  }

  /**
   * Send a {@code RenderEvent} to a particular {@code Section} and return the {@link SectionResult}
   * that came back.
   *
   * <p>This method uses the {@link SingleResultCollector} as the {@code RenderResultListener}.
   *
   * @param info The current info
   * @param sectionId The id of the {@code Section}
   * @return The {@code SectionResult} returned
   */
  @Nullable
  public static SectionResult renderSectionResult(RenderContext info, SectionId sectionId) {
    SingleResultCollector collectOne = new SingleResultCollector();
    RenderEvent renderEvent = new RenderEvent(info, sectionId, collectOne);
    info.processEvent(renderEvent);
    return collectOne.getResult();
  }

  /**
   * Send {@code RenderEvent}s to all children of a given {@code Section}.
   *
   * @param <T> The type of listener
   * @param info The current info
   * @param sectionId The parent {@code Section}'s id
   * @param listener The {@code RenderResultListener}
   * @return The listener
   */
  public static <T extends RenderResultListener> T renderChildren(
      RenderContext info, SectionId sectionId, T listener) {
    return renderSectionIds(info, info.getChildIds(sectionId), listener);
  }

  public static List<SectionRenderable> renderSectionIds(
      RenderContext info, Collection<SectionId> sectionIds) {
    return renderSectionIds(info, sectionIds, new ResultListCollector()).getResultList();
  }

  public static SectionRenderable renderSectionsCombined(
      RenderContext info, Collection<? extends SectionId> sectionIds) {
    return renderSectionIds(info, sectionIds, new ResultListCollector(true)).getFirstResult();
  }

  /**
   * Get the {@link HeaderHelper} associated with the current page render.
   *
   * @param info The current info
   * @return The {@code HeaderHelper}
   */
  @Deprecated
  public static HeaderHelper getHelper(RenderContext info) {
    return info.getHelper();
  }

  /**
   * Get a {@code SectionInfo} unique id.
   *
   * <p>Returns an id which is guaranteed to be unique for this request.
   *
   * <p>E.g. More than one request to the same page isn't guaranteed to return the same id. If you
   * need this sort of stableness, you should see {@link #getTreeUniqueId(SectionTree)}.
   *
   * @param info The current info
   * @return The id
   */
  public static String getPageUniqueId(SectionInfo info) {
    Integer upto = info.getAttribute(UNIQUEIDSS_KEY);
    if (upto == null) {
      upto = -1;
    }
    upto++;
    info.setAttribute(UNIQUEIDSS_KEY, upto);
    return upto.toString();
  }

  public static String htmlSafeId(String id) {
    return id.replace('.', '_');
  }

  /**
   * Clear the model for a given {@code Section} (and any child {@code Section}s.
   *
   * <p>This essentially tells the {@code SectionInfo} to remove the given {@code Section}s model
   * from it's model cache.
   *
   * @param info The current info
   * @param sectionId The id for the {@code Section}
   */
  public static void clearModel(SectionInfo info, SectionId sectionId) {
    List<SectionId> childIds = info.getAllChildIds(sectionId);
    for (SectionId childId : childIds) {
      SectionUtils.clearModel(info, childId);
    }
    info.clearModel(sectionId);
  }

  /**
   * Retrieve the referring {@code SectionInfo} if this {@code SectionInfo} was created as a result
   * of a forward.
   *
   * @see SectionInfo#createForward(String)
   * @param info The info
   * @return The info that created this one
   */
  @Nullable
  public static SectionInfo getOriginalInfo(SectionInfo info) {
    if (!info.isReal()) {
      return null;
    }
    if (info.getAttribute(SectionInfo.KEY_ORIGINALINFO) == null) {
      return info;
    }
    // else
    SectionInfo i2 = (SectionInfo) info.getAttribute(SectionInfo.KEY_ORIGINALINFO);
    if (i2 == null) {
      throw new Error("No referring info attribute in info");
    }
    return getOriginalInfo(i2);
  }

  /**
   * Get a {@code SectionTree} unique id.
   *
   * <p>Returns an id which is unique to this {@code SectionTree}.
   *
   * @param tree The {@code SectionTree}
   * @return The id
   */
  public static String getTreeUniqueId(SectionTree tree) {

    Integer upto = tree.getAttribute(UNIQUEIDSS_KEY);
    if (upto == null) {
      upto = -1;
    }
    upto++;
    tree.setAttribute(UNIQUEIDSS_KEY, upto);
    return htmlSafeId(tree.getRootId()) + upto.toString();
  }

  /**
   * Checks to see if any of the child {@code Section}s are viewable.
   *
   * @param info The current info
   * @param sectionId The id for the {@code Section}
   * @return true if any are visible
   */
  public static boolean canViewChildren(SectionInfo info, SectionId sectionId) {
    return canViewChildrenCount(info, sectionId) > 0;
  }

  /**
   * Checks to see if how many of the child {@code Section}s are viewable.
   *
   * @param info The current info
   * @param sectionId The id for the {@code Section}
   * @return true if any are visible
   */
  public static int canViewChildrenCount(SectionInfo info, SectionId sectionId) {
    List<SectionId> childIds = info.getChildIds(sectionId);
    int viewable = 0;
    for (SectionId childId : childIds) {
      SectionId section = info.getSectionForId(childId);
      if (section instanceof ViewableChildInterface
          && ((ViewableChildInterface) section).canView(info)) {
        viewable++;
      }
    }
    return viewable;
  }

  public static int countViewable(SectionInfo info, List<SectionId> sectionIds) {
    int i = 0;
    for (SectionId sectionId : sectionIds) {
      SectionId sectionForId = info.getSectionForId(sectionId);
      if (sectionForId instanceof ViewableChildInterface) {
        if (((ViewableChildInterface) sectionForId).canView(info)) {
          i++;
        }
      } else if (canViewChildren(info, sectionId)) {
        i++;
      }
    }
    return i;
  }

  public static SectionRenderable convertToRenderer(@Nullable Object obj) {
    if (obj == null) {
      return new SimpleSectionResult("");
    }
    if (obj instanceof SectionRenderable) {
      return ((SectionRenderable) obj);
    }
    if (obj instanceof WrappedLabel) {
      return new WrappedLabelRenderer((WrappedLabel) obj);
    }
    if (obj instanceof Label) {
      return new LabelRenderer((Label) obj);
    }
    if (obj instanceof SectionId) {
      return new SectionIdRenderer((SectionId) obj);
    }
    return new LabelRenderer(new TextLabel(obj.toString()));
  }

  public static SectionRenderable[] convertToRenderers(Collection<?> objs) {
    SectionRenderable[] renderers = new SectionRenderable[objs.size()];
    int i = 0;
    for (Object object : objs) {
      renderers[i++] = convertToRenderer(object);
    }
    return renderers;
  }

  public static SectionRenderable[] convertToRenderers(Object... obj) {
    SectionRenderable[] renderers = new SectionRenderable[obj.length];
    int i = 0;
    for (Object object : obj) {
      renderers[i++] = convertToRenderer(object);
    }
    return renderers;
  }

  public static SectionRenderable convertToRenderer(Object... obj) {
    return CombinedRenderer.combineMultipleResults(convertToRenderers(obj));
  }

  public static String getFilenameFromFilepath(String filepath) {
    return PathUtils.getFilenameFromFilepath(filepath);
  }

  public static String relativePath(String base, String path) {
    return PathUtils.relativeUrlPath(base, path);
  }

  public static <T extends RenderResultListener> T renderSectionIds(
      RenderContext context, Collection<? extends SectionId> sectionIds, T listener) {
    for (SectionId sectionId : sectionIds) {
      RenderEvent renderEvent = new RenderEvent(context, sectionId, listener);
      context.processEvent(renderEvent);
    }
    return listener;
  }

  private SectionUtils() {
    throw new Error();
  }
}
