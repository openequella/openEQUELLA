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

package com.tle.core.hibernate.type;

import com.google.common.base.Objects;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.hibernate.dialect.ExtendedOracle10gDialect;
import com.tle.hibernate.dialect.ExtendedPostgresDialect;
import com.tle.hibernate.dialect.SQLServerDialect;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

/**
 * Provide a custom hibernate type to support JSON String in Postgres, Oracle and SQL Server. For
 * Postgres, the column type is 'jsonb'. For Oracle, the column type is 'nclob'. For SQL Server, the
 * column type is 'nvarchar(max)'.
 */
public class JsonStringCustomType implements UserType {
  private final ExtendedDialect dialect;

  public JsonStringCustomType(ExtendedDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public int[] sqlTypes() {
    int jsonStringSqlType = ExtendedPostgresDialect.OEQ_JSON;

    if (dialect instanceof SQLServerDialect) {
      jsonStringSqlType = Types.LONGNVARCHAR;
    } else if (dialect instanceof ExtendedOracle10gDialect) {
      jsonStringSqlType = Types.NCLOB;
    }

    return new int[] {jsonStringSqlType};
  }

  @Override
  public Class<?> returnedClass() {
    return String.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return Objects.equal(x, y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  @Override
  public Object nullSafeGet(
      ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
      throws HibernateException, SQLException {
    return StandardBasicTypes.STRING.nullSafeGet(rs, names[0], session);
  }

  @Override
  public void nullSafeSet(
      PreparedStatement st, Object object, int index, SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    String value = object == null ? null : object.toString();
    // For Postgres, we must use a `PGobject` to save the value since the column type is 'jsonb'.
    if (dialect instanceof ExtendedPostgresDialect) {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(value);
      st.setObject(index, jsonObject, Types.OTHER);
      return;
    }
    // For other databases, just save as a string.
    st.setString(index, value);
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) deepCopy(value);
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return deepCopy(cached);
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return deepCopy(original);
  }
}
