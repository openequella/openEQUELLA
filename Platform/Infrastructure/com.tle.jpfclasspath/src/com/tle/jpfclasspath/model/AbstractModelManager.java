/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM
 * Corporation - initial API and implementation
 * *****************************************************************************
 */
package com.tle.jpfclasspath.model;

import org.eclipse.core.runtime.ListenerList;

public abstract class AbstractModelManager {

  private ListenerList fListeners = new ListenerList();

  public synchronized void removeModelProviderListener(IModelProviderListener listener) {
    // TODO see bug 214613... investigate why FeatureModelManager is being
    // bad
    if (listener != null) {
      fListeners.remove(listener);
    }
  }

  public synchronized void addModelProviderListener(IModelProviderListener listener) {
    fListeners.add(listener);
  }

  public void fireModelProviderEvent(IModelProviderEvent event) {
    Object[] listeners = fListeners.getListeners();
    for (int i = 0; i < listeners.length; i++) {
      ((IModelProviderListener) listeners[i]).modelsChanged(event);
    }
  }

  public void shutdown() {
    removeListeners();
  }

  protected void removeListeners() {
    fListeners.clear();
  }
}
