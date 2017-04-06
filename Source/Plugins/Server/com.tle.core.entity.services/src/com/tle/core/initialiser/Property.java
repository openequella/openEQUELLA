package com.tle.core.initialiser;

import java.lang.annotation.Annotation;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.CollectionOfElements;

import com.tle.common.DoNotClone;
import com.tle.common.DoNotSimplify;

public abstract class Property
{
	private Boolean isCascade;
	private Boolean isManyToMany;
	private Boolean isManyToOne;
	private Boolean isDoNotSimplify;
	private Boolean isDoNotClone;

	public Property()
	{
		super();
	}

	public boolean isCascade()
	{
		if( isCascade == null )
		{
			isCascade = isCascadeAll();
		}
		return isCascade;
	}

	public boolean isManyToMany()
	{
		if( isManyToMany == null )
		{
			isManyToMany = getAnnotation(ManyToMany.class) != null;
		}
		return isManyToMany;
	}

	public boolean isManyToOne()
	{
		if( isManyToOne == null )
		{
			isManyToOne = getAnnotation(ManyToOne.class) != null;
		}
		return isManyToOne;
	}

	public boolean isDoNotSimplify()
	{
		if( isDoNotSimplify == null )
		{
			isDoNotSimplify = getAnnotation(DoNotSimplify.class) != null;
		}
		return isDoNotSimplify;
	}

	public boolean isDoNotClone()
	{
		if( isDoNotClone == null )
		{
			isDoNotClone = getAnnotation(DoNotClone.class) != null;
		}
		return isDoNotClone;
	}

	private boolean isCascadeAll()
	{
		OneToMany onetomany = getAnnotation(OneToMany.class);
		if( onetomany != null )
		{
			return hasCascadeAll(onetomany.cascade());
		}
		OneToOne onetoone = getAnnotation(OneToOne.class);
		if( onetoone != null )
		{
			return hasCascadeAll(onetoone.cascade());
		}
		ManyToOne manyToOne = getAnnotation(ManyToOne.class);
		if( manyToOne != null )
		{
			return hasCascadeAll(manyToOne.cascade());
		}
		// CollectionOfElements is a 'default' cascade all
		if( getAnnotation(CollectionOfElements.class) != null )
		{
			return true;
		}

		return false;
	}

	private boolean hasCascadeAll(CascadeType[] cascades)
	{
		for( CascadeType type : cascades )
		{
			if( CascadeType.ALL == type || CascadeType.REMOVE == type )
			{
				return true;
			}
		}
		return false;
	}

	public abstract Class<?> getReturnType();

	abstract <T extends Annotation> T getAnnotation(Class<T> name);

	public abstract Object get(Object object);

	public abstract void set(Object object, Object newObject);

	public abstract boolean isAnnotationPresent(Class<? extends Annotation> name);
}
