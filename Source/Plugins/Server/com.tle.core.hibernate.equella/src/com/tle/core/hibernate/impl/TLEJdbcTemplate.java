package com.tle.core.hibernate.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.tle.core.guice.Bind;

@Bind(JdbcTemplate.class)
@Singleton
public class TLEJdbcTemplate extends JdbcTemplate
{
	@Inject
	private DataSource dataSource;

	@PostConstruct
	@Override
	public void afterPropertiesSet()
	{
		setDataSource(dataSource);
	}

}