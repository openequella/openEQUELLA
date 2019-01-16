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

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name: HTMLLayout2.java

package com.dytech.common.log4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

@SuppressWarnings("nls")
public class HTMLLayout2 extends Layout {
  protected final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
  protected static final int BUF_SIZE = 256;
  protected static final int MAX_CAPACITY = 1024;
  protected static final String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
  /** @deprecated Field LOCATION_INFO_OPTION is deprecated */
  public static final String LOCATION_INFO_OPTION = "LocationInfo";

  public static final String TITLE_OPTION = "Title";

  private StringBuilder sbuf;
  private boolean locationInfo;
  private String title;

  public HTMLLayout2() {
    sbuf = new StringBuilder(256);
    locationInfo = false;
    title = "Log4J Log Messages";
  }

  public void setLocationInfo(boolean flag) {
    locationInfo = flag;
  }

  public boolean getLocationInfo() {
    return locationInfo;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String getContentType() {
    return "text/html";
  }

  @Override
  public void activateOptions() {
    // no-op
  }

  @Override
  public String format(LoggingEvent event) {
    if (sbuf.capacity() > 1024) {
      sbuf = new StringBuilder(256);
    } else {
      sbuf.setLength(0);
    }
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<tr>");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<td>");
    sbuf.append(dateFormat.format(new Date(event.timeStamp)));
    sbuf.append("</td>");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<td>");
    if (event.getLevel().equals(Level.DEBUG)) {
      sbuf.append("<font color=\"#339933\">");
      sbuf.append(event.getLevel());
      sbuf.append("</font>");
    } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
      sbuf.append("<font color=\"#993300\"><strong>");
      sbuf.append(event.getLevel());
      sbuf.append("</strong></font>");
    } else {
      sbuf.append(event.getLevel());
    }
    sbuf.append("</td>");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<td>");
    sbuf.append(Transform.escapeTags(event.getLoggerName()));
    sbuf.append("</td>");
    sbuf.append(Layout.LINE_SEP);
    if (locationInfo) {
      LocationInfo locInfo = event.getLocationInformation();
      sbuf.append("<td>");
      sbuf.append(Transform.escapeTags(locInfo.getFileName()));
      sbuf.append(':');
      sbuf.append(locInfo.getLineNumber());
      sbuf.append("</td>");
      sbuf.append(Layout.LINE_SEP);
    }
    sbuf.append("<td>");
    sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
    sbuf.append("</td>");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("</tr>");
    sbuf.append(Layout.LINE_SEP);
    if (event.getNDC() != null) {
      sbuf.append(
          "<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
      sbuf.append("NDC: ");
      sbuf.append(Transform.escapeTags(event.getNDC()));
      sbuf.append("</td></tr>");
      sbuf.append(Layout.LINE_SEP);
    }
    String s[] = event.getThrowableStrRep();
    if (s != null) {
      sbuf.append(
          "<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
      appendThrowableAsHTML(s, sbuf);
      sbuf.append("</td></tr>");
      sbuf.append(Layout.LINE_SEP);
    }
    return sbuf.toString();
  }

  void appendThrowableAsHTML(String s[], StringBuilder sbuf) {
    if (s != null) {
      int len = s.length;
      if (len == 0) {
        return;
      }

      sbuf.append(Transform.escapeTags(s[0]));
      sbuf.append(Layout.LINE_SEP);
      for (int i = 1; i < len; i++) {
        sbuf.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;");
        sbuf.append(Transform.escapeTags(s[i]));
        sbuf.append(Layout.LINE_SEP);
      }
    }
  }

  @Override
  public String getHeader() {
    StringBuilder header = new StringBuilder();
    header.append(
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
            + Layout.LINE_SEP);
    header.append("<html>");
    header.append(Layout.LINE_SEP);
    header.append("<head>");
    header.append(Layout.LINE_SEP);
    header.append("<title>");
    header.append(title);
    header.append("</title>");
    header.append(Layout.LINE_SEP);
    header.append("<style type=\"text/css\">");
    header.append(Layout.LINE_SEP);
    header.append("<!--");
    header.append(Layout.LINE_SEP);
    header.append("body, table {font-family: arial,sans-serif; font-size: x-small;}");
    header.append(Layout.LINE_SEP);
    header.append("th {background: #336699; color: #FFFFFF; text-align: left;}");
    header.append(Layout.LINE_SEP);
    header.append("-->");
    header.append(Layout.LINE_SEP);
    header.append("</style>");
    header.append(Layout.LINE_SEP);
    header.append("</head>");
    header.append(Layout.LINE_SEP);
    header.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">");
    header.append(Layout.LINE_SEP);
    header.append("<hr size=\"1\" noshade>");
    header.append(Layout.LINE_SEP);
    header.append("Log session start time ");
    header.append(new Date().toString());
    header.append("<br>");
    header.append(Layout.LINE_SEP);
    header.append("<br>");
    header.append(Layout.LINE_SEP);
    header.append(
        "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
    header.append(Layout.LINE_SEP);
    header.append("<tr>");
    header.append(Layout.LINE_SEP);
    header.append("<th>Time</th>");
    header.append(Layout.LINE_SEP);
    header.append("<th>Level</th>");
    header.append(Layout.LINE_SEP);
    header.append("<th>Category</th>");
    header.append(Layout.LINE_SEP);
    if (locationInfo) {
      header.append("<th>File:Line</th>");
      header.append(Layout.LINE_SEP);
    }
    header.append("<th>Message</th>");
    header.append(Layout.LINE_SEP);
    header.append("</tr>");
    header.append(Layout.LINE_SEP);
    return header.toString();
  }

  @Override
  public String getFooter() {
    StringBuilder footer = new StringBuilder();
    footer.append("</table>");
    footer.append(Layout.LINE_SEP);
    footer.append("<br>");
    footer.append(Layout.LINE_SEP);
    footer.append("</body></html>");
    return footer.toString();
  }

  @Override
  public boolean ignoresThrowable() {
    return false;
  }
}
