package com.tle.mypages.workflow.operation;

import java.io.InputStream;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.mycontent.service.MyContentFields;

@BindFactory
public interface OperationFactory
{
	EditMyPagesOperation create(MyContentFields fields, String filename, InputStream inputStream,
		@Assisted("remove") boolean removeExistingAttachments, @Assisted("use") boolean useExistingAttachment);
}
