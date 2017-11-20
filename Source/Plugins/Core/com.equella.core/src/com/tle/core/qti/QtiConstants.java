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

package com.tle.core.qti;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.core.services.FileSystemService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public final class QtiConstants
{
	private QtiConstants()
	{
		throw new Error("No");
	}

	public static final String QTI_FOLDER_NAME = "_QTI";
	public static final String QTI_FOLDER_PATH = PathUtils.filePath(FileSystemService.SECURE_FOLDER,
		QtiConstants.QTI_FOLDER_NAME);

	public static final String TEST_CUSTOM_ATTACHMENT_TYPE = "qtitest";
	public static final String QUESTION_CUSTOM_ATTACHMENT_TYPE = "qtiquestion";

	public static final String TEST_MIME_TYPE = "equella/qtitest";
	public static final String MIME_ICON_PATH = "icons/qti.png";
	// This MIME type does not exist yet
	public static final String QUESTION_MIME_TYPE = "equella/qtiquestion";

	public static final String KEY_TEST_UUID = "qti_testUuid";
	public static final String KEY_FILE_SIZE = "qti_fileSize";
	public static final String KEY_XML_PATH = "qti_xmlPath";
	public static final String KEY_TIME_LIMIT = "qti_timeLimit";
	public static final String KEY_QUESTION_COUNT = "qti_questionCount";
	public static final String KEY_TOOL_NAME = "qti_toolName";
	public static final String KEY_TOOL_VERSION = "qti_toolVersion";
	public static final String KEY_MAX_TIME = "qti_maxTime";
	public static final String KEY_SECTION_COUNT = "qti_sectionCount";
	public static final String KEY_NAVIGATION_MODE = "qti_navigationMode";
}
