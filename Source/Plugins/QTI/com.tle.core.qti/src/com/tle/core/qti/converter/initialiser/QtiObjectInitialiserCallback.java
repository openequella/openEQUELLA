package com.tle.core.qti.converter.initialiser;

import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.common.qti.entity.QtiItemResult;
import com.tle.core.initialiser.Property;
import com.tle.core.institution.convert.EntityInitialiserCallback;

/**
 * @author Aaron
 */
public class QtiObjectInitialiserCallback extends EntityInitialiserCallback
{
	@Override
	public void set(Object obj, Property property, Object value)
	{
		if( value instanceof QtiAssessmentItem )
		{
			QtiAssessmentItem toset = (QtiAssessmentItem) value;
			toset.setUuid(((QtiAssessmentItem) property.get(obj)).getUuid());
		}
		else if( value instanceof QtiAssessmentItemRef )
		{
			QtiAssessmentItemRef toset = (QtiAssessmentItemRef) value;
			toset.setUuid(((QtiAssessmentItemRef) property.get(obj)).getUuid());
		}
		else if( value instanceof QtiAssessmentTest )
		{
			QtiAssessmentTest toset = (QtiAssessmentTest) value;
			toset.setUuid(((QtiAssessmentTest) property.get(obj)).getUuid());
		}
		else if( value instanceof QtiItemResult )
		{
			QtiItemResult toset = (QtiItemResult) value;
			toset.setId(((QtiItemResult) property.get(obj)).getId());
		}
		super.set(obj, property, value);
	}

	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof QtiAssessmentItem )
		{
			QtiAssessmentItem toset = (QtiAssessmentItem) newObj;
			QtiAssessmentItem oldObj = (QtiAssessmentItem) old;
			toset.setUuid(oldObj.getUuid());
		}
		else if( old instanceof QtiAssessmentItemRef )
		{
			QtiAssessmentItemRef toset = (QtiAssessmentItemRef) newObj;
			QtiAssessmentItemRef oldObj = (QtiAssessmentItemRef) old;
			toset.setUuid(oldObj.getUuid());
		}
		else if( old instanceof QtiAssessmentTest )
		{
			QtiAssessmentTest toset = (QtiAssessmentTest) newObj;
			QtiAssessmentTest oldObj = (QtiAssessmentTest) old;
			toset.setUuid(oldObj.getUuid());
		}
		else if( old instanceof QtiItemResult )
		{
			QtiItemResult toset = (QtiItemResult) newObj;
			QtiItemResult oldObj = (QtiItemResult) old;
			toset.setId(oldObj.getId());
		}
		super.entitySimplified(old, newObj);
	}
}
