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

// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: VerticalFlowLayout.java

package com.dytech.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

public class VerticalFlowLayout extends FlowLayout
{
	public static final int TOP = 0;
	public static final int MIDDLE = 1;
	public static final int BOTTOM = 2;

	private int hgap;
	private int vgap;
	private boolean hfill;
	private boolean vfill;
	private int hAlign = LEFT;

	public VerticalFlowLayout()
	{
		this(0, 5, 5, true, false);
	}

	public VerticalFlowLayout(boolean flag, boolean flag1)
	{
		this(0, 5, 5, flag, flag1);
	}

	public VerticalFlowLayout(int i)
	{
		this(i, 5, 5, true, false);
	}

	public VerticalFlowLayout(int i, boolean flag, boolean flag1)
	{
		this(i, 5, 5, flag, flag1);
	}

	public VerticalFlowLayout(int i, int j, int k, boolean flag, boolean flag1)
	{
		setAlignment(i);
		hgap = j;
		vgap = k;
		hfill = flag;
		vfill = flag1;
	}

	@Override
	public Dimension preferredLayoutSize(Container container)
	{
		Dimension dimension = new Dimension(0, 0);
		for( int i = 0; i < container.getComponentCount(); i++ )
		{
			Component component = container.getComponent(i);
			if( component.isVisible() )
			{
				Dimension dimension1 = component.getPreferredSize();
				dimension.width = Math.max(dimension.width, dimension1.width);
				if( i > 0 )
				{
					dimension.height += hgap;
				}
				dimension.height += dimension1.height;
			}
		}

		Insets insets = container.getInsets();
		dimension.width += insets.left + insets.right + hgap * 2;
		dimension.height += insets.top + insets.bottom + vgap * 2;
		return dimension;
	}

	@Override
	public Dimension minimumLayoutSize(Container container)
	{
		Dimension dimension = new Dimension(0, 0);
		for( int i = 0; i < container.getComponentCount(); i++ )
		{
			Component component = container.getComponent(i);
			if( component.isVisible() )
			{
				Dimension dimension1 = component.getMinimumSize();
				dimension.width = Math.max(dimension.width, dimension1.width);
				if( i > 0 )
				{
					dimension.height += vgap;
				}
				dimension.height += dimension1.height;
			}
		}

		Insets insets = container.getInsets();
		dimension.width += insets.left + insets.right + hgap * 2;
		dimension.height += insets.top + insets.bottom + vgap * 2;
		return dimension;
	}

	public void setVerticalFill(boolean flag)
	{
		vfill = flag;
	}

	public boolean getVerticalFill()
	{
		return vfill;
	}

	public void setHorizontalFill(boolean flag)
	{
		hfill = flag;
	}

	public boolean getHorizontalFill()
	{
		return hfill;
	}

	private void placethem(Container container, int x, int y, int l, int first, int last)
	{
		// Set the x position:
		Insets insets = container.getInsets();
		int width = (int) (container.getSize().getWidth());
		int right = width - insets.right;
		int middle = insets.left + ((width - insets.left - insets.right) / 2);

		int k1 = getAlignment();
		if( k1 == 1 )
		{
			y += l / 2;
		}

		if( k1 == 2 )
		{
			y += l;
		}

		for( int i = first; i < last; i++ )
		{
			Component component = container.getComponent(i);
			Dimension size = component.getSize();
			if( component.isVisible() )
			{
				if( hAlign == CENTER )
				{
					x = middle - size.width / 2;
				}
				else if( hAlign == RIGHT )
				{
					x = right - size.width;
				}
				component.setLocation(x, y);
				y += vgap + size.height;
			}
		}
	}

	/**
	 * Should be: LEFT RIGHT CENTER
	 */
	public void setHorizontalAlignment(int align)
	{
		hAlign = align;
	}

	@Override
	public void layoutContainer(Container container)
	{
		Insets insets = container.getInsets();
		int i = container.getSize().height - (insets.top + insets.bottom + vgap * 2);
		int j = container.getSize().width - (insets.left + insets.right + hgap * 2);
		int k = container.getComponentCount();
		int x = insets.left + hgap;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		for( int l1 = 0; l1 < k; l1++ )
		{
			Component component = container.getComponent(l1);
			if( component.isVisible() )
			{
				Dimension dimension = component.getPreferredSize();
				if( vfill && l1 == k - 1 )
				{
					dimension.height = Math.max(i - i1, component.getPreferredSize().height);
				}

				if( hfill )
				{
					component.setSize(j, dimension.height);
					dimension.width = j;
				}
				else
				{
					component.setSize(dimension.width, dimension.height);
				}

				if( i1 + dimension.height > i )
				{
					placethem(container, x, insets.top + vgap, i - i1, k1, l1);
					i1 = dimension.height;
					x += hgap + j1;
					j1 = dimension.width;
					k1 = l1;
				}
				else
				{
					if( i1 > 0 )
					{
						i1 += vgap;
					}
					i1 += dimension.height;
					j1 = Math.max(j1, dimension.width);
				}
			}
		}

		placethem(container, x, insets.top + vgap, i - i1, k1, k);
	}
}
