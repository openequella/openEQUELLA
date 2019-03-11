package com.tle.common.interfaces;

import com.sun.istack.internal.Nullable;

public class SimpleI18NString implements I18NString {
  @Nullable private final String text;
  @Nullable private final String defaultText;

  public SimpleI18NString(@Nullable String text) {
    this.text = text;
    this.defaultText = null;
  }

  public SimpleI18NString(@Nullable String text, @Nullable String defaultText) {
    this.text = text;
    this.defaultText = defaultText;
  }

  public SimpleI18NString(@Nullable I18NString text, @Nullable String defaultText) {
    this.text = text == null ? null : text.toString();
    this.defaultText = defaultText;
  }

  @Nullable
  @Override
  public String toString() {
    if (text == null) {
      return defaultText;
    }
    return text;
  }
}
