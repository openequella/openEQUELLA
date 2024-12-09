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

package com.tle.core.security.impl;

import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.security.expressions.PostfixExpressionParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class AclExpressionCollector extends PostfixExpressionParser<Object> {
  private List<String> results = new ArrayList<String>();

  public AclExpressionCollector() {
    super();
  }

  public List<String> getComponents(String expression) {
    if (Check.isEmpty(expression)) {
      return Collections.emptyList();
    } else {
      getResult(expression);
      return results;
    }
  }

  @Override
  protected void doOperator(Stack<Pair<Object, Integer>> operands, BooleanOp operator) {
    results.add(operator.toString());
  }

  @Override
  protected Object processOperand(String token) {
    results.add(token);
    return null;
  }
}
