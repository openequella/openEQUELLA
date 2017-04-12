package com.tle.common.recipientselector;

import java.util.List;

/**
 * @author Nicholas Read
 */
public interface UserGroupRoleFinder
{
	RecipientFilter getSelectedFilter();

	List<Object> getSelectedResults();

	void setSingleSelectionOnly(boolean b);

	void clearAll();

	void addFinderListener(FinderListener listener);

	void setEnabled(boolean b);
}
