package com.tle.core.payment.service.impl;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.TaxType;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.TaxDao;
import com.tle.core.payment.events.TaxTypeReferencesEvent;
import com.tle.core.payment.service.TaxService;
import com.tle.core.payment.service.session.TaxTypeEditingBean;
import com.tle.core.payment.service.session.TaxTypeEditingSession;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
@Bind(TaxService.class)
@Singleton
@SecureEntity(TaxService.ENTITY_TYPE)
public class TaxServiceImpl extends AbstractEntityServiceImpl<TaxTypeEditingBean, TaxType, TaxService>
	implements
		TaxService
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(TaxService.class);

	// A bit arbitrary, but stops testers using ridiculous values
	private static final double MAX_PERCENT = 1000.0;
	private static final int MAX_PERCENT_DECIMALS = 4;

	// private final TaxDao taxDao;

	@Inject
	public TaxServiceImpl(TaxDao taxDao)
	{
		super(Node.TAX, taxDao);
		// this.taxDao = taxDao;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<TaxTypeEditingBean, TaxType>> SESSION createSession(
		String sessionId, EntityPack<TaxType> pack, TaxTypeEditingBean bean)
	{
		return (SESSION) new TaxTypeEditingSession(sessionId, pack, bean);
	}

	@Override
	protected void doValidation(EntityEditingSession<TaxTypeEditingBean, TaxType> session, TaxType tax,
		List<ValidationError> errors)
	{
		// No, only validate editing beans
	}

	@Override
	protected void doValidationBean(TaxTypeEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);

		final TaxTypeEditingBean taxb = bean;

		final String code = taxb.getCode();
		if( Strings.isNullOrEmpty(code) )
		{
			errors.add(new ValidationError(FIELD_CODE, resources.getString("tax.validation.code.mandatory")));
		}
		else if( code.length() > 10 )
		{
			errors.add(new ValidationError(FIELD_CODE, resources.getString("tax.validation.code.toolong")));
		}

		final String percent = taxb.getPercent();
		final BigDecimal num = parsePercent(percent, errors);
		if( num == null )
		{
			return;
		}
		final int compareToZero = num.compareTo(BigDecimal.ZERO);
		if( compareToZero < 0 )
		{
			errors.add(new ValidationError(FIELD_PERCENT, resources.getString("tax.validation.percent.nonnegative")));
		}
		if( num.compareTo(new BigDecimal(MAX_PERCENT)) > 0 )
		{
			errors.add(new ValidationError(FIELD_PERCENT, resources.getString("tax.validation.percent.toobig",
				MAX_PERCENT)));
		}
	}

	/**
	 * Normal Java parsing is evil. It doesn't blow up for strings such as
	 * "23.45hhhjd##!", instead it returns 23.45
	 * 
	 * @param stringVal
	 * @return
	 */
	private BigDecimal parsePercent(String percent, List<ValidationError> errors)
	{
		if( Check.isEmpty(percent) )
		{
			errors.add(new ValidationError(FIELD_PERCENT, resources.getString("tax.validation.percent.mandatory")));
			return null;
		}

		final DecimalFormat df = (DecimalFormat) (NumberFormat.getNumberInstance(CurrentLocale.getLocale()));
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(MAX_PERCENT_DECIMALS);

		// See
		// http://stackoverflow.com/questions/10906522/how-to-convert-formatted-strings-to-float
		//
		// Have to use the ParsePosition API or else it will silently stop
		// parsing even though some of the characters weren't part of the parsed
		// number.
		final ParsePosition position = new ParsePosition(0);
		df.setParseBigDecimal(true);
		final BigDecimal parsed = (BigDecimal) df.parseObject(percent, position);

		// getErrorIndex() doesn't seem to accurately reflect errors, but
		// getIndex() does reflect how far we successfully parsed.
		if( position.getIndex() == percent.length() )
		{
			if( parsed.scale() > MAX_PERCENT_DECIMALS )
			{
				errors.add(new ValidationError(FIELD_PERCENT, resources.getString("tax.validation.percent.truncate",
					MAX_PERCENT_DECIMALS)));
				return null;
			}
			return parsed;
		}
		errors.add(new ValidationError(FIELD_PERCENT, resources.getString("tax.validation.percent.nonnegative")));
		return null;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected TaxTypeEditingBean createEditingBean()
	{
		return new TaxTypeEditingBean();
	}

	@Override
	protected void populateEditingBean(TaxTypeEditingBean bean, TaxType tax)
	{
		super.populateEditingBean(bean, tax);

		final TaxTypeEditingBean taxb = bean;
		taxb.setCode(tax.getCode());

		final BigDecimal percent = tax.getPercent();
		if( percent != null )
		{
			final NumberFormat df = NumberFormat.getNumberInstance(CurrentLocale.getLocale());
			df.setMinimumFractionDigits(2);
			df.setMaximumFractionDigits(4);
			taxb.setPercent(df.format(percent));
		}
	}

	@Override
	protected void populateEntity(TaxTypeEditingBean bean, TaxType tax)
	{
		super.populateEntity(bean, tax);

		final TaxTypeEditingBean taxb = bean;
		tax.setCode(taxb.getCode());

		final List<ValidationError> errors = Lists.newArrayList();
		tax.setPercent(parsePercent(taxb.getPercent(), errors));
		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	@Override
	public BigDecimal calculateTax(BigDecimal price, Currency currency, List<TaxType> taxes)
	{
		BigDecimal runningTotal = new BigDecimal(0, MathContext.UNLIMITED);
		for( TaxType tax : taxes )
		{
			final BigDecimal taxPercent = tax.getPercent().movePointLeft(2);
			runningTotal = runningTotal.add(price.multiply(taxPercent));
		}
		// round it!! http://dev.equella.com/issues/7590
		final int decimals = (currency == null ? 0 : currency.getDefaultFractionDigits());
		return runningTotal.setScale(decimals, RoundingMode.HALF_UP);
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		final TaxTypeReferencesEvent event = new TaxTypeReferencesEvent(get(id));
		publishEvent(event);
		return event.getReferencingClasses();
	}
}
