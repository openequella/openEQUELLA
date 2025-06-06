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

package com.dytech.gui;

import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JNumberTextField extends JTextField {
  public JNumberTextField(int maxNumber) {
    super(new NumberDocument(maxNumber), null, 0);
  }

  public void clear() {
    ((NumberDocument) getDocument()).clear();
  }

  @Override
  public void setText(String t) {
    if (t == null || t.length() == 0) {
      clear();
    } else {
      super.setText(t);
    }
  }

  public int getNumber() {
    return getNumber(-1);
  }

  public int getNumber(int defaultValue) {
    String t = getText();
    return t.length() == 0 ? defaultValue : Integer.parseInt(t);
  }

  private static class NumberDocument extends PlainDocument {
    private int maxNumber;

    public NumberDocument(int maxNumber) {
      this.maxNumber = maxNumber;
    }

    public void clear() {
      try {
        super.remove(0, getLength());
      } catch (BadLocationException ex) {
        throw new RuntimeException("This should never happen", ex);
      }
    }

    @Override
    public void insertString(int offset, String s, AttributeSet attributeSet)
        throws BadLocationException {
      try {
        int value = Integer.parseInt(s);
        if (value < 0) {
          Toolkit.getDefaultToolkit().beep();
          return;
        }
      } catch (NumberFormatException ex) {
        Toolkit.getDefaultToolkit().beep();
        return;
      }

      super.insertString(offset, s, attributeSet);

      if (Integer.parseInt(getText(0, getLength())) > maxNumber) {
        Toolkit.getDefaultToolkit().beep();
        super.remove(offset, s.length());
      }
    }
  }
}
