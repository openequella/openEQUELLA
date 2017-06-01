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

package com.tle.core.qti.parse.v1x;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a QTI material node, which can contain a number of different
 * elements
 * 
 * @author will
 */
public class QTIMaterial implements Serializable
{
	private static final long serialVersionUID = 1L;
	private List<QTIMaterialElement> elements = new ArrayList<QTIMaterialElement>();

	public QTIMaterial()
	{

	}

	public QTIMaterial(List<QTIMaterialElement> elements)
	{
		this.elements = elements;
	}

	public QTIMaterial(QTIMaterialElement element)
	{
		this.elements.add(element);
	}

	public void setElements(List<QTIMaterialElement> elements)
	{
		this.elements = elements;
	}

	public List<QTIMaterialElement> getElements()
	{
		return elements;
	}

	public void add(QTIMaterialElement element)
	{
		elements.add(element);
	}

	public void addAll(List<QTIMaterialElement> elements)
	{
		this.elements.addAll(elements);
	}

	public String getHtml()
	{
		StringBuilder html = new StringBuilder();

		for( QTIMaterialElement ele : elements )
		{
			html.append(ele.getHtml());
		}
		return html.toString();
	}
}
