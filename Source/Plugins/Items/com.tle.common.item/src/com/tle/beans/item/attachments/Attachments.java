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

package com.tle.beans.item.attachments;

import java.util.Iterator;
import java.util.List;

/*
 * @author aholland
 */
public interface Attachments extends Iterable<IAttachment>
{
	/**
	 * @param <T>
	 * @param attachmentType
	 * @return An UNMODIFIABLE list
	 */
	<T extends IAttachment> List<T> getList(AttachmentType attachmentType);

	ImsAttachment getIms();

	<T extends IAttachment> Iterator<T> getIterator(AttachmentType attachmentType);

	List<CustomAttachment> getCustomList(String type);

	CustomAttachment getFirstCustomOfType(String type);

	IAttachment getAttachmentByUuid(String uuid);

	IAttachment getAttachmentByFilename(String filename);

	/**
	 * Does a UUID comparison.
	 * 
	 * @param attachment
	 * @return
	 */
	boolean contains(IAttachment attachment);
}