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

package com.tle.web.sections.result.util;

public class PluralKeyLabel extends KeyLabel {

  public PluralKeyLabel(String key, long count) {
    super(key + ((count == 1) ? ".1" : ""), count);
  }

  public PluralKeyLabel(String key, long count, Object... args) {
    super(key + ((count == 1) ? ".1" : ""), combineArgs(count, args));
  }

  private static Object[] combineArgs(long count, Object[] args) {
    final Object[] newArgs = new Object[1 + args.length];
    if (count > 1) {
      newArgs[0] = count;
      System.arraycopy(args, 0, newArgs, 1, args.length);
    } else {
      newArgs[0] = args[0];
    }
    return newArgs;
  }
}
