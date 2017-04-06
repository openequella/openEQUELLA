package com.tle.mycontent.workflow.operations;

import java.io.InputStream;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.operations.MyContentStatusOperation;
import com.tle.mycontent.service.MyContentFields;

@BindFactory
public interface OperationFactory
{
	EditMyContentOperation create(MyContentFields fields, String filename, InputStream inputStream,
		@Assisted("staginguuid") String stagingUuid,
		@Assisted("remove") boolean removeExistingAttachments, @Assisted("use") boolean useExistingAttachment);

	MyContentStatusOperation status();
}