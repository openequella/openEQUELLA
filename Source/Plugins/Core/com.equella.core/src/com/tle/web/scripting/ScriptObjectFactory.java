/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.scripting;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.util.Logger;
import com.tle.core.guice.BindFactory;
import com.tle.web.scripting.objects.*;

@BindFactory
public interface ScriptObjectFactory
{
	FileScriptingObjectImpl createFile(@Assisted("handle") FileHandle handle);

	NavigationScriptWrapper createNavigation(@Assisted("item") Item item);

	AttachmentsScriptWrapper createAttachments(@Assisted("attachments") ModifiableAttachments attachments, @Assisted("staging") FileHandle staging);

	UserScriptWrapper createUser();

	LoggingScriptWrapper createLogger(@Assisted("logger") Logger logger);

	DrmScriptWrapper createDrm(@Assisted("item") Item item, @Assisted("drmSettings") DrmSettings drmSettings);

	ImagesScriptWrapper createImages(@Assisted("handle") FileHandle handle);
}
