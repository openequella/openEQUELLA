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

package com.tle.web.sections.js.generic.statement;

import com.google.common.collect.Lists;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

public class StatementBlock implements JSStatements {
  protected final Deque<JSStatements> statementsList = Lists.newLinkedList();
  private boolean seperate = false;

  public StatementBlock() {
    // nothing
  }

  private StatementBlock(JSStatements statement1, JSStatements statement2) {
    addStatements(statement1);
    addStatements(statement2);
  }

  public StatementBlock(Collection<? extends JSStatements> statements) {
    addStatements(statements);
  }

  public void addStatementsFirst(JSStatements statements) {
    if (statements != null) {
      statementsList.addFirst(statements);
    }
  }

  public void addStatements(JSStatements statements) {
    if (statements != null) {
      statementsList.add(statements);
    }
  }

  public void addStatements(Collection<? extends JSStatements> list) {
    for (JSStatements jsStatements : list) {
      addStatements(jsStatements);
    }
  }

  @Override
  public String getStatements(RenderContext info) {
    StringBuilder statementsString = new StringBuilder();
    for (JSStatements statement : statementsList) {
      if (statement != null) {
        statementsString.append(statement.getStatements(info));
        if (seperate) {
          statementsString.append(Js.NEWLINE);
        }
      }
    }
    return statementsString.toString();
  }

  @Override
  public void preRender(PreRenderContext info) {
    info.preRender(statementsList);
  }

  public boolean isSeperate() {
    return seperate;
  }

  public StatementBlock setSeperate(boolean seperate) {
    this.seperate = seperate;
    return this;
  }

  public static JSStatements get(JSStatements statement1, JSStatements statement2) {
    if (statement1 instanceof StatementBlock) {
      ((StatementBlock) statement1).addStatements(statement2);
      return statement1;
    }
    if (statement2 instanceof StatementBlock) {
      ((StatementBlock) statement2).addStatementsFirst(statement1);
      return statement2;
    }
    if (statement1 == null) {
      return statement2;
    }
    if (statement2 == null) {
      return statement1;
    }
    return new StatementBlock(statement1, statement2);
  }

  public static JSStatements get(JSStatements... statements) {
    if (statements.length == 0) {
      return null;
    }
    if (statements.length == 1) {
      return statements[0];
    }
    if (statements.length == 2) {
      return get(statements[0], statements[1]);
    }
    return new StatementBlock(Arrays.asList(statements));
  }

  public static JSStatements get(Collection<? extends JSStatements> statements) {
    return new StatementBlock(statements);
  }

  public boolean isEmpty() {
    return statementsList.isEmpty();
  }
}
