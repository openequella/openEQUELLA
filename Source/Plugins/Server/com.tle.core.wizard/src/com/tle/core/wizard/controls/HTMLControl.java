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

	void setInvalid(boolean yes, LanguageBundle msg);

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
	LanguageBundle getMessage();

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