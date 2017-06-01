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

public class ThumbnailOptions
{
	private int imgWidth;
	private int imgHeight;
	private int width;
	private int height;
	private String gravity;
	private int cropWidth;
	private int cropHeight;
	private int cropX;
	private int cropY;
	private boolean noSize;
	private boolean skipBlankCheck;
	private String backgroundColour;
	private boolean keepAspect;

	public boolean isKeepAspect()
	{
		return keepAspect;
	}

	public void setKeepAspect(boolean keepAspect)
	{
		this.keepAspect = keepAspect;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public String getGravity()
	{
		return gravity;
	}

	public void setGravity(String gravity)
	{
		this.gravity = gravity;
	}

	public int getCropWidth()
	{
		return cropWidth;
	}

	public void setCropWidth(int cropWidth)
	{
		this.cropWidth = cropWidth;
	}

	public int getCropHeight()
	{
		return cropHeight;
	}

	public void setCropHeight(int cropHeight)
	{
		this.cropHeight = cropHeight;
	}

	public int getCropX()
	{
		return cropX;
	}

	public void setCropX(int cropX)
	{
		this.cropX = cropX;
	}

	public int getCropY()
	{
		return cropY;
	}

	public void setCropY(int cropY)
	{
		this.cropY = cropY;
	}

	public int getImgWidth()
	{
		return imgWidth;
	}

	public void setImgWidth(int imgWidth)
	{
		this.imgWidth = imgWidth;
	}

	public int getImgHeight()
	{
		return imgHeight;
	}

	public void setImgHeight(int imgHeight)
	{
		this.imgHeight = imgHeight;
	}

	public boolean isNoSize()
	{
		return noSize;
	}

	public void setNoSize(boolean noSize)
	{
		this.noSize = noSize;
	}

	public boolean isSkipBlankCheck()
	{
		return skipBlankCheck;
	}

	public void setSkipBlankCheck(boolean skipBlankCheck)
	{
		this.skipBlankCheck = skipBlankCheck;
	}

	public String getBackgroundColour()
	{
		return backgroundColour;
	}

	public void setBackgroundColour(String backgroundColour)
	{
		this.backgroundColour = backgroundColour;
	}

}
