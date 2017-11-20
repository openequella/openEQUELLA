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

package com.tle.core.imagemagick;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import com.tle.common.filesystem.handle.FileHandle;

public interface ImageMagickService
{
	boolean supported(String mimeType);

	Dimension getImageDimensions(FileHandle handle, String filename) throws IOException;

	Dimension getImageDimensions(File image) throws IOException;

	void sample(File src, File dest, String width, String height, String... options) throws IOException;

	void crop(File src, File dest, String width, String height, String... options) throws IOException;

	void rotate(File src, File dest, int angle, String... options) throws IOException;

	/**
	 * @param srcFile
	 * @param dstFile
	 * @param format Not used
	 * @param options
	 */
	void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options);

	void generateStandardThumbnail(File srcFile, File dstFile);

	void sampleNoRatio(File src, File dest, String width, String height, String... options) throws IOException;

}
