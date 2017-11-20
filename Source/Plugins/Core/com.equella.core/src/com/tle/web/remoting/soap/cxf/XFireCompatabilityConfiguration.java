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

package com.tle.web.remoting.soap.cxf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebParam;
import javax.xml.namespace.QName;

import org.apache.cxf.aegis.databinding.XFireCompatibilityServiceConfiguration;
import org.apache.cxf.service.model.OperationInfo;

import com.tle.common.Check;

@SuppressWarnings("nls")
public class XFireCompatabilityConfiguration extends XFireCompatibilityServiceConfiguration
{
	@Override
	public QName getInParameterName(OperationInfo op, Method method, int paramNumber)
	{
		Annotation[][] annos = method.getParameterAnnotations();
		if( annos.length > paramNumber )
		{
			Annotation[] paramAnnos = annos[paramNumber];
			for( Annotation anno : paramAnnos )
			{
				if( anno instanceof WebParam )
				{
					WebParam webParam = (WebParam) anno;
					String targetNs = webParam.targetNamespace();
					if( Check.isEmpty(targetNs) )
					{
						targetNs = op.getName().getNamespaceURI();
					}
					return new QName(targetNs, webParam.name());
				}
			}
		}

		return new QName(op.getName().getNamespaceURI(), "in" + paramNumber);
	}

	@Override
	public QName getOutParameterName(OperationInfo op, Method method, int paramNumber)
	{
		return new QName(op.getName().getNamespaceURI(), "out");
	}
}
