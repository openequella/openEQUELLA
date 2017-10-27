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

public class TaskRenderer extends Renderer
{
	private String displayName;

	public TaskRenderer(WorkflowNode node)
	{
		super(node);
		displayName = CurrentLocale.get(node.getName());
	}

	@Override
	public void draw(Graphics g, Rectangle bounds)
	{
		// Make a few coordinate calculations
		Dimension totalTextSize = getTextArea(g);

		int maxTextStartX = (int) (bounds.getCenterX() - (totalTextSize.getWidth() / 2));
		int maxTextStartY = (int) (bounds.getCenterY() + (totalTextSize.getHeight() / 2));

		int boxStartY = maxTextStartY - (int) totalTextSize.getHeight() - PADDING_SIZE;
		int boxStartX = maxTextStartX - PADDING_SIZE;
		int boxWidth = (int) totalTextSize.getWidth() + (PADDING_SIZE * 2);
		int boxHeight = (int) totalTextSize.getHeight() + (PADDING_SIZE * 2);

		// Draw the box
		if( getHilight() != null )
		{
			g.setColor(getHilight());
			g.fillRect(boxStartX, boxStartY, boxWidth, boxHeight);
		}
		g.setColor(Color.BLACK);
		g.drawRect(boxStartX, boxStartY, boxWidth, boxHeight);

		// Draw the text
		Dimension textSize = getTextArea(g, displayName);
		int textX = (int) (bounds.getCenterX() - (textSize.getWidth() / 2));
		int textY = boxStartY + PADDING_SIZE + (int) textSize.getHeight();

		g.setFont(DEFAULT_FONT);
		g.drawString(displayName, textX, textY - 2);

		if( getMessage() != null )
		{
			textSize = getTextArea(g, getMessage());
			textX = (int) (bounds.getCenterX() - (textSize.getWidth() / 2));
			textY += NOTE_PADDING + (int) textSize.getHeight();

			g.setFont(NOTE_FONT);
			g.drawString(getMessage(), textX, textY - 2);
		}

		// Draw leading arrow
		drawLeadingInArrow(g, (int) bounds.getCenterX(), bounds.y, boxStartY - bounds.y);

		// Draw trailing arrow
		int arrowX = (int) bounds.getCenterX();
		int arrowY = boxStartY + boxHeight;
		int arrowLength = (bounds.y + bounds.height) - (boxStartY + boxHeight);
		drawLeadingOutArrow(g, arrowX, arrowY, arrowLength);
	}

	@Override
	public Dimension getSize(Graphics g)
	{
		Dimension d = getTextArea(g);

		d.width += PADDING_SIZE * 2;
		d.height += (PADDING_SIZE * 2) + (HALF_ARROW_HEIGHT * 2);

		return d;
	}

	private Dimension getTextArea(Graphics g)
	{
		Dimension d = new Dimension();
		if( g != null )
		{
			g.setFont(DEFAULT_FONT);
			d = getTextArea(g, displayName);
			if( getMessage() != null )
			{
				Dimension d2 = getTextArea(g, getMessage());
				d.width = Math.max(d.width, d2.width);
				d.height += NOTE_PADDING + d2.height;
			}
		}
		return d;
	}

	private Dimension getTextArea(Graphics g, String text)
	{
		Dimension d = new Dimension();
		if( g != null )
		{
			g.setFont(DEFAULT_FONT);
			Rectangle2D textSize = g.getFontMetrics().getStringBounds(text, g);
			d.width = (int) textSize.getWidth();
			d.height = (int) textSize.getHeight();
		}
		return d;
	}
}
