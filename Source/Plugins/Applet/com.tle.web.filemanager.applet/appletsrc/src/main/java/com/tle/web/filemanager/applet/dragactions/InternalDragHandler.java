package com.tle.web.filemanager.applet.dragactions;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.web.appletcommon.dnd.DropHandler;
import com.tle.web.filemanager.applet.FileListPanel;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import javax.swing.JPopupMenu;

public class InternalDragHandler implements DropHandler {
  public final FileListPanel fileList;
  public final InternalDragAction[] actions;

  public InternalDragHandler(FileListPanel fileList, InternalDragAction... actions) {
    this.fileList = fileList;
    this.actions = actions;
  }

  @Override
  public int getDropHandlerPriority() {
    return 100;
  }

  @Override
  public boolean supportsDrop(DropTargetDragEvent e) {
    return e.isDataFlavorSupported(FileInfoTransferable.fileInfoFlavor);
  }

  @Override
  public boolean handleDrop(DropTargetDropEvent e) throws Exception {
    final FileInfo draggedFile =
        (FileInfo) e.getTransferable().getTransferData(FileInfoTransferable.fileInfoFlavor);

    final FileInfo targetDir = fileList.getFileUnderMouseCursor();
    if (targetDir == null || !targetDir.isDirectory()) {
      return false;
    }

    JPopupMenu menu = new JPopupMenu();
    for (final InternalDragAction action : actions) {
      menu.add(
          new TLEAction(action.getDisplayName(), action.getIcon()) {
            @Override
            public void actionPerformed(ActionEvent e) {
              action.doAction(draggedFile, targetDir);
            }
          });
    }
    fileList.showPopup(menu, targetDir);

    return true;
  }

  public interface InternalDragAction {
    String getDisplayName();

    String getIcon();

    void doAction(FileInfo draggedFile, FileInfo targetDir);
  }
}
