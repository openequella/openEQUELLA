package com.tle.core.hibernate.dao;

import java.io.Serializable;
import java.util.List;

public interface GenericInstitutionalDao<T, ID extends Serializable> extends GenericDao<T, ID>
{
	List<T> enumerateAll();

	List<ID> enumerateAllIds();
}
