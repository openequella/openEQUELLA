package com.tle.web.upload;

import com.tle.core.guice.Bind;
import org.apache.commons.fileupload.FileItemStream;

import javax.inject.Singleton;
import java.util.Map;

/**
 * @author Aaron
 */
@Bind(UploadService.class)
@Singleton
public class UploadServiceImpl implements UploadService
{
	@Override
	public Upload addUpload(String sessionId, FileItemStream stream) {
		return null;
	}

	@Override
	public void removeUpload(String sessionId, String uuid) {

	}

	@Override
	public void killUpload(String sessionId, String uuid) {

	}

	@Override
	public Map<String, Upload> getUserUploads(String sessionId) {
		return null;
	}
}
