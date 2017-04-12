package com.tle.common.inplaceeditor;

public interface InPlaceEditorServerBackend
{
	String getDownloadUrl(String itemUuid, int itemVersion, String stagingId, String filename);

	void write(String stagingId, String filename, boolean append, byte[] upload);
}
