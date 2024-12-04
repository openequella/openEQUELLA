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

package com.tle.admin.search.searchset;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.JSmartTextField;
import com.dytech.gui.TableLayout;
import com.tle.admin.search.searchset.EntityWhereEditor.ItemDefinitionWhereEditor;
import com.tle.admin.search.searchset.EntityWhereEditor.SchemaWhereEditor;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.plugins.AbstractPluginService;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchSetFilter extends JPanel implements Changeable {
  private final ChangeDetector changeDetector;
  private final JTextField freetextQuery;
  private final SchemaWhereEditor schemasEditor;
  private final ItemDefinitionWhereEditor itemDefsEditor;

  private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

  protected String getString(String key) {
    return CurrentLocale.get(getKey(key));
  }

  protected String getKey(String key) {
    return KEY_PFX + key;
  }

  public SearchSetFilter(final EntityCache cache, final ClientService clientService) {
    final JLabel freetextLabel =
        new JLabel(getString("searchset.searchsetfilter.freetextlabel")); // $NON-NLS-1$
    final JLabel schemasAdditionalLabel =
        new JLabel(getString("searchset.searchsetfilter.searchschemas")); // $NON-NLS-1$
    final JLabel itemDefsAdditionalLabel =
        new JLabel(getString("searchset.searchsetfilter.searchcollections")); // $NON-NLS-1$

    freetextQuery = new JSmartTextField(100);
    schemasEditor = new SchemaWhereEditor(cache, clientService);
    itemDefsEditor = new ItemDefinitionWhereEditor(cache, clientService);

    final int height1 = freetextQuery.getPreferredSize().height;
    final int width1 = 20;

    final int[] rows = {
      height1, height1, height1, TableLayout.FILL, height1, TableLayout.FILL,
    };
    final int[] cols = {
      width1, TableLayout.FILL,
    };

    setLayout(new TableLayout(rows, cols));

    add(freetextLabel, new Rectangle(0, 0, 2, 1));
    add(freetextQuery, new Rectangle(1, 1, 1, 1));

    add(schemasAdditionalLabel, new Rectangle(0, 2, 2, 1));
    add(schemasEditor, new Rectangle(1, 3, 1, 1));

    add(itemDefsAdditionalLabel, new Rectangle(0, 4, 2, 1));
    add(itemDefsEditor, new Rectangle(1, 5, 1, 1));

    changeDetector = new ChangeDetector();
    changeDetector.watch(freetextQuery);
    changeDetector.watch(schemasEditor);
    changeDetector.watch(itemDefsEditor);
  }

  @Override
  public boolean hasDetectedChanges() {
    return changeDetector.hasDetectedChanges();
  }

  @Override
  public void clearChanges() {
    changeDetector.clearChanges();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    freetextQuery.setEnabled(enabled);
    schemasEditor.setEnabled(enabled);
    itemDefsEditor.setEnabled(enabled);
  }

  public void load(SearchSet searchSet) {
    freetextQuery.setText(searchSet.getFreetextQuery());
    schemasEditor.load(searchSet.getSchemas());
    itemDefsEditor.load(searchSet.getItemDefs());
  }

  public void save(SearchSet searchSet) {
    searchSet.setFreetextQuery(freetextQuery.getText());
    searchSet.setSchemas(schemasEditor.saveAsList());
    searchSet.setItemDefs(itemDefsEditor.saveAsList());
  }
}
