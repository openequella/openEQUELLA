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

package com.tle.core.reporting.birttypes;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import com.dytech.edge.wizard.beans.control.ListBox;
import com.dytech.edge.wizard.beans.control.ShuffleBox;
import com.dytech.edge.wizard.beans.control.WizardControl;

public class ListBoxBirtType extends AbstractListBirtType {
  public ListBoxBirtType(IScalarParameterDefn def, int paramNum, IParameterGroupDefn group) {
    super(def, paramNum, group);
  }

  @Override
  protected WizardControl createControl(IGetParameterDefinitionTask paramTask) {
    if (scalarDef.getScalarParameterType().equals("multi-value")) // $NON-NLS-1$
    {
      return new ShuffleBox();
    }
    return new ListBox();
  }
}
