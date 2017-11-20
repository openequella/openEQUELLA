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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class RootRenderer extends Renderer
{
	private static final String START_TEXT = "Contribute";
	private static final String END_TEXT = "Live";
	private static final int OVAL_HEIGHT = 40;

	private final Map<Color, String> legendText;

	public RootRenderer(Renderer child)
	{
		super(null);
		addChild(child);

		legendText = new HashMap<Color, String>();
	}

	@Override
	public void draw(Graphics g, Rectangle bounds)
	{
		g.setFont(Renderer.DEFAULT_FONT);

		final int centreX = (int) bounds.getCenterX();
		int topY = bounds.y;

		// Draw our start point
		drawCircledText(g, START_TEXT, centreX, topY);
		topY += OVAL_HEIGHT;

		drawLeadingOutArrow(g, centreX, topY, HALF_ARROW_HEIGHT);
		topY += HALF_ARROW_HEIGHT;

		// Get ready to draw our child
		Renderer child = getChild(0);
		Dimension childSize = child.getSize(g);
		Rectangle childBound = new Rectangle();
		childBound.x = centreX - (childSize.width / 2);
		childBound.y = topY;
		childBound.width = childSize.width;
		childBound.height = childSize.height;

		child.draw(g, childBound);
		topY += childSize.height;

		// Draw our end point
		drawLeadingInArrow(g, centreX, topY, HALF_ARROW_HEIGHT);
		topY += HALF_ARROW_HEIGHT;

		drawCircledText(g, END_TEXT, centreX, topY);
		topY += OVAL_HEIGHT;

		// Draw any legend elements
		topY += LEGEND_PADDING;
		g.setFont(Renderer.DEFAULT_FONT);
		int legendX = bounds.x + PADDING_SIZE;
		for( Map.Entry<Color, String> entry : legendText.entrySet() )
		{
			int textHeight = (int) getTextSize(g, entry.getValue()).getHeight();

			g.setColor(entry.getKey());
			g.fillRect(legendX, topY, textHeight, textHeight);
			g.setColor(Color.BLACK);
			g.drawRect(legendX, topY, textHeight, textHeight);

			g.drawString(entry.getValue(), legendX + textHeight + PADDING_SIZE, topY + textHeight);

			topY += textHeight + LEGEND_PADDING;
		}
	}

	private void drawCircledText(Graphics g, String text, int centreX, int topY)
	{
		// Make some coordinate calculations
		Rectangle2D textSize = g.getFontMetrics().getStringBounds(text, g);
		int halfDiamondY = OVAL_HEIGHT / 2;

		int textStartX = centreX - (int) (textSize.getWidth() / 2);
		int textStartY = topY + halfDiamondY + (int) (textSize.getHeight() / 2);

		int ovalStartX = textStartX - PADDING_SIZE;
		int ovalWidth = (int) textSize.getWidth() + (PADDING_SIZE * 2);

		// Draw the oval around the text
		g.setColor(Color.BLACK);
		g.fillOval(ovalStartX, topY, ovalWidth, OVAL_HEIGHT);

		// Draw our text
		Font backup = g.getFont();
		g.setFont(new Font(backup.getName(), Font.BOLD, backup.getSize()));
		g.setColor(Color.WHITE);
		g.drawString(text, textStartX - 3, textStartY - 3);
		g.setFont(backup);
	}

	@Override
	public Dimension getSize(Graphics g)
	{
		Dimension total = getChild(0).getSize(g);

		int legendHeight = 0;

		if( g != null )
		{
			total.width = Math.max(total.width, (int) getTextSize(g, START_TEXT).getWidth());

			for( String legend : legendText.values() )
			{
				Rectangle2D legSize = getTextSize(g, legend);

				// We use the height of the legend text as the width/height of
				// the colour box.
				int legWidth = (int) legSize.getHeight() + PADDING_SIZE + (int) legSize.getWidth();
				total.width = Math.max(total.width, legWidth);

				legendHeight += legSize.getHeight() + LEGEND_PADDING;
			}
		}

		total.width += PADDING_SIZE * 2;
		total.height += (OVAL_HEIGHT * 2) + (HALF_ARROW_HEIGHT * 2) + LEGEND_PADDING + legendHeight;

		return total;
	}

	private Rectangle2D getTextSize(Graphics g, String text)
	{
		g.setFont(Renderer.DEFAULT_FONT);
		return g.getFontMetrics().getStringBounds(text, g);
	}

	public void addColourLegend(Color colour, String text)
	{
		legendText.put(colour, text);
	}
}
