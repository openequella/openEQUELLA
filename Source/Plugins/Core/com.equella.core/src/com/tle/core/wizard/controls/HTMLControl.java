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
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.NameValue;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.LERepository;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public interface HTMLControl
{
	boolean isVisible();

	void setVisible(boolean visible);

	boolean isViewable();

	boolean isEnabled();

	void clearInvalid();

	void validate();

	void saveToDocument(PropBagEx itemxml) throws Exception;

	void loadFromDocument(PropBagEx itemxml);

	void resetToDefaults();

	boolean isInvalid();

	boolean isEmpty();

	boolean isMandatory();

	String getFormName();

	void setInvalid(boolean yes, Label msg);

	@Nullable
	BaseQuery getPowerSearchQuery();

	int getSize1();

	int getSize2();

	void setSize1(int size1);

	void setSize2(int size2);

	NameValue getNameValue();

	void afterSaveValidate() throws Exception;

	boolean isHidden();

	void setHidden(boolean hidden);

	void clearTargets(PropBagEx itemxml);

	void evaluate();

	boolean isIncluded();

	void setDontShowEmpty(boolean dontshow);

	void setValues(@Nullable String... values);

	String getTitle();

	WizardControl getControlBean();

	@Nullable
	Label getMessage();

	String getDescription();

	@Nullable
	LERepository getRepository();

	WizardPage getWizardPage();

	List<TargetNode> getTargets();

	TargetNode getFirstTarget();

	boolean isExpertSearch();

	int getControlNumber();

	int getNestingLevel();

	void setTopLevel(HTMLControl topLevel);

	/**
	 * Used for advanced searches. If true: This control maps to a metadata
	 * target that another control already maps to. There is some tricky code to
	 * ensure the original metadata isn't clobbered.
	 * AbstractHTMLControl.getDefaultPowerSearchQuery uses the appropriate
	 * metadata and the final advanced search will AND these 2 (or more) values.
	 * If false: It's just a normal control.
	 * 
	 * @return Whether this control is 'uniquified'
	 */
	boolean isUniquified();

	void setUniquified(boolean uniquified);

	/**
	 * @return Gets the parent. Provided it's a repeater ;)
	 */
	@Nullable
	HTMLControl getParent();

	void setParent(HTMLControl parent);
}