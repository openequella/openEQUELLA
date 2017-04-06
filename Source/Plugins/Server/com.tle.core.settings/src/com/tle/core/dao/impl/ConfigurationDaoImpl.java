package com.tle.core.dao.impl;

import java.util.Collection;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.ConfigurationProperty;
import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.core.dao.ConfigurationDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

@Singleton
@SuppressWarnings("nls")
@Bind(ConfigurationDao.class)
public class ConfigurationDaoImpl extends GenericDaoImpl<ConfigurationProperty, PropertyKey>
	implements
		ConfigurationDao
{
	public ConfigurationDaoImpl()
	{
		super(ConfigurationProperty.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void deleteAll()
	{
		getHibernateTemplate().deleteAll(
			getHibernateTemplate().findByNamedParam(
				"from ConfigurationProperty where key.institutionId = :institution", "institution",
				CurrentInstitution.get().getDatabaseId()));
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public synchronized void deletePropertiesLike(Collection<String> select)
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append("from ConfigurationProperty where (");
		int length = select.size();
		Object[] values = select.toArray(new Object[length + 1]);
		for( int i = 0; i < length; i++ )
		{
			if( i > 0 )
			{
				buffer.append(" or ");
			}
			values[i] = values[i].toString() + '%';
			buffer.append("key.property like ?");
		}
		buffer.append(")");

		buffer.append(" and key.institutionId = ?");
		values[length] = CurrentInstitution.get().getDatabaseId();

		Collection<?> col = getHibernateTemplate().find(buffer.toString(), values);
		getHibernateTemplate().deleteAll(col);

		// If you don't flush then you get a NonUniqueObjectException
		getHibernateTemplate().flush();
	}
}
