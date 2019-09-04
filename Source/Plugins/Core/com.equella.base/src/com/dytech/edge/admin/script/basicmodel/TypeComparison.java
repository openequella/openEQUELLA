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

package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.tle.common.i18n.CurrentLocale;
import java.util.function.Function;

public class TypeComparison implements Comparison {
  protected UserMethodMethod op;
  protected String value;
  public static Function<String, String> getRoleText;

  public TypeComparison(UserMethodMethod op, String value) {
    this.op = op;
    this.value = value;
  }

  public TypeComparison(Equality equality, String value2) {
    if (equality instanceof Equals) {
      op = new UserMethodMethod.HasRole();
    } else {
      op = new UserMethodMethod.DoesntHasRole();
    }
    this.value = value2;
  }

  public UserMethodMethod getOperation() {
    return op;
  }

  public void setOp(UserMethodMethod op) {
    this.op = op;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toScript() {
    return "user." + op.toScript() + "('" + BasicParser.encode(value) + "')";
  }

  @Override
  public String toEasyRead() {
    return CurrentLocale.get("com.dytech.edge.admin.script.target.role")
        + " "
        + op.toEasyRead()
        + " '"
        + getRoleText.apply(value)
        + "'";
  }
}
