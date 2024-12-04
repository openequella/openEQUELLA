package com.dytech.edge.importexport;

import com.dytech.edge.importexport.icons.Icons;
import com.dytech.gui.ComponentHelper;
import com.dytech.gui.JImage;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.AdvancedSwingWorker;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("nls")
public class WizardFrame extends JFrame implements ActionListener {
  protected JImage header;
  protected Footer footer;
  protected JPanel content;
  protected JButton next;
  protected JButton back;

  protected List<WizardPage> pages;
  protected int pageIndex;

  private String title;
  protected boolean export;

  public WizardFrame(List<WizardPage> pages, boolean export) {
    this.export = export;
    this.title = (export ? "Export Utility " : "Import Utility ");
    Properties properties = new Properties();
    try (InputStream propStream = getClass().getResourceAsStream("/version.properties")) {
      if (propStream != null) {
        properties.load(propStream);
      }
    } catch (IOException e) {
      // do nothing - just use the plain title
    }
    this.title += (String) properties.get("version.display");
    setup();

    this.pages = pages;
    for (WizardPage page : pages) {
      page.setFrame(this);
    }
    pageIndex = 0;

    content.add(pages.get(0));
  }

  private void setup() {
    header = Icons.getHeader(export);
    footer = new Footer();

    content = new JPanel(new GridLayout(1, 1));

    next = new JButton();
    back = new JButton("< Back");

    next.addActionListener(this);
    back.addActionListener(this);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(back);
    buttons.add(next);

    buttons.setPreferredSize(buttons.getPreferredSize());

    final int width = header.getPreferredSize().width;
    final int height1 = header.getPreferredSize().height;
    final int height2 = buttons.getPreferredSize().height;
    final int height3 = footer.getPreferredSize().height;

    final int[] rows = new int[] {height1, 300, height2, height3};
    final int[] cols = new int[] {width};

    JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
    all.add(header, new Rectangle(0, 0, 1, 1));
    all.add(content, new Rectangle(0, 1, 1, 1));
    all.add(buttons, new Rectangle(0, 2, 1, 1));
    all.add(footer, new Rectangle(0, 3, 1, 1));

    getContentPane().add(all);

    pack();
    setResizable(false);
    setTitle(title);
    ComponentHelper.centreOnScreen(this);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    updateButtons();
  }

  protected void updatePanel() {
    content.removeAll();
    content.add(pages.get(pageIndex));
    content.updateUI();
  }

  protected void updateButtons() {
    back.setEnabled(pageIndex > 0);
    next.setEnabled(true);

    if (pages != null && pageIndex == pages.size() - 1) {
      next.setText("Exit");
    } else {
      next.setText("Next >");
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    footer.setWorking(true);
    next.setEnabled(false);
    back.setEnabled(false);

    if (e.getSource() == next) {
      goNext();
    } else if (e.getSource() == back) {
      new BackWorker().start();
    }
  }

  public void goNext() {
    new NextWorker().start();
  }

  protected final class NextWorker extends AdvancedSwingWorker<Object> {
    @Override
    public Object construct() {
      if (pages.get(pageIndex).onNext()) {
        pageIndex++;
        if (pageIndex < pages.size()) {
          updatePanel();
          pages.get(pageIndex).onShow();
        } else {
          System.exit(0);
        }
      }
      return null;
    }

    @Override
    public void finished() {
      updateButtons();
      footer.setWorking(false);
    }
  }

  protected final class BackWorker extends AdvancedSwingWorker<Object> {
    @Override
    public Object construct() {
      if (pages.get(pageIndex).onBack()) {
        pageIndex--;
        updatePanel();
        pages.get(pageIndex).onShow();
      }
      return null;
    }

    @Override
    public void finished() {
      updateButtons();
      footer.setWorking(false);
    }
  }
}
