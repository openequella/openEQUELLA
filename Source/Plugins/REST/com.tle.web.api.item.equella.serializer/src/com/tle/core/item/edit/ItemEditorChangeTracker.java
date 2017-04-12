package com.tle.core.item.edit;

public interface ItemEditorChangeTracker
{
	boolean isForceFileCheck();

	boolean hasBeenEdited(Object oldValue, Object newValue);

	void editDetected();

	void attachmentEditDetected();

	// this will change when we do more than all/nothing
	void addIndexingEdit(String editType);

	void editWithPrivilege(String priv);
}
