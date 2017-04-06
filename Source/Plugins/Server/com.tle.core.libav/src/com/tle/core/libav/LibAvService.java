package com.tle.core.libav;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.beans.filesystem.FileHandle;

public interface LibAvService
{
	void screenshotVideo(File srcFile, File dstFile) throws IOException;

	void generatePreviewVideo(FileHandle handle, String filename) throws IOException;

	ObjectNode getVideoInfo(File srcFile) throws IOException;

	boolean isLibavInstalled();
}
