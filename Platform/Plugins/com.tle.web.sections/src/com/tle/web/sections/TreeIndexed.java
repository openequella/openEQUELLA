package com.tle.web.sections;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A marker interface that says any classes implementing this interface / class
 * will be recorded in the SectionTree for quick lookup via this interface /
 * class.
 * </p>
 * <p>
 * Note that all concrete Sections will be registered with their actual class
 * anyway (unless {@link Section#isTreeIndexed()} returns false) This annotation
 * is mostly useful for looking up sections via an abstract super class or an
 * interface , although it is good practice to mark any section you are doing a
 * TreeLookup on as TreeIndexed (since it could be subclassed at a later date).
 * </p>
 * E.g.
 * 
 * <pre>
 * <code>info.lookupSection(ResetFiltersParent.class)</code>
 * </pre>
 * 
 * OR
 * 
 * <pre>
 * <code>&#064;TreeLookup
 * private AbstractSearchQuerySection qs;</code>
 * </pre>
 * 
 * both require that ResetFiltersParent and AbstractQuerySection be TreeIndexed.
 * 
 * @author Aaron
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TreeIndexed
{
}
