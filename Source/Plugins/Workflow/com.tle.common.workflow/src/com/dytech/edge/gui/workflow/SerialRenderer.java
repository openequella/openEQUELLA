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

package com.dytech.edge.gui.workflow;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.tle.common.workflow.node.WorkflowNode;

public class SerialRenderer extends Renderer
{
	public SerialRenderer(WorkflowNode node)
	{
		super(node);
	}

	@Override
	public void draw(Graphics g, Rectangle bounds)
	{
		final int childWidth = bounds.width - (PADDING_SIZE * 2);

		// Start placing children from here...
		int nextY = bounds.y;

		final int childCount = getChildCount();
		for( int i = 0; i < childCount; i++ )
		{
			Renderer child = getChild(i);

			// Determine child boundries
			Rectangle childBound = new Rectangle();
			childBound.x = bounds.x + PADDING_SIZE;
			childBound.y = nextY;
			childBound.width = childWidth;
			childBound.height = child.getSize(g).height;

			// Draw child
			child.draw(g, childBound);

			// Next child Y coordinate.
			nextY += childBound.height;
		}

		int maxY = bounds.y + bounds.height;
		if( nextY < maxY )
		{
			int centreX = (int) bounds.getCenterX();
			g.setColor(ARROW_COLOUR);
			g.drawLine(centreX, nextY, centreX, maxY);
		}
	}

	protected Dimension getSizeWithNoPadding(Graphics g)
	{
		Dimension total = new Dimension();

		final int childCount = getChildCount();
		for( int i = 0; i < childCount; i++ )
		{
			Dimension childSize = getChild(i).getSize(g);

			total.height += childSize.height;
			total.width = Math.max(total.width, childSize.width);
		}
		return total;
	}

	@Override
	public Dimension getSize(Graphics g)
	{
		Dimension total = getSizeWithNoPadding(g);
		total.width += PADDING_SIZE * 2;
		return total;
	}
}
