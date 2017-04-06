package com.tle.core.services.entity;

import java.io.Serializable;
import java.util.Map;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;

/**
 * @author aholland
 */
public interface EntityEditingSession<B extends EntityEditingBean, E extends BaseEntity> extends Serializable
{
	@Deprecated
	<P extends EntityPack<E>> P getPack();

	/**
	 * Use getBean instead
	 * 
	 * @return
	 */
	@Deprecated
	E getEntity();

	boolean isNew();

	B getBean();

	String getSessionId();

	String getStagingId();

	void setStagingId(String stagingId);

	Map<String, Object> getValidationErrors();

	Map<Class<? extends Serializable>, Serializable> getAttributes();

	<T extends Serializable> T getAttribute(Class<T> key);

	<T extends Serializable> void setAttribute(Class<T> key, T value);

	void setValid(boolean valid);

	boolean isValid();

}
