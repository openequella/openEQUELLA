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

package com.dytech.edge.admin.wizard.editor;

import com.dytech.edge.admin.script.ScriptEditor;
import com.dytech.edge.admin.script.ScriptModel;
import com.dytech.edge.admin.script.options.ScriptOptions;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.walkers.IterateControls;
import com.dytech.gui.LineBorder;
import com.dytech.gui.TableLayout;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.controls.scripting.BasicModel;
import com.tle.admin.gui.EditorInterface;
import com.tle.admin.gui.common.JChangeDetectorPanel;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.AbstractPluginService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("nls")
public abstract class Editor extends JChangeDetectorPanel {
  private static final String SCRIPT_ICON = "/icons/greenbullet.gif";
  private static final String ERROR_ICON = "/icons/error.gif";

  private static final int SCRIPTING_INDEX = 2;

  private final Control control;
  private final int wizardType;
  private final SchemaModel schema;
  private ScriptOptions scriptOptions;
  private ClientService clientService;
  private PluginServiceImpl pluginService;
  private EditorInterface entityEditor;

  private TableLayout headerLayout;
  private JButton scripting;
  private JLabel errorMessage;
  private ImageIcon errorIcon;

  private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

  protected String getKey(String key) {
    return KEY_PFX + key;
  }

  protected String getString(String key) {
    return CurrentLocale.get(getKey(key));
  }

  /** Constructs a new Editor. */
  public Editor(Control control, int wizardType, SchemaModel schema) {
    this.control = control;
    this.wizardType = wizardType;
    this.schema = schema;
    control.setEditor(this);

    // Add gaps specifically around sections
    setLayout(new MigLayout("wrap 1, insets 0, gap 0 0", "[grow, fill]"));
    add(createHeader());

    setShowScripting(control.isScriptable());
  }

  public void init() {
    // Nothing by default
  }

  /** Adds a new section to the editor. */
  protected void addSection(JComponent section) {
    addSection(section, true);
  }

  protected void addSection(JComponent section, boolean followWithSeparator) {
    // MigLayout puts in a margin, TableLayout doesn't!
    String cons = section.getLayout() instanceof MigLayout ? "gap 0 0 0 0" : "gap 5 5 5 5";

    add(section, cons);
    if (followWithSeparator) {
      add(new JSeparator());
    }
  }

  protected void removeSection(JComponent section) {
    for (int i = 0, count = getComponentCount(); i < count; i++) {
      Component c = getComponent(i);
      if (c.equals(section)) {
        remove(i);

        // Remove next component too if it's a JSeparator
        if (i < count && getComponent(i) instanceof JSeparator) {
          remove(i);
        }
      }
    }
  }

  /** Creates the top header section of the editors. */
  private JComponent createHeader() {
    JLabel titleLabel = new JLabel("<html><h3>" + control.getDefinition().getName());

    errorMessage = new JLabel(" ");

    scripting =
        createHeaderButton(
            CurrentLocale.get("com.dytech.edge.admin.wizard.editor.editor.scripting"), SCRIPT_ICON);

    final int[] rows = {
      titleLabel.getPreferredSize().height, errorMessage.getPreferredSize().height, 5,
    };
    final int[] cols = {
      10, TableLayout.FILL, 0, 0, 0,
    };
    headerLayout = new TableLayout(rows, cols, 0, 5);

    JPanel header = new JPanel(headerLayout);
    header.setBackground(Color.WHITE);
    header.setBorder(new LineBorder(Color.BLACK, 0, 0, 1, 0));

    header.add(titleLabel, new Rectangle(1, 0, 1, 1));
    header.add(scripting, new Rectangle(SCRIPTING_INDEX, 0, 1, 1));

    header.add(errorMessage, new Rectangle(1, 1, 4, 1));

    errorIcon = new ImageIcon(Editor.class.getResource(ERROR_ICON));
    clearError();

    setShowScripting(false);

    return header;
  }

  /**
   * @return Returns the control.
   */
  public Control getControl() {
    return control;
  }

  /**
   * @return Returns the schema.
   */
  public SchemaModel getSchema() {
    return schema;
  }

  /** Sets an error message to display. */
  private void setError(String message) {
    if (message == null || message.length() == 0) {
      clearError();
    } else {
      errorMessage.setText(message);
      errorMessage.setIcon(errorIcon);
      updateUI();
    }
  }

  /** Removes any existing error message. */
  private void clearError() {
    errorMessage.setText("");
    errorMessage.setIcon(null);
    updateUI();
  }

  /** Invoke this to launch the scripting editor. */
  private void doScripting() {
    ScriptModel model = createScriptingModel();

    ScriptEditor scriptEditor = new ScriptEditor(model);
    scriptEditor.importScript(control.getScript());
    scriptEditor.showEditor(this);

    if (scriptEditor.scriptWasSaved()) {
      control.setScript(scriptEditor.getScript());
    }
  }

  protected ScriptModel createScriptingModel() {
    IterateControls walker = new IterateControls();
    walker.execute(WizardHelper.getRoot(control));
    return new BasicModel(schema, scriptOptions, walker.getControls());
  }

  /** Saves the editors values back to the control. */
  public void saveToControl() {
    saveControl();

    switch (getWizardType()) {
      case WizardHelper.WIZARD_TYPE_CONTRIBUTION:
        saveForContributionWizard();
        break;

      case WizardHelper.WIZARD_TYPE_POWERSEARCH:
        saveForPowerSearch();
        break;

      default:
        break; // Shouldn't be anything else
    }
  }

  /** Informs the control to save its value back to the Control model. */
  protected abstract void saveControl();

  /** Informs the control to load its value from the Control model. */
  protected abstract void loadControl();

  /**
   * @return Returns the wizardType.
   */
  public int getWizardType() {
    return wizardType;
  }

  /** Enables the scripting button. */
  protected void setShowScripting(boolean b) {
    setShowComponent(scripting, SCRIPTING_INDEX, b);
  }

  /** Shows or hides a button component of the header. */
  private void setShowComponent(JComponent component, int layoutIndex, boolean visible) {
    int width = 0;
    if (visible) {
      width = component.getPreferredSize().width;
    }
    headerLayout.setColumnSize(layoutIndex, width);
  }

  /** Creates a new button for displaying on the left hand side. */
  private JButton createHeaderButton(String text, String iconPath) {
    Icon icon = new ImageIcon(Editor.class.getResource(iconPath));

    JButton button = new JButton(text, icon);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.addActionListener(buttonListener);
    return button;
  }

  /** Loads the control by multiplexing out to one of the other loadFor* methods. */
  public void loadFromControl() {
    // Set the error message
    setError(control.getErrorMessage());

    // Load the control.
    loadControl();

    switch (getWizardType()) {
      case WizardHelper.WIZARD_TYPE_CONTRIBUTION:
        loadForContributionWizard();
        break;

      case WizardHelper.WIZARD_TYPE_POWERSEARCH:
        loadForPowerSearch();
        break;

      default:
        break; // Shouldn't be anything else
    }
  }

  protected void loadForContributionWizard() {
    // Nothing to do here by default.
  }

  protected void loadForPowerSearch() {
    // Nothing to do here by default.
  }

  protected void saveForContributionWizard() {
    // Nothing to do here by default.
  }

  protected void saveForPowerSearch() {
    // Nothing to do here by default.
  }

  public void setScriptOptions(ScriptOptions scriptOptions) {
    this.scriptOptions = scriptOptions;
  }

  public ClientService getClientService() {
    return clientService;
  }

  public void setClientService(ClientService clientService) {
    this.clientService = clientService;
  }

  public PluginServiceImpl getPluginService() {
    return pluginService;
  }

  public void setPluginService(PluginServiceImpl pluginService) {
    this.pluginService = pluginService;
  }

  public void setEntityEditor(EditorInterface entityEditor) {
    this.entityEditor = entityEditor;
  }

  protected EditorInterface getEntityEditor() {
    return entityEditor;
  }

  private final ActionListener buttonListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (e.getSource() == scripting) {
            doScripting();
          }
        }
      };
}
