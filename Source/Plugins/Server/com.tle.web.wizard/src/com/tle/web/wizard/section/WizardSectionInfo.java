package com.tle.web.wizard.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.wizard.WizardState;

@NonNullByDefault
public interface WizardSectionInfo extends ItemSectionInfo
{
	@Nullable
	WizardState getWizardState();

	boolean isAvailableForEditing();

	boolean isLockedForEditing();

	boolean isNewItem();

	void cancelEdit();
}
