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

package com.tle.web.sections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker interface that says any classes implementing this interface / class will be recorded in
 * the SectionTree for quick lookup via this interface / class.
 *
 * <p>Note that all concrete Sections will be registered with their actual class anyway (unless
 * {@link Section#isTreeIndexed()} returns false) This annotation is mostly useful for looking up
 * sections via an abstract super class or an interface , although it is good practice to mark any
 * section you are doing a TreeLookup on as TreeIndexed (since it could be subclassed at a later
 * date). E.g.
 *
 * <pre>
 * <code>info.lookupSection(ResetFiltersParent.class)</code>
 * </pre>
 *
 * OR
 *
 * <pre>
 * <code>&#064;TreeLookup
 * private AbstractSearchQuerySection qs;</code>
 * </pre>
 *
 * both require that ResetFiltersParent and AbstractQuerySection be TreeIndexed.
 *
 * @author Aaron
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TreeIndexed {}
