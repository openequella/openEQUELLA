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

package com.tle.core.institution.convert.importhandler;

import java.util.Iterator;

/**
 * Interface for different importer implementations. Older versions of 3.1 and
 * 3.2 would generate exports with a single file that contained all the nodes
 * (see SingleFileImportHandler), while the new format separates these out
 * sequentially numbered files.
 */
public interface ImportHandler<NODE_TYPE>
{
	int getNodeCount();

	Iterator<NODE_TYPE> iterateNodes();
}