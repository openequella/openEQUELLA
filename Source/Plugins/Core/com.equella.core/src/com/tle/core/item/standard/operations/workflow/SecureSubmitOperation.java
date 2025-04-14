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

package com.tle.core.item.standard.operations.workflow;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.EDIT_ITEM)
public class SecureSubmitOperation extends SubmitOperation {
  @AssistedInject
  public SecureSubmitOperation(@Nullable @Assisted String message) {
    super(message);
  }

  @AssistedInject
  public SecureSubmitOperation() {
    // no message;
  }
}
