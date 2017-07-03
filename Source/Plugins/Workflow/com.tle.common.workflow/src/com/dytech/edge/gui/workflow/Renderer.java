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
import java.util.ArrayList;
import java.util.List;

import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Nicholas Read
 */
public abstract class Renderer
{
	protected static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12); //$NON-NLS-1$
	protected static final Font NOTE_FONT = new Font("SansSerif", Font.ITALIC, 12); //$NON-NLS-1$

	protected static final Color ARROW_COLOUR = Color.GRAY;
	protected static final int PADDING_SIZE = 10;
	protected static final int NOTE_PADDING = 5;
	protected static final int LEGEND_PADDING = 5;
	protected static final int HALF_ARROW_HEIGHT = 10;
	protected static final int ARROW_HEAD_WIDTH = 3;
	protected static final int ARROW_HEAD_HEIGHT = 7;

	private WorkflowNode node;
	private Color hilight;
	private String message;
	private List<Renderer> children;

	public Renderer(WorkflowNode node)
	{
		this.node = node;

		children = new ArrayList<Renderer>();
	}

	public abstract void draw(Graphics g, Rectangle bounds);

	public abstract Dimension getSize(Graphics g);

	public Color getHilight()
	{
		return hilight;
	}

	public void setHilight(Color hilight)
	{
		this.hilight = hilight;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String note)
	{
		this.message = note;
	}

	public String getNodeID()
	{
		if( node == null )
		{
			return null;
		}
		else
		{
			return node.getUuid();
		}
	}

	public void addChild(Renderer child)
	{
		children.add(child);
	}

	public int getChildCount()
	{
		return children.size();
	}

	public Renderer getChild(int index)
	{
		return children.get(index);
	}

	protected void drawArrow(Graphics g, int x, int y, int length, boolean drawPointer)
	{
		int y2 = y + length;
		g.setColor(ARROW_COLOUR);
		g.drawLine(x, y, x, y2);
		if( drawPointer )
		{
			g.drawLine(x - ARROW_HEAD_WIDTH, y2 - ARROW_HEAD_HEIGHT, x, y2);
			g.drawLine(x + ARROW_HEAD_WIDTH, y2 - ARROW_HEAD_HEIGHT, x, y2);
		}
	}

	protected void drawLeadingOutArrow(Graphics g, int x, int y, int length)
	{
		drawArrow(g, x, y, length, false);
	}

	protected void drawLeadingInArrow(Graphics g, int x, int y, int length)
	{
		drawArrow(g, x, y, length, true);
	}
}
