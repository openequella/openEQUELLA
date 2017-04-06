package com.tle.core.hierarchy.impl;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.core.user.UserState;

@BindFactory
public interface HierarchyTaskFactory
{
	ImportTask createImportTask(UserState userState, String xml, long topicInto, @Assisted("newids") boolean newids,
		@Assisted("useSecurity") boolean useSecurity);

	ExportTask createExportTask(UserState userState, long exportId, boolean withSecurity);
}
