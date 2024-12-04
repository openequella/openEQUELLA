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

package com.dytech.installer.commands;

import com.dytech.installer.InstallerException;
import com.dytech.installer.Progress;
import com.dytech.installer.TaskListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class Command {
  private Collection taskListeners;
  private boolean mandatory = true;
  private Progress progress;

  public Command() {
    taskListeners = new ArrayList();
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean b) {
    mandatory = b;
  }

  public void addTaskListener(TaskListener l) {
    taskListeners.add(l);
  }

  public void removeTaskListener(TaskListener l) {
    taskListeners.remove(l);
  }

  public abstract void execute() throws InstallerException;

  @Override
  public abstract String toString();

  protected void propogateTaskStarted(int subtasks) {
    Iterator i = taskListeners.iterator();
    while (i.hasNext()) {
      ((TaskListener) i.next()).taskStarted(subtasks);
    }
  }

  protected void propogateTaskCompleted() {
    Iterator i = taskListeners.iterator();
    while (i.hasNext()) {
      ((TaskListener) i.next()).taskCompleted();
    }
  }

  protected void propogateSubtaskCompleted() {
    Iterator i = taskListeners.iterator();
    while (i.hasNext()) {
      ((TaskListener) i.next()).subtaskCompleted();
    }
  }

  /**
   * @return Returns the progress.
   */
  public Progress getProgress() {
    return progress;
  }

  /**
   * @param progress The progress to set.
   */
  public void setProgress(Progress progress) {
    this.progress = progress;
  }
}
