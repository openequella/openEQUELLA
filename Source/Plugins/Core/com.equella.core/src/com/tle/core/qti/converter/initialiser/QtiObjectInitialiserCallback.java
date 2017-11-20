/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.qti.converter.initialiser;

import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.common.qti.entity.QtiAssessmentTest;
import com.tle.common.qti.entity.QtiItemResult;
import com.tle.core.entity.service.impl.EntityInitialiserCallback;
import com.tle.core.hibernate.equella.service.Property;

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
