package com.tle.hibernate.dialect;

import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

// Standard Hib 5.3+ does not include the unique constraints
//  in the create table logic, so we need to add our own.
public class InPlaceUniqueDelegate extends DefaultUniqueDelegate {
  private static final Log LOGGER = LogFactory.getLog(InPlaceUniqueDelegate.class);

  public InPlaceUniqueDelegate(Dialect dialect) {
    super(dialect);
  }

  @Override
  public String getTableCreationUniqueConstraintsFragment(Table table) {
    Iterator<UniqueKey> iter = table.getUniqueKeyIterator();
    StringBuilder sb = new StringBuilder();
    while (iter.hasNext()) {
      sb.append(", ").append(uniqueConstraintSql(iter.next()));
    }
    final String sql = sb.toString();
    LOGGER.debug(
        "For table [" + table.getName() + "], generated the uniqueness constraint: " + sql);
    return sql;
  }
}
