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

package com.tle.tomcat.migration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

// Only used for creation
@Entity
@AccessType("field")
public class TomcatSessions
{
	@Id
	@Column(length = 100)
	String id;
	@Column(length = 1, nullable = false)
	char valid;
	@Column(nullable = false)
	int maxinactive;
	@Column(nullable = false)
	long lastaccess;
	@Index(name = "tomcatSessionAppIndex")
	@Column(length = 255, nullable = false)
	String app;

	@Lob
	@Column(nullable = false)
	byte[] data;
}