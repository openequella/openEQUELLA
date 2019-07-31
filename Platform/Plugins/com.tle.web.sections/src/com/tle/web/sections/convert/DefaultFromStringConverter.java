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

package com.tle.web.sections.convert;

import net.entropysoft.transmorph.converters.ImmutableIdentityConverter;
import net.entropysoft.transmorph.converters.MultiConverter;
import net.entropysoft.transmorph.converters.StringToBoolean;
import net.entropysoft.transmorph.converters.StringToCalendar;
import net.entropysoft.transmorph.converters.StringToDate;
import net.entropysoft.transmorph.converters.StringToNumber;
import net.entropysoft.transmorph.converters.enums.StringToEnum;

public class DefaultFromStringConverter extends MultiConverter {
  private StringToEnum stringToEnum = new StringToEnum();
  private StringToBoolean stringToBoolean = new StringToBoolean();
  private StringToCalendar stringToCalendar = new StringToCalendar();
  private StringToDate stringToDate = new StringToDate();
  private StringToNumber stringToNumber = new StringToNumber();
  private ImmutableIdentityConverter immutable = new ImmutableIdentityConverter();

  public DefaultFromStringConverter() {
    super(false);
    addConverter(immutable);
    addConverter(stringToEnum);
    addConverter(stringToBoolean);
    addConverter(stringToCalendar);
    addConverter(stringToDate);
    addConverter(stringToNumber);
  }
}
