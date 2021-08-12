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

import com.tle.core.hibernate.ExtendedDialect;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import org.hibernate.type.BasicType;
import org.hibernate.type.CustomType;

public class HibernateCustomTypes {
  private static final CustomType TYPE_BLANKABLE =
      new CustomType(new HibernateEscapedString(Types.VARCHAR), new String[] {"blankable"});

  private static final CustomType TYPE_XSTREAM =
      new CustomType(
          new ImmutableHibernateXStreamType(Types.CLOB), new String[] {"xstream_immutable"});

  private static final CustomType TYPE_CSV =
      new CustomType(new HibernateCsvType(Types.VARCHAR), new String[] {"csv"});

  public static List<BasicType> getCustomTypes(ExtendedDialect dialect) {
    final CustomType TYPE_JSON =
        new CustomType(new JsonStringCustomType(dialect), new String[] {"json"});
    return Arrays.asList(TYPE_JSON, TYPE_BLANKABLE, TYPE_CSV, TYPE_XSTREAM);
  }
}
