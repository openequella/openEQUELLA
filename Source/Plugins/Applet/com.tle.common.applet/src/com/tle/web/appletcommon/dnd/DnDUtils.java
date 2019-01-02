/*
 * Copyright 2019 Apereo
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

package com.tle.web.appletcommon.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

public final class DnDUtils
{
	private static final String ZERO_CHAR_STRING = "" + (char) 0; //$NON-NLS-1$

	public static void registerDropHandler(final JComponent component, final Collection<DropHandler> dropHandlers,
		final HoverHandler hoverHandler)
	{
		new DropTarget(component, DnDConstants.ACTION_COPY, new DropTargetListener()
		{
			private DropHandler dropHandler;

			private boolean actionSupported(int action)
			{
				return (action & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
			}

			private DropHandler getSupportedDropHandler(DropTargetDragEvent e)
			{
				for( DropHandler handler : dropHandlers )
				{
					if( handler.supportsDrop(e) )
					{
						return handler;
					}
				}
				return null;
			}

			@Override
			public void dragEnter(DropTargetDragEvent e)
			{
				dropHandler = getSupportedDropHandler(e);
				dropActionChanged(e);
			}

			@Override
			public void dragOver(DropTargetDragEvent e)
			{
				dropActionChanged(e);
			}

			@Override
			public void dragExit(DropTargetEvent e)
			{
				// Nothing to do here
			}

			@Override
			public void drop(DropTargetDropEvent e)
			{
				int dropAction = e.getDropAction();
				if( dropHandler != null && actionSupported(dropAction) )
				{
					try
					{
						e.acceptDrop(dropAction);
						e.dropComplete(dropHandler.handleDrop(e));
					}
					catch( Exception ex )
					{
						ex.printStackTrace();
						e.dropComplete(false);
					}
				}
				else
				{
					e.rejectDrop();
				}
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent e)
			{
				int dropAction = e.getDropAction();
				if( dropHandler != null && actionSupported(dropAction) )
				{
					if( hoverHandler != null )
					{
						hoverHandler.hovering(e);
					}
					e.acceptDrag(DnDConstants.ACTION_COPY);
				}
				else
				{
					e.rejectDrag();
				}
			}
		});
	}

	public static boolean supportsNativeFileDrop(DropTargetDragEvent e)
	{
		for( DataFlavor flavor : e.getCurrentDataFlavors() )
		{
			if( flavor.equals(DataFlavor.javaFileListFlavor) || flavor.isRepresentationClassReader() )
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static List<File> getDroppedNativeFiles(Transferable t) throws UnsupportedFlavorException, IOException
	{
		// This works for Windows
		if( t.isDataFlavorSupported(DataFlavor.javaFileListFlavor) )
		{
			return (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		}

		// This works for Gnome and KDE
		for( DataFlavor flavor : t.getTransferDataFlavors() )
		{
			if( flavor.isRepresentationClassReader() )
			{
				try( BufferedReader reader = new BufferedReader(flavor.getReaderForText(t)) )
				{
					List<File> list = new ArrayList<File>();

					String line = null;
					while( (line = reader.readLine()) != null )
					{
						try
						{
							if( ZERO_CHAR_STRING.equals(line) )
							{
								continue;
							}

							list.add(new File(new java.net.URI(line)));
						}
						catch( Exception ex )
						{
							ex.printStackTrace();
						}
					}

					return list;
				}
			}
		}

		throw new UnsupportedFlavorException(null);
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private DnDUtils()
	{
		throw new Error();
	}
}
