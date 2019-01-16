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

import java.util.Map;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.CheckBoxGroup;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.tle.common.i18n.LangUtils;

public class CheckBoxBirtType extends AbstractBirtType {

  public CheckBoxBirtType(IScalarParameterDefn def, int paramNum) {
    super(def, paramNum);
  }

  @Override
  protected WizardControl createControl(IGetParameterDefinitionTask paramTask) {
    if (!(scalarDef.getDataType() == IParameterDefn.TYPE_BOOLEAN)) {
      throw new BirtTypeException(
          "The display type check box is invalid for the given data type."); //$NON-NLS-1$
    }

    Boolean selected = (Boolean) paramTask.getDefaultValue(scalarDef);
    WizardControl control = new CheckBoxGroup();
    WizardControlItem check =
        new WizardControlItem(
            LangUtils.createTextTempLangugageBundle(""), "true"); // $NON-NLS-1$ //$NON-NLS-2$
    check.setDefaultOption(selected);
    control.getItems().add(check);
    return control;
  }

  @Override
  public void convertToParams(PropBagEx docXml, Map<String, String[]> params) {
    params.put(
        scalarDef.getName(),
        new String[] {Boolean.toString(docXml.getNode(targetNode).equals("true"))}); // $NON-NLS-1$
  }

  @Override
  public void convertToXml(PropBagEx docXml, Map<String, String[]> params) {
    String val = getFirstValue(params);
    if (val != null) {
      docXml.setNode(targetNode, Boolean.parseBoolean(val));
    }
  }
}
