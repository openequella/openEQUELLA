package com.tle.common.institution;

import java.io.Serializable;
import java.util.List;

import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public interface TreeNodeInterface<T extends TreeNodeInterface<T>> extends Serializable, IdCloneable
{
	String getUuid();

	void setUuid(String uuid);

	T getParent();

	void setParent(T parent);

	Institution getInstitution();

	void setInstitution(Institution institution);

	List<T> getAllParents();

	void setAllParents(List<T> allParents);
}