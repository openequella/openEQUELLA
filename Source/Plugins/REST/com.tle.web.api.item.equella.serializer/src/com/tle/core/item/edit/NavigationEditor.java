package com.tle.core.item.edit;

import java.util.List;

public interface NavigationEditor
{
	NavigationNodeEditor getNavigationNodeEditor(String uuid);

	void editRootNodes(List<String> nodeUuids);

	void editManualNavigation(boolean manualNavigation);

	void editShowSplitOption(boolean showSplitOption);
}
