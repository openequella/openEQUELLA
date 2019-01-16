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

package com.tle.core.equella.runner;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LoggingEvent;

/** @author Nicholas Read */
public class HTMLLayout3 extends Layout {
  protected final DateFormat dateFormat = new AbsoluteTimeDateFormat();
  protected static final String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
  public static final String TITLE_OPTION = "Title";

  private String title;

  public HTMLLayout3() {
    title = "Log4J Log Messages";
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
    // Nothing to do here
  }

  @Override
  public String format(LoggingEvent event) {
    StringBuilder sbuf = new StringBuilder();

    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<tr><td>");
    sbuf.append(dateFormat.format(new Date(event.timeStamp)));
    sbuf.append("</td><td>");
    appendContextText(event, sbuf);
    sbuf.append("</td><td>");
    appendLevelText(event, sbuf);
    sbuf.append("</td><td>");
    appendLoggerName(event, sbuf);
    sbuf.append("</td>");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<td>");
    sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
    sbuf.append("</td></tr>");

    String s[] = event.getThrowableStrRep();
    if (s != null) {
      sbuf.append(Layout.LINE_SEP);
      sbuf.append(
          "<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"5\">");
      appendThrowableAsHTML(s, sbuf);
      sbuf.append("</td></tr>");
    }

    return sbuf.toString();
  }

  protected void appendLoggerName(LoggingEvent event, StringBuilder sbuf) {
    String n = event.getLoggerName();
    int end = n.lastIndexOf('.', n.length() - 2);
    if (end >= 0) {
      n = n.substring(end + 1);
    }
    sbuf.append(Transform.escapeTags(n));
  }

  protected void appendLevelText(LoggingEvent event, StringBuilder sbuf) {
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
  }

  protected void appendContextText(LoggingEvent event, StringBuilder sbuf) {
    Object context = event.getMDC("SessionID");
    if (context != null) {
      sbuf.append(Transform.escapeTags(context.toString()));
    }
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
    StringBuilder sbuf = new StringBuilder();
    sbuf.append(
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("<html><head><title>");
    sbuf.append(title);
    sbuf.append("</title><style type=\"text/css\"><!--");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("body, table {font-family: arial,sans-serif; font-size: x-small;}");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append(
        "--></style></head><body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\"><hr size=\"1\">");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append(
        "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
    sbuf.append(Layout.LINE_SEP);
    sbuf.append(
        "<tr><th>Time</th><th>Context</th><th>Level</th><th>Category</th><th>Message</th></tr>");
    sbuf.append(Layout.LINE_SEP);
    return sbuf.toString();
  }

  @Override
  public String getFooter() {
    return "</table><br></body></html>";
  }

  @Override
  public boolean ignoresThrowable() {
    return false;
  }
}
