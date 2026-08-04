/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.gui.i18n;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.Changeable;
import com.dytech.gui.JLinkButton;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.LocaleUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import net.miginfocom.swing.MigLayout;

public class I18nTextField extends JComponent implements Changeable {
  private static final long serialVersionUID = 1L;
  private final NavigableMap<Locale, String> textMap;
  private final NavigableSet<Locale> editableLocales;
  private final Set<Locale> defaultLocales;

  private Locale currentLocale;
  private JTextComponent currentText;
  private JButton popupButton;

  private boolean changesDetected;

  public I18nTextField(Set<Locale> defaultLocales) {
    this.defaultLocales = defaultLocales;

    Comparator<Locale> localeComparator =
        new NumberStringComparator<Locale>() {
          private static final long serialVersionUID = 1L;

          @Override
          public String convertToString(Locale locale) {
            return locale.getDisplayName();
          }
        };

    this.editableLocales = new TreeSet<Locale>(localeComparator);
    this.editableLocales.addAll(defaultLocales);

    this.textMap = new TreeMap<Locale, String>(localeComparator);

    initialise();
  }

  private void initialise() {
    currentText = getTextComponent();
    popupButton = new JButton();
    popupButton.addActionListener(new PopupListener());
    popupButton.setIcon(new ArrowIcon(SwingConstants.SOUTH));
    popupButton.setHorizontalTextPosition(SwingConstants.LEFT);

    initialiseLayout("hidemode 3,insets 0,fill", "growx", true); // $NON-NLS-1$ //$NON-NLS-2$

    switchLocale(null);
  }

  protected void initialiseLayout(
      String layoutConstraints, String cellConstraint, boolean addTextComponent) {
    setLayout(new MigLayout(layoutConstraints, "[grow,fill]0[]")); // $NON-NLS-1$
    if (addTextComponent) {
      add(prepareTextComponent(currentText), cellConstraint);
    }
    add(popupButton, "top"); // $NON-NLS-1$
  }

  protected JTextComponent getTextComponent() {
    return new JTextField();
  }

  protected Component prepareTextComponent(JTextComponent component) {
    return component;
  }

  private void switchLocale(Locale locale) {
    currentLocale = locale != null ? locale : CurrentLocale.getLocale();

    if (!editableLocales.contains(currentLocale)) {
      // look for the best match, otherwise first
      currentLocale = LocaleUtils.getClosestLocale(editableLocales, currentLocale);

      // not sure it can be
      if (currentLocale == null) {
        if (!editableLocales.isEmpty()) {
          currentLocale = editableLocales.first();
        } else if (!textMap.isEmpty()) {
          currentLocale = textMap.firstKey();
        } else {
          // Absolute worst case fallback
          currentLocale = Locale.getDefault();
        }
      }
    }

    loadCurrent();
  }

  private void saveCurrent() {
    if (currentLocale != null) {
      setText(currentLocale, currentText);
    }
  }

  private void loadCurrent() {
    currentText.setText(textMap.get(currentLocale));
    popupButton.setText(currentLocale.getDisplayName());

    popupButton.setVisible(editableLocales.size() > 1);

    // Fire event
    ChangeEvent event = new ChangeEvent(this);
    for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
      l.stateChanged(event);
    }
  }

  private void setText(Locale locale, JTextComponent textField) {
    final String text = textField.getText();
    final boolean isEmpty = text.length() == 0;

    String replaced = isEmpty ? textMap.remove(locale) : textMap.put(locale, text);
    if (replaced == null) {
      replaced = ""; // $NON-NLS-1$
    }

    if (!text.equals(replaced)) {
      changesDetected = true;
    }
  }

  protected void onPopupClose() {
    // Nothing to do here
  }

  public void showPopup() {
    popupButton.doClick();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    currentText.setEnabled(enabled);
    popupButton.setEnabled(enabled);
  }

  @Override
  public synchronized void addKeyListener(KeyListener listener) {
    currentText.addKeyListener(listener);
  }

  @Override
  public synchronized void removeKeyListener(KeyListener listener) {
    currentText.removeKeyListener(listener);
  }

  public void load(LanguageBundle bundle) {
    textMap.clear();
    editableLocales.clear();
    editableLocales.addAll(defaultLocales);

    final Locale userLocale = CurrentLocale.getLocale();
    Locale nonEmptyLocale = null;
    if (bundle != null) {
      Map<String, LanguageString> strings = bundle.getStrings();
      if (!Check.isEmpty(strings)) {
        for (LanguageString string : strings.values()) {
          String t = string.getText();
          if (!Check.isEmpty(t)) {
            Locale l = LocaleUtils.parseLocale(string.getLocale());
            if (nonEmptyLocale == null || l.equals(userLocale)) {
              nonEmptyLocale = l;
            }

            editableLocales.add(l);
            textMap.put(l, t);
          }
        }
      }
    }

    switchLocale(nonEmptyLocale);
    clearChanges();
  }

  public LanguageBundle save() {
    saveCurrent();

    LanguageBundle bundle = new LanguageBundle();
    for (Entry<Locale, String> entry : textMap.entrySet()) {
      LangUtils.createLanguageString(bundle, entry.getKey(), entry.getValue());
    }

    if (bundle.isEmpty()) {
      bundle = null;
    }

    clearChanges();

    return bundle;
  }

  public boolean isCompletelyEmpty() {
    saveCurrent();
    for (String value : textMap.values()) {
      if (!Check.isEmpty(value)) {
        return false;
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.Changeable#clearChanges()
   */
  @Override
  public void clearChanges() {
    changesDetected = false;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.Changeable#hasDetectedChanges()
   */
  @Override
  public boolean hasDetectedChanges() {
    saveCurrent();
    return changesDetected;
  }

  protected class PopupListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      saveCurrent();

      final JPopupMenu menu = new JPopupMenu();
      final PopupPanel popupPanel = new PopupPanel(menu);
      menu.add(popupPanel);
      menu.show(I18nTextField.this, 5, 5);
    }
  }

  private class PopupPanel extends JPanel implements PopupMenuListener, ActionListener {
    private static final long serialVersionUID = 1L;
    private final JPopupMenu popup;
    private final Map<Locale, JTextComponent> mapping;
    private final JLinkButton closeButton;

    public PopupPanel(JPopupMenu popup) {
      this.popup = popup;
      this.mapping = new HashMap<Locale, JTextComponent>();

      setLayout(
          new MigLayout("wrap,fill", "[grow]r[]", "")); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      for (Locale locale : editableLocales) {
        String actionCommand = locale.toString();
        JTextComponent field = getTextComponent();

        JButton button = new JButton(locale.getDisplayName());
        button.setActionCommand(actionCommand);
        button.addActionListener(this);

        field.setText(textMap.get(locale));
        mapping.put(locale, field);

        add(prepareTextComponent(field), "growx, sizegroup f, hmax 100px"); // $NON-NLS-1$
        add(button, "sizegroup b"); // $NON-NLS-1$
      }

      closeButton = new JLinkButton(CurrentLocale.get("prompts.close")); // $NON-NLS-1$
      closeButton.addActionListener(this);
      closeButton.setActionCommand(""); // $NON-NLS-1$
      closeButton.setIcon(new ArrowIcon(SwingConstants.NORTH));
      closeButton.setHorizontalTextPosition(SwingConstants.LEFT);
      add(closeButton, "span 2, align right, shrink"); // $NON-NLS-1$

      setBorder(BorderFactory.createLineBorder(Color.BLACK));

      final int width = Math.max(400, I18nTextField.this.getSize().width);
      setPreferredSize(new Dimension(width, getPreferredSize().height));

      popup.addPopupMenuListener(this);
    }

    private void save(Locale localeChange) {
      for (Entry<Locale, JTextComponent> entry : mapping.entrySet()) {
        setText(entry.getKey(), entry.getValue());
      }

      if (localeChange == null) {
        loadCurrent();
      } else {
        switchLocale(localeChange);
      }
    }

    /*
     * (non-Javadoc)
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
     * )
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      String command = ((JButton) e.getSource()).getActionCommand();
      save(Check.isEmpty(command) ? null : LocaleUtils.parseLocale(command));
      popup.setVisible(false);
      onPopupClose();
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing
     * .event.PopupMenuEvent)
     */
    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      save(null);
      onPopupClose();
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(
     * javax.swing.event.PopupMenuEvent)
     */
    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      // Don't care
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax
     * .swing.event.PopupMenuEvent)
     */
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // Don't care
    }
  }
}
