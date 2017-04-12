package com.tle.core.payment.migration;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
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
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.beans.IdCloneable;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.payment.migration.CreateDefaultPaymentObjectsDatabaseMigration.FakeSubscriptionPeriod.DurationUnit;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateDefaultPaymentObjectsDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String keyPrefix = PluginServiceImpl
		.getMyPluginId(CreateDefaultPaymentObjectsDatabaseMigration.class) + ".migration.adddefaults.";

	private static List<FakeSubscriptionPeriod> getDefaultPeriods(FakeInstitution i)
	{
		final List<FakeSubscriptionPeriod> periods = Lists.newArrayList();
		// i18n? What's the point? Also, how does one edit this?
		periods.add(createPeriod(i, "Week", 1, DurationUnit.WEEKS));
		periods.add(createPeriod(i, "Month", 1, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "3 Months", 3, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "6 Months", 6, DurationUnit.MONTHS));
		periods.add(createPeriod(i, "Year", 1, DurationUnit.YEARS));
		return periods;
	}

	private static FakeSubscriptionPeriod createPeriod(FakeInstitution i, String name, int duration, DurationUnit unit)
	{
		final FakeSubscriptionPeriod p = new FakeSubscriptionPeriod();
		p.setInstitution(i);
		p.setUuid(UUID.randomUUID().toString());
		p.setName(createName(name));
		p.setDuration(duration);
		p.setDurationUnit(unit);
		return p;
	}

	private static LanguageBundle createName(String name)
	{
		LanguageBundle bundle = new LanguageBundle();
		Map<String, LanguageString> strings = new HashMap<String, LanguageString>();
		LanguageString langString = new LanguageString();
		langString.setBundle(bundle);
		langString.setLocale("en");
		langString.setText(name);
		strings.put("en", langString);
		bundle.setStrings(strings);
		return bundle;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		Set<Class<?>> domainClasses = new HashSet<Class<?>>();
		Collections.addAll(domainClasses, FakeSubscriptionPeriod.class, LanguageBundle.class, FakeInstitution.class,
			LanguageString.class);
		return domainClasses.toArray(new Class<?>[domainClasses.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeInstitution> institutions = session.createQuery("from Institution").list();
		for( FakeInstitution i : institutions )
		{
			for( FakeSubscriptionPeriod p : getDefaultPeriods(i) )
			{
				session.save(p);
			}
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 5;
	}

	@Entity(name = "SubscriptionPeriod")
	@AccessType("field")
	public static class FakeSubscriptionPeriod implements Serializable, IdCloneable
	{
		private static final long serialVersionUID = 1L;

		public enum DurationUnit
		{
			DAYS, WEEKS, MONTHS, YEARS
		}

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 40, nullable = false)
		@Index(name = "periodUuidIndex")
		String uuid;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "periodInstitutionIndex")
		FakeInstitution institution;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "periodName")
		LanguageBundle name;

		@Min(0)
		int duration;

		@Min(0)
		int durationUnit;

		@Min(0)
		long magnitude;

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

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public void setName(LanguageBundle name)
		{
			this.name = name;
		}

		public void setDuration(int duration)
		{
			this.duration = duration;
		}

		public void setDurationUnit(int durationUnit)
		{
			this.durationUnit = durationUnit;
		}

		public void setMagnitude(long magnitude)
		{
			this.magnitude = magnitude;
		}

		public void setDurationUnit(DurationUnit durationUnitEnum)
		{
			this.durationUnit = durationUnitEnum.ordinal();
			updateMagnitude();
		}

		public DurationUnit getDurationUnit()
		{
			return DurationUnit.values()[durationUnit];
		}

		public int getDuration()
		{
			return duration;
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

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}
}
