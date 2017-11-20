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
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;

@Bind
@Singleton
public class ImageThumbnailGenerator implements ThumbnailGenerator
{
	@Inject
	private ImageMagickService imageMagickService;

	@Override
	public void generateThumbnail(File src, File dest)
	{
		imageMagickService.generateStandardThumbnail(src, dest);
	}

	@Override
	public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) throws Exception
	{
		imageMagickService.generateThumbnailAdvanced(srcFile, dstFile, options);
	}

	@Override
	public Dimension getImageDimensions(File srcFile) throws IOException
	{
		return imageMagickService.getImageDimensions(srcFile);
	}

	@Override
	public boolean supportsThumbType(ThumbnailType type)
	{
		return true;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}
}
