package com.tle.common.interfaces;

import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public interface I18NStrings {
  Map<String, String> getStrings();

  I18NString asI18NString(String defaultText);

  @Override
  String toString();
}
