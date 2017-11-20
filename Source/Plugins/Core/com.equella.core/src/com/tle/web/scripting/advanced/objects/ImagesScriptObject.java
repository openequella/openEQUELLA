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

import java.awt.Dimension;
import java.io.IOException;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'images' variable in script.
 * 
 * @author aholland
 */
public interface ImagesScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "images"; //$NON-NLS-1$

	/**
	 * Get the dimensions of the image found at path.
	 * 
	 * @param path The file path to the image, relative to the item
	 * @return A Dimension object with a getWidth() and a getHeight() method
	 * @throws IOException If there were problems getting the dimensions of the
	 *             image, e.g. there is no file at path or the image was in an
	 *             unrecognised format.
	 */
	Dimension getDimensions(String path) throws IOException;

	/**
	 * Creates a new image from an existing image with new dimensions.
	 * 
	 * @param path The file path to the image to resize, relative to the item
	 * @param newWidth The width of the new image
	 * @param newHeight The height of the new image
	 * @param newPath The file path to the image to create, relative to the item
	 * @throws IOException If there were problems resizing the image, e.g. there
	 *             is no file at path or the image was in an unrecognised
	 *             format.
	 */
	void resize(String path, int newWidth, int newHeight, String newPath) throws IOException;

	// Needs a ScriptingFileHandleType
	// FileHandle createThumbnail(String path) throws IOException;
}
