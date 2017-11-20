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

package com.tle.web.scripting.advanced.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.web.scripting.advanced.types.MimeTypeScriptType;

/**
 * Referenced by the 'mime' variable in script.
 * 
 * @author aholland
 */
public interface MimeScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "mime"; //$NON-NLS-1$

	/**
	 * Get at MimeTypeScriptType for the given filename (will read the extension
	 * on the filename to determine the MIME type)
	 * 
	 * @param filename The name of the file
	 * @return A MimeTypeScriptType object, or null if the mime type could not
	 *         be determined.
	 */
	MimeTypeScriptType getMimeTypeForFilename(String filename);
}
