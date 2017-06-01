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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.WorkflowNode;

public class DecisionRenderer extends SerialRenderer
{
	private static final int DIAMOND_SIZE = 40;

	private String nodeName;

	public DecisionRenderer(WorkflowNode node)
	{
		super(node);
		nodeName = CurrentLocale.get(node.getName());
	}

	@Override
	public void draw(Graphics g, Rectangle bounds)
	{
		final int centreX = (int) bounds.getCenterX();

		// Draw the leading arrow
		drawLeadingInArrow(g, centreX, bounds.y, HALF_ARROW_HEIGHT);

		// Draw the trailing arrow
		drawLeadingOutArrow(g, centreX, bounds.y + bounds.height - HALF_ARROW_HEIGHT, HALF_ARROW_HEIGHT);

		// Get ready to draw our child
		Dimension childSize = super.getSizeWithNoPadding(g);
		Rectangle childBound = new Rectangle();
		childBound.x = centreX - (childSize.width / 2);
		childBound.y = bounds.y + (HALF_ARROW_HEIGHT * 2) + DIAMOND_SIZE;
		childBound.width = childSize.width;
		childBound.height = childSize.height;

		// Draw child
		super.draw(g, childBound);

		// Do some general calculations
		Rectangle2D textSize = g.getFontMetrics().getStringBounds(nodeName, g);

		int diamondTop = bounds.y + HALF_ARROW_HEIGHT;
		int halfDiamondY = DIAMOND_SIZE / 2;
		int halfDiamondX = (int) (textSize.getWidth() / 2) + (PADDING_SIZE * 2);

		int textStartX = centreX - (int) (textSize.getWidth() / 2);
		int textStartY = diamondTop + halfDiamondY + (int) (textSize.getHeight() / 2);

		// Draw the diamond
		int[] pointsX = {centreX, centreX - halfDiamondX, centreX, centreX + halfDiamondX};
		int[] pointsY = {diamondTop, diamondTop + halfDiamondY, diamondTop + DIAMOND_SIZE, diamondTop + halfDiamondY};

		if( getHilight() != null )
		{
			g.setColor(getHilight());
			g.fillPolygon(pointsX, pointsY, pointsX.length);
		}

		g.setColor(Color.BLACK);
		g.drawPolygon(pointsX, pointsY, pointsX.length);

		// Draw the text
		g.setColor(Color.BLACK);
		g.drawString(nodeName, textStartX, textStartY - 3);

		// Draw the arrow leading out from the diamond
		drawLeadingOutArrow(g, centreX, diamondTop + DIAMOND_SIZE, HALF_ARROW_HEIGHT);

		// Draw connecting lines
		int lineX1 = centreX - (childSize.width / 2) - PADDING_SIZE;
		int lineX2 = centreX - halfDiamondX;
		int lineY1 = diamondTop + halfDiamondY;
		int lineY2 = lineY1 + HALF_ARROW_HEIGHT + halfDiamondY + childSize.height;

		lineX1 = Math.min(lineX1, lineX2);

		g.setColor(ARROW_COLOUR);
		g.drawLine(lineX1, lineY1, lineX2, lineY1);
		g.drawLine(lineX1, lineY2, centreX, lineY2);
		g.drawLine(lineX1, lineY1, lineX1, lineY2);

		// Draw sideways pointer on arrow
		g.drawLine(centreX - ARROW_HEAD_HEIGHT, lineY2 - ARROW_HEAD_WIDTH, centreX, lineY2);
		g.drawLine(centreX - ARROW_HEAD_HEIGHT, lineY2 + ARROW_HEAD_WIDTH, centreX, lineY2);
	}

	@Override
	public Dimension getSize(Graphics g)
	{
		// Get the size of the children first
		Dimension total = super.getSizeWithNoPadding(g);
		total.width += PADDING_SIZE * 4;

		if( g != null )
		{
			Rectangle2D textSize = g.getFontMetrics().getStringBounds(nodeName, g);
			total.width = (int) Math.max(total.width, textSize.getWidth() + (PADDING_SIZE * 4));
		}

		total.height += DIAMOND_SIZE + (HALF_ARROW_HEIGHT * 3);
		return total;
	}
}
