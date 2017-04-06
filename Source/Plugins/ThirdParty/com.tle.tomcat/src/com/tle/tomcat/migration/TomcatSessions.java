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