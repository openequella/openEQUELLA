package com.tle.web.filemanager.applet;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.web.appletcommon.dnd.DropHandler;
import com.tle.web.filemanager.applet.FileManager.Parameters;
import com.tle.web.filemanager.applet.actions.DeleteAction;
import com.tle.web.filemanager.applet.actions.DownloadAction;
import com.tle.web.filemanager.applet.actions.EditAction;
import com.tle.web.filemanager.applet.actions.ExtractArchiveAction;
import com.tle.web.filemanager.applet.actions.MarkAsResourceAction;
import com.tle.web.filemanager.applet.actions.NewFolderAction;
import com.tle.web.filemanager.applet.actions.OpenAction;
import com.tle.web.filemanager.applet.actions.PreJava6OpenAction;
import com.tle.web.filemanager.applet.actions.RenameAction;
import com.tle.web.filemanager.applet.actions.UploadAction;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.applet.dragactions.CopyDragAction;
import com.tle.web.filemanager.applet.dragactions.InternalDragHandler;
import com.tle.web.filemanager.applet.dragactions.MoveDragAction;
import com.tle.web.filemanager.applet.gui.ExpandLayout;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MainPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private List<TLEAction> actions;

  private final JToolBar toolBar;
  final Backend backend;
  final FileListPanel fileList;

  public MainPanel(Backend backend, Parameters params) {
    this.backend = backend;
    fileList = new FileListPanel(backend);
    fileList.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

    toolBar = new JToolBar();
    toolBar.setLayout(
        new ExpandLayout() {
          @Override
          public Component createComponent(Action a) {
            return new JButton(a);
          }
        });
    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    setupActions(params);

    setLayout(new BorderLayout());
    add(toolBar, BorderLayout.NORTH);
    add(fileList, BorderLayout.CENTER);

    // TODO: Temporary
    FileInfo root = new FileInfo();
    root.setDirectory(true);
    fileList.setCwd(root);
  }

  private void setupActions(Parameters params) {
    TLEAction doubleClickAction = null;

    actions = new ArrayList<TLEAction>();

    String versionString = System.getProperty("java.version");
    versionString = versionString.substring(0, 3);
    float version = Float.parseFloat(versionString);
    boolean isPostJava5 = version > 1.5;

    if (isPostJava5) {
      doubleClickAction = new OpenAction(backend, fileList);
      actions.add(doubleClickAction);

      if (Desktop.isDesktopSupported()
          && Desktop.getDesktop().isSupported(java.awt.Desktop.Action.EDIT)) {
        doubleClickAction = new EditAction(backend, fileList);
        actions.add(doubleClickAction);
      }
    } else {
      doubleClickAction = new PreJava6OpenAction(fileList);
      actions.add(doubleClickAction);
    }

    actions.add(new RenameAction(backend, fileList));
    actions.add(new DeleteAction(backend, fileList));
    actions.add(new NewFolderAction(backend, fileList));
    actions.add(new MarkAsResourceAction(backend, fileList, params.isAutoMarkAsResource()));
    actions.add(new DownloadAction(backend, fileList, isPostJava5));
    actions.add(new UploadAction(backend, fileList, params.isAutoMarkAsResource()));
    actions.add(new ExtractArchiveAction(backend, fileList));

    for (TLEAction action : actions) {
      // If we add buttons rather than just actions, the button text will
      // show.
      toolBar.add(new JButton(action));

      KeyStroke keyStroke = action.invokeForWindowKeyStroke();
      if (keyStroke != null) {
        String actionName = action.getClass().getName() + System.identityHashCode(action);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionName);
        getActionMap().put(actionName, action);
      }

      if (action instanceof DropHandler) {
        fileList.registerDropHandler((DropHandler) action);
      }
    }

    fileList.addPopupActions(actions);
    fileList.addDoubleClickAction(doubleClickAction);
    fileList.addListSelectionListener(
        new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent e) {
            updateActions();
          }
        });

    // Register internal drag/drop handlers
    fileList.registerDropHandler(
        new InternalDragHandler(
            fileList, new CopyDragAction(backend), new MoveDragAction(backend)));

    updateActions();
  }

  private void updateActions() {
    for (TLEAction action : actions) {
      action.update();
    }
  }
}
