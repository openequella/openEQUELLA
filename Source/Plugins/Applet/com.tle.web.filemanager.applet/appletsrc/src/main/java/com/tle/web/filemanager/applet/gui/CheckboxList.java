package com.tle.web.filemanager.applet.gui;

import com.dytech.gui.VerticalFlowLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CheckboxList<T> extends JPanel {
  private static final long serialVersionUID = 1L;
  private final Map<AbstractButton, T> mapping = new HashMap<AbstractButton, T>();

  public CheckboxList() {
    setBackground(Color.WHITE);
    setLayout(new VerticalFlowLayout());
  }

  public void setElements(Collection<T> elems, boolean selectedByDefault) {
    mapping.clear();
    removeAll();

    for (T elem : elems) {
      JCheckBox checkBox = new JCheckBox(elementToString(elem), selectedByDefault);
      checkBox.setOpaque(false);

      mapping.put(checkBox, elem);
      add(checkBox);
    }
  }

  public List<T> getSelectedElements() {
    List<T> results = new ArrayList<T>();
    for (Map.Entry<AbstractButton, T> entry : mapping.entrySet()) {
      if (entry.getKey().isSelected()) {
        results.add(entry.getValue());
      }
    }
    return results;
  }

  public String elementToString(T element) {
    return element.toString();
  }
}
