package com.tle.configmanager;

import org.apache.commons.configuration.PropertiesConfiguration;

@SuppressWarnings("nls")
public class PropertyEditor extends PropertiesConfiguration {
  public PropertyEditor() {
    setLayout(new ExtendedPropertiesLayout(this));
    setEncoding("UTF-8");
    setDelimiterParsingDisabled(true);
  }
}
