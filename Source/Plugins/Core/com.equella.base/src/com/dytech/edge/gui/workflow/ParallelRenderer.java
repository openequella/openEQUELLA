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

public class ParallelRenderer extends Renderer
{
	public ParallelRenderer(WorkflowNode node)
	{
		super(node);
	}

	@Override
	public void draw(Graphics g, Rectangle bounds)
	{
		final int childY = bounds.y + HALF_ARROW_HEIGHT;
		final int centreX = (int) bounds.getCenterX();
		final int childHeight = bounds.height - (HALF_ARROW_HEIGHT * 2);

		// Draw the leading arrow
		drawLeadingInArrow(g, centreX, bounds.y, HALF_ARROW_HEIGHT);

		// Draw the trailing arrow
		drawLeadingOutArrow(g, centreX, bounds.y + bounds.height - HALF_ARROW_HEIGHT, HALF_ARROW_HEIGHT);

		final int childCount = getChildCount();
		int nextX = centreX + PADDING_SIZE / 2;
		for( int i = 0; i < childCount; i++ )
		{
			Renderer child = getChild(i);
			nextX -= (child.getSize(g).width + PADDING_SIZE) / 2;
		}

		int middleOfFirstChild = -1;
		int middleOfLastChild = -1;
		for( int i = 0; i < childCount; i++ )
		{
			Renderer child = getChild(i);

			// If first child
			if( i == 0 )
			{
				middleOfFirstChild = nextX + (child.getSize(g).width / 2);
			}

			// If last child
			if( i + 1 == childCount )
			{
				middleOfLastChild = nextX + (child.getSize(g).width / 2);
			}

			// Determine child boundries
			Rectangle childBound = new Rectangle();
			childBound.x = nextX;
			childBound.y = childY;
			childBound.width = child.getSize(g).width;
			childBound.height = childHeight;

			// Draw child
			child.draw(g, childBound);

			// Next child X coordinate.
			nextX += childBound.width + PADDING_SIZE;
		}

		g.setColor(ARROW_COLOUR);
		int lineY1 = bounds.y + HALF_ARROW_HEIGHT;
		g.drawLine(middleOfFirstChild, lineY1, middleOfLastChild, lineY1);
		int lineY2 = bounds.y + bounds.height - HALF_ARROW_HEIGHT;
		g.drawLine(middleOfFirstChild, lineY2, middleOfLastChild, lineY2);
	}

	@Override
	public Dimension getSize(Graphics g)
	{
		Dimension total = new Dimension();

		final int childCount = getChildCount();
		for( int i = 0; i < childCount; i++ )
		{
			Dimension childSize = getChild(i).getSize(g);

			total.height = Math.max(total.height, childSize.height);
			total.width += childSize.width;
		}

		// Add the arrow heights
		total.height += HALF_ARROW_HEIGHT * 2;

		// Add width padding
		total.width += (childCount + 1) * PADDING_SIZE;

		return total;
	}
}
