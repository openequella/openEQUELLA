package com.tle.web.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.fileupload.FileItemStream;

/**
 * @author Aaron
 */
public interface UploadService
{
	Upload addUpload(String sessionId, FileItemStream stream);

	void removeUpload(String sessionId, String uuid);

	void killUpload(String sessionId, String uuid);

	/**
	 * @param sessionId
	 * @return Unmodifiable map
	 */
	Map<String, Upload> getUserUploads(String sessionId);

	abstract class Upload extends InputStream
	{
		public abstract void kill() throws IOException, StreamKilledException;

		public abstract String getUuid();

		public abstract boolean isClosed();

		public abstract String getControlId();

		public abstract Date getStartTime();

	}

	class StreamKilledException extends IOException
	{
		// Nothing
	}
}
