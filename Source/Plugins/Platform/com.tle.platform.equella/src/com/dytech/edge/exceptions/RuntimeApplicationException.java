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

package com.dytech.edge.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Provides a generic exception for the TLE to build upon.
 *
 * @author Nicholas Read
 */
public class RuntimeApplicationException extends RuntimeException implements QuietlyLoggable {
  private static final long serialVersionUID = 1L;

  private boolean logged = false;
  private boolean showStackTrace = true;
  private boolean silent = false;
  private boolean warnOnly = false;

  public RuntimeApplicationException() {
    super("Error occurred in application."); // $NON-NLS-1$
  }

  public RuntimeApplicationException(String message) {
    super(message);
  }

  public RuntimeApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuntimeApplicationException(Throwable cause) {
    super(cause);
  }

  @Override
  public String getMessage() {
    // Get this exception's message.
    StringBuilder message = new StringBuilder();

    String superMsg = super.getMessage();
    if (superMsg != null) {
      message.append(superMsg);
    }

    Throwable parent = this;
    Throwable child;

    // Look for nested exceptions.
    while ((child = parent.getCause()) != null) {
      // Get the child's message.
      String msg2 = child.getMessage();

      // If we found a message for the child exception,
      // we append it.
      if (msg2 != null) {
        if (message.length() > 0) {
          message.append(": "); // $NON-NLS-1$
        }
        message.append(msg2);
      }

      // Any nested ApplicationException will append its own
      // children, so we need to break out of here.
      if (child instanceof RuntimeApplicationException) {
        break;
      }
      parent = child;
    }

    // Return the completed message.
    return message.toString();
  }

  @Override
  public void printStackTrace(PrintStream s) {
    // Print the stack trace for this exception.
    super.printStackTrace(s);

    Throwable parent = this;
    Throwable child;

    // Print the stack trace for each nested exception.
    while ((child = parent.getCause()) != null) {
      s.print("Caused by: "); // $NON-NLS-1$
      child.printStackTrace(s);

      if (child instanceof RuntimeApplicationException) {
        break;
      }
      parent = child;
    }
  }

  @Override
  public void printStackTrace(PrintWriter w) {
    // Print the stack trace for this exception.
    super.printStackTrace(w);

    Throwable parent = this;
    Throwable child;

    // Print the stack trace for each nested exception.
    while ((child = parent.getCause()) != null) {
      w.print("Caused by: "); // $NON-NLS-1$
      child.printStackTrace(w);

      if (child instanceof RuntimeApplicationException) {
        break;
      }
      parent = child;
    }
  }

  public boolean isLogged() {
    return logged;
  }

  public void setLogged(boolean logged) {
    this.logged = logged;
  }

  @Override
  public boolean isShowStackTrace() {
    return showStackTrace;
  }

  public void setShowStackTrace(boolean showStackTrace) {
    this.showStackTrace = showStackTrace;
  }

  @Override
  public boolean isSilent() {
    return silent;
  }

  public void setSilent(boolean silent) {
    this.silent = silent;
  }

  @Override
  public boolean isWarnOnly() {
    return warnOnly;
  }

  public void setWarnOnly(boolean warnOnly) {
    this.warnOnly = warnOnly;
  }
}
