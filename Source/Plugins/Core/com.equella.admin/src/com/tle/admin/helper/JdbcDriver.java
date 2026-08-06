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

package com.tle.admin.helper;

import com.google.common.base.Preconditions;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.swing.JComboBox;

@SuppressWarnings("nls")
public class JdbcDriver implements Check.FieldEquality<JdbcDriver> {
  private static final Collection<JdbcDriver> DRIVERS =
      Collections.unmodifiableCollection(
          Arrays.asList(
              new JdbcDriver(
                  "sqlserver",
                  "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                  "jdbc:sqlserver://<host>[:<1433>];SelectMethod=cursor;databaseName=<dbname>"),
              new JdbcDriver(
                  "oracle",
                  "oracle.jdbc.OracleDriver",
                  "jdbc:oracle:thin:@<host>[:<1521>]:<dbname>"),
              new JdbcDriver(
                  "postgres",
                  "org.postgresql.Driver",
                  "jdbc:postgresql://<host>[:<5432>]/<dbname>")));

  private final String name;
  private final String driverClass;
  private final String jdbcUrl;

  public static void addDrivers(JComboBox<JdbcDriver> cb) {
    for (JdbcDriver d : getDrivers()) {
      cb.addItem(d);
    }
  }

  public static Collection<JdbcDriver> getDrivers() {
    return DRIVERS;
  }

  public JdbcDriver(String driverClazz) {
    Preconditions.checkNotNull(driverClazz);

    this.name = null;
    this.driverClass = driverClazz;
    this.jdbcUrl = null;
  }

  private JdbcDriver(String nameKeyPart, String driverClass, String jdbcUrl) {
    Preconditions.checkNotNull(driverClass);

    this.name = CurrentLocale.get("com.tle.admin.jdbcdriver." + nameKeyPart);
    this.driverClass = driverClass;
    this.jdbcUrl = jdbcUrl;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    return Check.commonEquals(this, obj);
  }

  @Override
  public boolean checkFields(JdbcDriver rhs) {
    return Objects.equals(driverClass, rhs.driverClass);
  }

  @Override
  public int hashCode() {
    return driverClass.hashCode();
  }
}
