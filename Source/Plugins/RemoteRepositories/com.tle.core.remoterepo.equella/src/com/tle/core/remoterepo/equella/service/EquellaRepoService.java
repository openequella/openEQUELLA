package com.tle.core.remoterepo.equella.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.search.TLESettings;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.progress.PercentageProgressCallback;
import com.tle.core.services.impl.Task;
import com.tle.core.user.UserState;

/**
 * @author agibb
 * @author aholland
 */
public interface EquellaRepoService
{
	/**
	 * @return A session id
	 */
	String downloadAttachments(TLESettings settings, String uuid, int version, PercentageProgressCallback callback);

	AttachmentDownloadSession downloadProgress(String sessionId);

	/**
	 * Only call this once for any completed session (it MUST be completed). The
	 * session is removed after calling this method.
	 * 
	 * @return Returns null if the session is not finished
	 */
	StagingFile getDownloadedFiles(AttachmentDownloadSession session);

	/**
	 * @param settings
	 * @param uuid
	 * @param version
	 * @param stripFileBasedAttachments Removes all attachments/attachment nodes
	 *            of type "local","zip","imsres","html" (basically anything that
	 *            relies on a physical local file)
	 * @return
	 */
	PropBagEx getItemXml(TLESettings settings, String uuid, int version, boolean stripFileBasedAttachments);

	Task createDownloadTask(UserState userState, String username, String uuid, int version, String url,
		String sharedId, String sharedValue, String key);

	interface AttachmentDownloadSession
	{
		boolean isFinished();
	}
}
