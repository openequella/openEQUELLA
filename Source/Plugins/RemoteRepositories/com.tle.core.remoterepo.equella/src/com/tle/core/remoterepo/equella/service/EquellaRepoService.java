/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.remoterepo.equella.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.search.TLESettings;
import com.tle.common.beans.progress.PercentageProgressCallback;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.services.impl.Task;
import com.tle.common.usermanagement.user.UserState;

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

	Task createDownloadTask(UserState userState, String username, String uuid, int version, String url, String sharedId,
		String sharedValue, String key);

	interface AttachmentDownloadSession
	{
		boolean isFinished();
	}
}
