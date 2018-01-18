package com.tle.web.filemanager.applet.backend;

import java.util.EventListener;

public interface BackendListener extends EventListener
{
	void fileAdded(BackendEvent event);

	void fileDeleted(BackendEvent event);

	void fileMoved(BackendEvent event);

	void fileCopied(BackendEvent event);

	void fileMarkedAsResource(BackendEvent event);

	void localFilesEdited(BackendEvent event);

	void extractArchive(BackendEvent event);
}
