package com.tle.webtests.pageobject.adminconsole;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.PageContext;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.MouseButton;
import org.fest.swing.core.matcher.DialogMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.core.matcher.JLabelMatcher;
import org.fest.swing.core.matcher.JTextComponentMatcher;
import org.fest.swing.data.TableCell;
import org.fest.swing.data.TableCellByColumnId;
import org.fest.swing.driver.BasicJListCellReader;
import org.fest.swing.driver.JComboBoxDriver;
import org.fest.swing.driver.JTableDriver;
import org.fest.swing.driver.JTextComponentDriver;
import org.fest.swing.exception.ActionFailedException;
import org.fest.swing.exception.ComponentLookupException;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.JTableFixture;
import org.fest.swing.fixture.WindowFixture;
import org.fest.swing.timing.Condition;
import org.fest.swing.timing.Pause;

@SuppressWarnings("unchecked")
public abstract class AbstractAppletWindow<A extends AbstractAppletWindow<A>> {
  protected PageContext context;
  protected WindowFixture<?> windowHolder;

  public AbstractAppletWindow(PageContext context, WindowFixture<?> windowHolder) {
    this.context = context;
    this.windowHolder = windowHolder;
  }

  public AbstractAppletWindow(PageContext context) {
    this.context = context;
  }

  protected A clickList(String text) {
    windowHolder.list(wait(new JListMatcher(text))).selectItem(text);
    return (A) this;
  }

  protected A clickButton(String text) {
    windowHolder.button(wait(JButtonMatcher.withText(text).andShowing())).click();
    return (A) this;
  }

  protected A clickTableCell(String text) {
    JTableFixture table = windowHolder.table(wait(new JTableMatcher(text)));
    TableCell cell = table.cell(text);
    table.click(cell, MouseButton.LEFT_BUTTON);
    return (A) this;
  }

  protected A clickTableCell(Object columnId, int row) {
    TableCellByColumnId cell = TableCellByColumnId.row(row).columnId(columnId);
    JTableFixture table = windowHolder.table(wait(JTable.class));
    table.cell(cell).click();
    return (A) this;
  }

  protected A clickCheckBox(String text) {
    windowHolder.checkBox(new JCheckBoxMatcher(text)).click();
    return (A) this;
  }

  protected A clickButton(String text, int index) {
    List<JButton> buttons =
        Lists.newArrayList(
            windowHolder
                .robot
                .finder()
                .findAll(windowHolder.component(), JButtonMatcher.withText(text).andShowing()));
    windowHolder.robot.click(buttons.get(index));
    return (A) this;
  }

  protected A clickTab(String text) {
    windowHolder.tabbedPane(wait(JTabbedPane.class)).selectTab(text);
    return (A) this;
  }

  protected A clickTreePath(String path) {
    windowHolder.tree(wait(JTree.class)).selectPath(path);
    return (A) this;
  }

  protected A clickComboBox(String item) {
    windowHolder.comboBox(wait(new JComboBoxMatcher(item))).selectItem(item);
    return (A) this;
  }

  protected A setText(int index, String text) {
    List<JTextComponent> edits =
        Lists.newArrayList(
            windowHolder
                .robot
                .finder()
                .findAll(windowHolder.component(), JTextComponentMatcher.any().andShowing()));
    new JTextComponentDriver(windowHolder.robot).setText(edits.get(index), text);
    return (A) this;
  }

  protected A waitForWindow(String partialTitle) {
    WindowFinder.findDialog(
            DialogMatcher.withTitle(Pattern.compile(".*" + partialTitle + ".*")).andShowing())
        .withTimeout(30000)
        .using(windowHolder.robot);
    return (A) this;
  }

  protected <T extends Component> GenericMatcher<T> matcher(Class<T> clazz) {
    return new GenericMatcher<T>(clazz);
  }

  protected <T extends Component> GenericMatcher<T> wait(Class<T> clazz) {
    return wait(matcher(clazz));
  }

  protected <T extends GenericTypeMatcher<?>> T wait(final T cm) {
    Pause.pause(
        new Condition("Codition: " + cm.toString()) {
          @Override
          public boolean test() {
            try {
              Component component = windowHolder.robot.finder().find(cm);
              return component.isShowing();
            } catch (ComponentLookupException e) {
              return false;
            }
          }
        });
    return (T) cm;
  }

  protected void waitForDownload() {
    Pause.pause(
        new Condition("Codition: Waiting for download") {
          @Override
          public boolean test() {
            try {
              Component component =
                  windowHolder
                      .robot
                      .finder()
                      .find(JLabelMatcher.withText("Downloading...").andShowing());
              return !component.isShowing();
            } catch (ComponentLookupException e) {
              return true;
            }
          }
        });
  }

  protected class GenericMatcher<T extends Component> extends GenericTypeMatcher<T> {

    public GenericMatcher(Class<T> supportedType) {
      super(supportedType, true);
    }

    @Override
    protected boolean isMatching(T component) {
      return component.isShowing();
    }
  }

  protected class JListMatcher extends GenericTypeMatcher<JList> {
    private final String item;

    public JListMatcher(String item) {
      super(JList.class, true);
      this.item = item;
    }

    @Override
    protected boolean isMatching(JList component) {
      BasicJListCellReader cellReader = new BasicJListCellReader();

      int size = component.getModel().getSize();
      for (int i = 0; i < size; i++) {
        String elementAt = cellReader.valueAt(component, i);
        if (elementAt.equals(item)) {
          return true;
        }
      }
      return false;
    }
  }

  protected class JComboBoxMatcher extends GenericTypeMatcher<JComboBox> {
    private final String item;

    public JComboBoxMatcher(String item) {
      super(JComboBox.class, true);
      this.item = item;
    }

    @Override
    protected boolean isMatching(JComboBox component) {
      JComboBoxDriver driver = new JComboBoxDriver(windowHolder.robot);
      return Arrays.asList(driver.contentsOf(component)).contains(item);
    }
  }

  protected class JCheckBoxMatcher extends GenericTypeMatcher<JCheckBox> {
    private final String title;

    public JCheckBoxMatcher(String title) {
      super(JCheckBox.class, true);
      this.title = title;
    }

    @Override
    protected boolean isMatching(JCheckBox component) {
      return title.equals(component.getText());
    }
  }

  protected class JTableMatcher extends GenericTypeMatcher<JTable> {
    private final String cell;

    public JTableMatcher(String cell) {
      super(JTable.class, true);
      this.cell = cell;
    }

    @Override
    protected boolean isMatching(JTable component) {
      JTableDriver driver = new JTableDriver(windowHolder.robot);
      try {
        driver.cell(component, cell);
        return true;
      } catch (ActionFailedException e) {
        return false;
      }
    }
  }
}
