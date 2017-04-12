package com.tle.core.connectors.dao;

import java.util.List;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.dao.AbstractEntityDao;

/**
 * @author aholland
 */
public interface ConnectorDao extends AbstractEntityDao<Connector>
{
	List<Connector> enumerateForUrl(String url);
}
