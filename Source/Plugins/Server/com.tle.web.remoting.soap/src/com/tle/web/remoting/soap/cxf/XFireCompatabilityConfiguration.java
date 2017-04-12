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
