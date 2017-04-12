package com.tle.core.healthcheck.listeners.bean;

import java.io.Serializable;

public class ServiceStatus implements Serializable
{
	public enum ServiceName
	{
		INDEX, FILESTORE, IMAGEMAGICK
	}

	public enum Status
	{
		WAITING, GOOD, BAD
	}

	private ServiceName serviceName;
	private Status serviceStatus;
	private String moreInfo;

	public ServiceStatus(ServiceName name)
	{
		this.serviceName = name;
		this.serviceStatus = Status.WAITING;
	}

	public ServiceName getServiceName()
	{
		return serviceName;
	}

	public void setServiceName(ServiceName serviceName)
	{
		this.serviceName = serviceName;
	}

	public Status getServiceStatus()
	{
		return serviceStatus;
	}

	public void setServiceStatus(Status serviceStatus)
	{
		this.serviceStatus = serviceStatus;
	}

	public String getMoreInfo()
	{
		return moreInfo;
	}

	public void setMoreInfo(String moreInfo)
	{
		this.moreInfo = moreInfo;
	}

}
