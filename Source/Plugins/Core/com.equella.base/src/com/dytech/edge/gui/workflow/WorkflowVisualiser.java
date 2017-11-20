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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

import javax.imageio.ImageIO;

import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author Nicholas Read
 */
public class WorkflowVisualiser extends Canvas
{
	private static final int PADDING = 10;
	private static final long serialVersionUID = 1L;

	private final RootRenderer rootRenderer;
	private Dimension cachedSize;
	private final Dimension desiredSize;

	/**
	 * Constructs a new WorkflowVisualiser.
	 * 
	 * @param root the root node of the workflow.
	 */
	public WorkflowVisualiser(WorkflowNode root, Dimension desiredSize)
	{
		rootRenderer = new RootRenderer(createRenderer(root));
		this.desiredSize = desiredSize;
	}

	/**
	 * Creates a renderer for the given workflow node and each of it's children.
	 */
	private Renderer createRenderer(WorkflowNode node)
	{
		Renderer renderer = null;

		switch( node.getType() )
		{
			case WorkflowNode.SERIAL_TYPE:
				renderer = new SerialRenderer(node);
				break;

			case WorkflowNode.PARALLEL_TYPE:
				renderer = new ParallelRenderer(node);
				break;

			case WorkflowNode.DECISION_TYPE:
				renderer = new DecisionRenderer(node);
				break;

			case WorkflowNode.ITEM_TYPE:
				renderer = new TaskRenderer(node);
				break;

			case WorkflowNode.SCRIPT_TYPE:
				renderer = new TaskRenderer(node);
				break;

			default:
				throw new IllegalArgumentException("Unknown workflow type " + node.getType());
		}

		int count = node.numberOfChildren();
		for( int i = 0; i < count; i++ )
		{
			renderer.addChild(createRenderer(node.getChild(i)));
		}

		return renderer;
	}

	@Override
	public void paint(Graphics g)
	{
		g.setColor(Color.WHITE);
		Dimension canvasSize = super.getSize();

		Dimension requiredSize = getSize(g);
		int left;
		int top;
		if( desiredSize != null )
		{
			g.fillRect(0, 0, canvasSize.width, canvasSize.height);
			left = (canvasSize.width - requiredSize.width) / 2;
			top = (canvasSize.height - requiredSize.height) / 2;
		}
		else
		{
			left = PADDING;
			top = PADDING;
		}
		rootRenderer.draw(g, new Rectangle(left, top, requiredSize.width, requiredSize.height));
	}

	@Override
	public Dimension getSize()
	{
		return getSize(getGraphics());
	}

	public Dimension getSize(Graphics g)
	{
		if( g == null )
		{
			return desiredSize;
		}
		cachedSize = rootRenderer.getSize(g);
		return cachedSize;
	}

	@Override
	public int getWidth()
	{
		return getSize().width;
	}

	@Override
	public int getHeight()
	{
		return getSize().height;
	}

	/**
	 * Hilights a node with the given colour.
	 * 
	 * @param hilight the colour to hilight it. <code>null</code> for
	 *            transparent.
	 * @param nodeID the ID of the node to hilight.
	 */
	public void setColourForNode(Color hilight, String nodeID)
	{
		setColourForNodes(hilight, Collections.singleton(nodeID));
	}

	/**
	 * Hilights a set of nodess with the given colour.
	 * 
	 * @param hilight the colour to hilight each node. <code>null</code> for
	 *            transparent.
	 * @param nodeIDs the IDs of the nodes to hilight.
	 */
	public void setColourForNodes(Color hilight, Set<String> nodeIDs)
	{
		setColourForNodes(rootRenderer, hilight, nodeIDs);
	}

	/**
	 * Walks over the renderer tree and sets hilighting on the required nodes.
	 * 
	 * @param renderer the current renderer.
	 * @param hilight the colour to hilight each node. <code>null</code> for
	 *            transparent.
	 * @param nodeIDs the IDs of the nodes to hilight.
	 */
	private void setColourForNodes(Renderer renderer, Color hilight, Set<String> nodeIDs)
	{
		// Check if it is a node that needs hilighting
		if( nodeIDs.contains(renderer.getNodeID()) )
		{
			renderer.setHilight(hilight);
		}

		// Recurse on children
		final int childCount = renderer.getChildCount();
		for( int i = 0; i < childCount; i++ )
		{
			setColourForNodes(renderer.getChild(i), hilight, nodeIDs);
		}
	}

	public void addMessageToNode(String nodeID, String message)
	{
		addMessageToNode(rootRenderer, nodeID, message);
	}

	private boolean addMessageToNode(Renderer renderer, String nodeID, String message)
	{
		if( nodeID.equals(renderer.getNodeID()) )
		{
			renderer.setMessage(message);
			return true;
		}
		else
		{
			final int childCount = renderer.getChildCount();
			for( int i = 0; i < childCount; i++ )
			{
				if( addMessageToNode(renderer.getChild(i), nodeID, message) )
				{
					return true;
				}
			}
			return false;
		}
	}

	public void addColourLegend(Color colour, String legendText)
	{
		rootRenderer.addColourLegend(colour, legendText);
	}

	/**
	 * Writes the graphic output to the given output stream in PNG format.
	 */
	public void writeToPng(OutputStream output) throws IOException
	{
		BufferedImage image = null;
		Graphics2D g = null;

		if( cachedSize == null )
		{
			// Create a temporary graphics to work out stuff.
			image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
			try
			{
				g = image.createGraphics();
				getSize(g);
			}
			finally
			{
				if( g != null )
				{
					g.dispose();
				}
			}
		}

		// Now we actually create the image
		int fullWidth = cachedSize.width + PADDING * 2;
		int fullHeight = cachedSize.height + PADDING * 2;
		image = new BufferedImage(fullWidth, fullHeight, BufferedImage.TYPE_3BYTE_BGR);
		g = null;
		try
		{
			g = image.createGraphics();
			g.setColor(Color.WHITE);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.fillRect(0, 0, fullWidth, fullHeight);
			paint(g);
		}
		finally
		{
			if( g != null )
			{
				g.dispose();
			}
		}

		// Write to output stream
		ImageIO.write(image, "png", output); //$NON-NLS-1$
	}
}
