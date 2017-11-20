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

package com.tle.core.copyright;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.Item;

/**
 * @author Aaron
 */
public interface Holding
{
	Date getIssueDate();

	long getId();

	Item getItem();

	List<? extends Portion> getPortions();

	List<String> getIds();

	List<String> getAuthors();

	String getTitle();

	String getPublisher();

	String getPubDate();

	String getDescription();

	String getComments();

	String getLength();

	String getType();

	boolean isOutOfPrint();

	String getAuthorList();

	String getIdList();

	String getVolume();

	String getIssueNumber();

	void setItem(Item item);

	void setPortions(List<? extends Portion> portions);
}
