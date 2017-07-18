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

package com.tle.core.wizard.controls;

import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.NameValue;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.LERepository;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public class HTMLCtrlWrapper implements HTMLControl
{
	protected HTMLControl control;

	public HTMLCtrlWrapper()
	{
		// for spring
	}

	public HTMLControl getWrappedControl()
	{
		return control;
	}

	public void setWrappedControl(HTMLControl control)
	{
		this.control = control;
	}

	public HTMLCtrlWrapper(HTMLControl control)
	{
		this.control = control;
	}

	@Override
	public boolean isVisible()
	{
		return control.isVisible();
	}

	@Override
	public void setVisible(boolean visible)
	{
		control.setVisible(visible);
	}

	@Override
	public boolean isViewable()
	{
		return control.isViewable();
	}

	@Override
	public boolean isEnabled()
	{
		return control.isEnabled();
	}

	@Override
	public void clearInvalid()
	{
		control.clearInvalid();
	}

	@Override
	public void validate()
	{
		control.validate();
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		control.saveToDocument(itemxml);
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		control.loadFromDocument(itemxml);
	}

	@Override
	public void resetToDefaults()
	{
		control.resetToDefaults();
	}

	@Override
	public boolean isInvalid()
	{
		return control.isInvalid();
	}

	@Override
	public boolean isEmpty()
	{
		return control.isEmpty();
	}

	@Override
	public boolean isMandatory()
	{
		return control.isMandatory();
	}

	@Override
	public String getFormName()
	{
		return control.getFormName();
	}

	@Override
	public void setInvalid(boolean yes, Label msg)
	{
		control.setInvalid(yes, msg);
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return control.getPowerSearchQuery();
	}

	@Override
	public int getSize1()
	{
		return control.getSize1();
	}

	@Override
	public int getSize2()
	{
		return control.getSize2();
	}

	@Override
	public void setSize1(int size1)
	{
		control.setSize1(size1);
	}

	@Override
	public void setSize2(int size2)
	{
		control.setSize2(size2);
	}

	@Override
	public NameValue getNameValue()
	{
		return control.getNameValue();
	}

	@Override
	public void afterSaveValidate() throws Exception
	{
		control.afterSaveValidate();
	}

	@Override
	public boolean isHidden()
	{
		return control.isHidden();
	}

	@Override
	public void setHidden(boolean hidden)
	{
		control.setHidden(hidden);
	}

	@Override
	public void clearTargets(PropBagEx itemxml)
	{
		control.clearTargets(itemxml);
	}

	@Override
	public void evaluate()
	{
		control.evaluate();
	}

	@Override
	public boolean isIncluded()
	{
		return control.isIncluded();
	}

	@Override
	public void setDontShowEmpty(boolean dontshow)
	{
		control.setDontShowEmpty(dontshow);
	}

	@Override
	public void setValues(String[] values)
	{
		control.setValues(values);
	}

	@Override
	public WizardControl getControlBean()
	{
		return control.getControlBean();
	}

	@Override
	public String getDescription()
	{
		return control.getDescription();
	}

	@Override
	public Label getMessage()
	{
		return control.getMessage();
	}

	@Override
	public LERepository getRepository()
	{
		return control.getRepository();
	}

	@Override
	public String getTitle()
	{
		return control.getTitle();
	}

	@Override
	public WizardPage getWizardPage()
	{
		return control.getWizardPage();
	}

	@Override
	public TargetNode getFirstTarget()
	{
		return control.getFirstTarget();
	}

	@Override
	public List<TargetNode> getTargets()
	{
		return control.getTargets();
	}

	@Override
	public boolean isExpertSearch()
	{
		return control.isExpertSearch();
	}

	@Override
	public boolean isUniquified()
	{
		return control.isUniquified();
	}

	@Override
	public void setUniquified(boolean uniquified)
	{
		control.setUniquified(uniquified);
	}

	@Override
	public int getControlNumber()
	{
		return control.getControlNumber();
	}

	@Override
	public int getNestingLevel()
	{
		return control.getNestingLevel();
	}

	@Override
	public void setTopLevel(HTMLControl topLevel)
	{
		control.setTopLevel(topLevel);
	}

	@Override
	public HTMLControl getParent()
	{
		return control.getParent();
	}

	@Override
	public void setParent(HTMLControl parent)
	{
		control.setParent(parent);
	}
}
