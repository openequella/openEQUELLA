package com.tle.web.scripting;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.util.Logger;
import com.tle.core.guice.BindFactory;
import com.tle.web.scripting.objects.*;

@BindFactory
public interface ScriptObjectFactory
{
	FileScriptingObjectImpl createFile(@Assisted("handle") FileHandle handle);

	NavigationScriptWrapper createNavigation(@Assisted("item") Item item);

	AttachmentsScriptWrapper createAttachments(@Assisted("attachments") ModifiableAttachments attachments, @Assisted("staging") FileHandle staging);

	UserScriptWrapper createUser();

	LoggingScriptWrapper createLogger(@Assisted("logger") Logger logger);

	DrmScriptWrapper createDrm(@Assisted("item") Item item, @Assisted("drmSettings") DrmSettings drmSettings);

	ImagesScriptWrapper createImages(@Assisted("handle") FileHandle handle);
}
