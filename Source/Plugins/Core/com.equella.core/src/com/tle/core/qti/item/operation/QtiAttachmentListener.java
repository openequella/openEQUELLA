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

package com.tle.core.qti.item.operation;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.PathUtils;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemAttachmentListener;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.qti.QtiConstants;
import com.tle.core.qti.service.QtiAssessmentTestService;
import com.tle.core.qti.service.QtiService;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class QtiAttachmentListener implements ItemAttachmentListener
{
	@Inject
	private QtiService qtiService;
	@Inject
	private QtiAssessmentTestService qtiTestService;

	@SuppressWarnings("nls")
	@Override
	public void attachmentsChanged(ItemEditor editor, Item item, FileHandle handle)
	{
		final UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);
		final List<CustomAttachment> customs = attachments.getList(AttachmentType.CUSTOM);

		if( customs.isEmpty() )
		{
			qtiTestService.deleteForItemId(item.getId());
		}
		else
		{
			for( CustomAttachment att : customs )
			{
				if( att.getType().equals("qtitest") )
				{
					final String testUuid = (String) att.getData(QtiConstants.KEY_TEST_UUID);
					final QtiAssessmentTest test = qtiTestService.findByUuid(testUuid);

					if( test == null )
					{
						final QtiAssessmentTest existingTest = qtiTestService.findByItem(item);
						if( existingTest != null )
						{
							qtiTestService.delete(existingTest);
						}

						final String xmlPath = (String) att.getData(QtiConstants.KEY_XML_PATH);
						final String xmlRelPath = PathUtils.relativize(QtiConstants.QTI_FOLDER_PATH, xmlPath);
						final ResolvedAssessmentTest quiz = qtiService.loadV2Test(handle, QtiConstants.QTI_FOLDER_PATH,
							xmlRelPath);
						final QtiAssessmentTest testEntity = qtiTestService.convertTestToEntity(quiz, item, xmlPath,
							testUuid);
						qtiTestService.save(testEntity);
						return;
					}
				}
			}
		}
	}
}
