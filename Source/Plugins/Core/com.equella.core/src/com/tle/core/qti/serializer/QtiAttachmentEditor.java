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

package com.tle.core.qti.serializer;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;
import com.tle.core.qti.QtiConstants;

@Bind
public class QtiAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE;
	}

	public void editXmlFullPath(String xmlFullPath)
	{
		editCustomData(QtiConstants.KEY_XML_PATH, xmlFullPath);
	}

	public void editTestUuid(String testUuid)
	{
		editCustomData(QtiConstants.KEY_TEST_UUID, testUuid);
	}

	public void editToolName(String toolName)
	{
		editCustomData(QtiConstants.KEY_TOOL_NAME, toolName);
	}

	public void editToolVersion(String toolVersion)
	{
		editCustomData(QtiConstants.KEY_TOOL_VERSION, toolVersion);
	}

	public void editMaxTime(long maxTime)
	{
		editCustomData(QtiConstants.KEY_MAX_TIME, maxTime);
	}

	public void editQuestionCount(int questionCount)
	{
		editCustomData(QtiConstants.KEY_QUESTION_COUNT, questionCount);
	}

	public void editSectionCount(int sectionCount)
	{
		editCustomData(QtiConstants.KEY_SECTION_COUNT, sectionCount);
	}

	public void editNavigationMode(String navigationMode)
	{
		editCustomData(QtiConstants.KEY_NAVIGATION_MODE, navigationMode);
	}

	public void editManifestPath(String manifestPath)
	{
		if( hasBeenEdited(customAttachment.getUrl(), manifestPath) )
		{
			customAttachment.setUrl(manifestPath);
		}
	}
}
