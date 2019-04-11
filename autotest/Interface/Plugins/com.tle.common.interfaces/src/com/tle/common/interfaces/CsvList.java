package com.tle.common.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CsvList {
  private final List<String> values;

  public CsvList(List<String> values) {
    this.values = values;
  }

  public List<String> getValues() {
    return values;
  }

  @SuppressWarnings("nls")
  public static CsvList valueOf(String value) {
    final List<String> values = new ArrayList<String>();
    final String[] valsArr = value.split(",");
    for (String val : valsArr) {
      val = val.trim();
      if (!val.isEmpty()) {
        values.add(val);
      }
    }
    return new CsvList(values);
  }

  public static List<String> asList(CsvList list, String... defaultValues) {
    if (list == null) {
      if (defaultValues.length == 0) {
        return Collections.emptyList();
      }
      List<String> all = new ArrayList<String>();
      for (String val : defaultValues) {
        all.add(val);
      }
      return all;
    }
    return list.getValues();
  }
}
