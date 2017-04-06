package com.tle.common.payment.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
public class TaxType extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	/**
	 * E.g. GST,VAT
	 */
	@Column(length = 10, nullable = false)
	private String code;

	// FFS. Hibernate should handle this (percent is reserved word in SQL
	// Server)
	@Column(name = "pct", nullable = false, precision = 9, scale = 4)
	private BigDecimal percent;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public BigDecimal getPercent()
	{
		return percent;
	}

	public void setPercent(BigDecimal percent)
	{
		this.percent = percent;
	}
}
