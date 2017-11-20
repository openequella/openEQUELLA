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

package com.tle.core.workflow.thumbnail;

import java.awt.Dimension;
import java.io.File;

import com.tle.core.imagemagick.ThumbnailOptions;

public interface ThumbnailGenerator
{
	void generateThumbnail(File src, File dest) throws Exception;

	void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) throws Exception;

	Dimension getImageDimensions(File srcFile) throws Exception;

	/**
	 * 
	 * @param type
	 * @return
	 */
	boolean supportsThumbType(ThumbnailType type);

	/**
	 * E.g. video thumbnailer may be disabled because of not being installed
	 * @return
	 */
	boolean isEnabled();
}
