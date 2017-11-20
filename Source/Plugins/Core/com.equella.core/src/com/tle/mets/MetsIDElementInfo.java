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

package com.tle.mets;

import com.dytech.devlib.PropBagEx;

import edu.harvard.hul.ois.mets.helper.MetsIDElement;

/**
 * @author Aaron
 */
public class MetsIDElementInfo<T extends MetsIDElement>
{
	private final T elem;
	private final String mimeType;
	private final PropBagEx xml;

	public MetsIDElementInfo(T elem, String mimeType, PropBagEx xml)
	{
		this.elem = elem;
		this.mimeType = mimeType;
		this.xml = xml;
	}

	public T getElem()
	{
		return elem;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public PropBagEx getXml()
	{
		return xml;
	}
}