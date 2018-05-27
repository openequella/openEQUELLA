/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.freetext.extracter.handler;

import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class CappedBodyContentHandler extends BodyContentHandler {
	private long parseDurationCap;

	private long start = 0L;

	private int durationCheckCounter = 0;

	private static final int DURATION_CHECK_FREQUENCY = 200;

	private static final Logger LOGGER = LoggerFactory.getLogger(CappedBodyContentHandler.class);

	public CappedBodyContentHandler(ContentHandler handler, long parseDurationCap) {
		super(handler);
		this.parseDurationCap = parseDurationCap;
	}

	@Override
	public void startDocument() throws SAXException {
		LOGGER.debug("Beginning startDocument of parse with a parseDurationCap=[" + parseDurationCap + "].");
		start = System.currentTimeMillis();
		super.startDocument();
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		checkIfOverCappedDuration();
		super.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		checkIfOverCappedDuration();
		super.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		checkIfOverCappedDuration();
		super.skippedEntity(name);
	}

	private void checkIfOverCappedDuration() throws SAXException{
		if(durationCheckCounter++ > DURATION_CHECK_FREQUENCY) {
			long dur = System.currentTimeMillis() - start;
			LOGGER.debug("Checking if parser is past capped duration - time spent so far: " + dur);
			if(parseDurationCap < dur) {
				throw new SAXException("Parser exceeded maximum duration of parse.  max="+parseDurationCap);
			} else {
				durationCheckCounter = 0;
			}
		}
	}
}