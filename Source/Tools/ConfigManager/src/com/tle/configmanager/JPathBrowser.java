package com.tle.configmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

// Author: Andrew Gibb

@SuppressWarnings({"serial", "nls"})
public class JPathBrowser extends JPanel implements ActionListener {
  private static File lastSelectedPath;

  private final JTextField path;
  private final JButton browse;
  private final JFileChooser fchBrowse;

  public JPathBrowser(int type) {
    this.setLayout(new MigLayout("fill, insets 0"));

    path = new JTextField();
    browse = new JButton("Browse");
    browse.addActionListener(this);

    fchBrowse = new JFileChooser();

    if (type == JFileChooser.DIRECTORIES_ONLY) {
      fchBrowse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    } else if (type == JFileChooser.FILES_AND_DIRECTORIES) {
      fchBrowse.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    this.add(path, "pushx, grow");
    this.add(browse);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == browse) {
      fchBrowse.setCurrentDirectory(lastSelectedPath);
      if (fchBrowse.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        lastSelectedPath = fchBrowse.getSelectedFile();
        String p = fchBrowse.getSelectedFile().toString();
        p = p.replaceAll("\\\\", "/");
        path.setText(p);
      }
    }
  }

  public String getPath() {
    return path.getText();
  }

  public void setPath(String path) {
    this.path.setText(path);
  }

  @Override
  public void setEnabled(boolean b) {
    path.setEnabled(b);
    browse.setEnabled(b);
  }

  @Override
  public boolean isEnabled() {
    return path.isEnabled() && browse.isEnabled();
  }
}
