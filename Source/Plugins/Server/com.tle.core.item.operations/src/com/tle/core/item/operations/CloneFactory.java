package com.tle.core.item.operations;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.operations.SaveOperation;

@BindFactory
public interface CloneFactory
{
	CloneOperation clone(@Assisted("copyAttachments") boolean copyAttachments, @Assisted("submit") boolean submit);

	CloneOperation clone(String collection, @Assisted("copyAttachments") boolean copyAttachments,
		@Assisted("submit") boolean submit);

	MoveDirectOperation moveDirect(String collection);

	SaveOperation save();
}
