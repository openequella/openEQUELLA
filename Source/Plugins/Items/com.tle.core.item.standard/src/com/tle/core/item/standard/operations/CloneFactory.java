package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;

@BindFactory
public interface CloneFactory
{
	CloneOperation clone(@Assisted("copyAttachments") boolean copyAttachments, @Assisted("submit") boolean submit);

	CloneOperation clone(String collection, @Assisted("copyAttachments") boolean copyAttachments,
		@Assisted("submit") boolean submit);

	MoveDirectOperation moveDirect(@Assisted String collection, @Assisted boolean copyFiles);

	SaveOperation save();
}
