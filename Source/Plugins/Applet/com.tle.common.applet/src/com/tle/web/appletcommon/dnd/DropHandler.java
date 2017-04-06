package com.tle.web.appletcommon.dnd;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

public interface DropHandler
{
	int getDropHandlerPriority();

	boolean supportsDrop(DropTargetDragEvent e);

	boolean handleDrop(DropTargetDropEvent e) throws Exception;
}