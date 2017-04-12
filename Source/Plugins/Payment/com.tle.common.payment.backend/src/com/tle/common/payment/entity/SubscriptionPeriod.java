package com.tle.common.payment.entity;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;

@Entity
@AccessType("field")
public class SubscriptionPeriod implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	/*
	 * Do not re-order this enum!
	 */
	public enum DurationUnit
	{
		DAYS, WEEKS, MONTHS, YEARS
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "periodUuidIndex")
	private String uuid;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "periodInstitutionIndex")
	private Institution institution;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "periodName")
	private LanguageBundle name;

	@Min(0)
	private int duration;

	/**
	 * An <em>index</em> into the Duration enum.
	 */
	@Min(0)
	private int durationUnit;

	/**
	 * A count of millis (but not a *true* count, since 3 months can vary in
	 * length for example), but an indicator of the scale of the duration (for
	 * sorting)
	 */
	@Min(0)
	private long magnitude;

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
		updateMagnitude();
	}

	public DurationUnit getDurationUnit()
	{
		return DurationUnit.values()[durationUnit];
	}

	public void setDurationUnit(DurationUnit durationUnitEnum)
	{
		this.durationUnit = durationUnitEnum.ordinal();
		updateMagnitude();
	}

	public long getMagnitude()
	{
		return magnitude;
	}

	private void updateMagnitude()
	{
		final int days;
		switch( getDurationUnit() )
		{
			case DAYS:
				days = 1;
				break;
			case WEEKS:
				days = 7;
				break;
			case MONTHS:
				days = 30;
				break;
			case YEARS:
				days = 365;
				break;
			default:
				days = 0;
		}
		magnitude = getDuration() * TimeUnit.DAYS.toMillis(days);
	}
}
