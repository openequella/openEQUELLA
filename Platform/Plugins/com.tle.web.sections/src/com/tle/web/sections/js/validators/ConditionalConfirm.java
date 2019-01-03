/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.js.validators;

import java.io.Serializable;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.render.Label;

/**
 * Only asks for confirmation if supplied condition is met.
 * 
 * @author Aaron
 */
public class ConditionalConfirm extends Confirm implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final JSExpression condition;

	public ConditionalConfirm(JSExpression condition, Label label)
	{
		super(label);
		this.condition = condition;
	}

	@SuppressWarnings("nls")
	@Override
	public String getValidatorExpressionText(RenderContext info)
	{
		return condition.getExpression(info) + " || " + super.getValidatorExpressionText(info);
	}
}
