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

package com.dytech.gui.adapters;

import java.util.List;
import javax.swing.table.DefaultTableModel;

class DefaultTableModelAdapter implements TablePasteModel {
  private DefaultTableModel model;

  public DefaultTableModelAdapter(DefaultTableModel model) {
    this.model = model;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.adapters.TablePasteModelAdapter#insertRow(int,
   * java.util.Vector)
   */
  @Override
  public void insertRow(int row, List<?> data) {
    model.insertRow(row, data.toArray());
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.adapters.TablePasteModelAdapter#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int column) {
    return model.getColumnClass(column);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.adapters.TablePasteModelAdapter#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return model.getColumnCount();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.gui.adapters.TablePasteModelAdapter#getRowCount()
   */
  @Override
  public int getRowCount() {
    return model.getRowCount();
  }
}
