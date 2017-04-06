package com.tle.core.payment.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Region;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.payment.dao.PaymentGatewayDao;
import com.tle.core.payment.events.listeners.RegionDeletionListener;
import com.tle.core.payment.events.listeners.RegionReferencesListener;
import com.tle.core.payment.gateway.DefaultPaymentGatewayExtensionService;
import com.tle.core.payment.gateway.GatewayTypeDescriptor;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfo;
import com.tle.core.payment.gateway.PaymentGatewayCheckoutInfoExtension;
import com.tle.core.payment.gateway.PaymentGatewayImplementation;
import com.tle.core.payment.gateway.PaymentGatewayServiceExtension;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.payment.service.session.PaymentGatewayEditingSession;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;
import com.tle.core.user.CurrentInstitution;

@SuppressWarnings("nls")
@Bind(PaymentGatewayService.class)
@Singleton
@SecureEntity(PaymentGatewayService.ENTITY_TYPE)
public class PaymentGatewayServiceImpl
	extends
		AbstractEntityServiceImpl<PaymentGatewayEditingBean, PaymentGateway, PaymentGatewayService>
	implements
		PaymentGatewayService,
		RegionDeletionListener,
		RegionReferencesListener
{
	private static final String KEY_ENABLED_ERROR = "com.tle.core.payment.backend.validate.enable.multiple";

	private final PaymentGatewayDao payDao;

	@Inject
	private DefaultPaymentGatewayExtensionService defaultExtension;

	private PluginTracker<PaymentGatewayServiceExtension> typesTracker;
	private PluginTracker<PaymentGatewayImplementation> implTracker;
	private PluginTracker<PaymentGatewayCheckoutInfoExtension> infoTracker;

	private Map<String, GatewayTypeDescriptor> typeDescriptorMap;
	private Collection<GatewayTypeDescriptor> typeDescriptorList;

	@Inject
	public PaymentGatewayServiceImpl(PaymentGatewayDao payDao)
	{
		super(Node.PAYMENT_GATEWAY, payDao);
		this.payDao = payDao;
	}

	@Override
	protected PaymentGatewayEditingBean createEditingBean()
	{
		return new PaymentGatewayEditingBean();
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected void doValidation(EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session,
		PaymentGateway entity, List<ValidationError> errors)
	{
		// None
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway>> SESSION createSession(
		String sessionId, EntityPack<PaymentGateway> pack, PaymentGatewayEditingBean bean)
	{
		return (SESSION) new PaymentGatewayEditingSession(sessionId, pack, bean);
	}

	@Override
	protected void populateEditingBean(PaymentGatewayEditingBean gatewayBean, PaymentGateway entity)
	{
		super.populateEditingBean(gatewayBean, entity);
		gatewayBean.setEnabled(!entity.isDisabled());
		gatewayBean.setType(entity.getGatewayType());
	}

	@Override
	protected void populateEntity(PaymentGatewayEditingBean gatewayBean, PaymentGateway entity)
	{
		super.populateEntity(gatewayBean, entity);
		entity.setGatewayType(gatewayBean.getType());
	}

	@Override
	protected void doValidationBean(PaymentGatewayEditingBean gatewayBean, List<ValidationError> errors)
	{
		super.doValidationBean(gatewayBean, errors);
		if( gatewayBean.isEnabled() )
		{
			if( !canEnable(gatewayBean.getType(), gatewayBean.getId()) )
			{
				errors.add(new ValidationError("enabled", CurrentLocale.get(KEY_ENABLED_ERROR, gatewayBean.getType())));
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public boolean canEnable(String gatewayType, long id)
	{
		List<PaymentGateway> enabledByType = payDao.enumerateEnabledByType(gatewayType);
		for( PaymentGateway paymentGateway : enabledByType )
		{
			if( paymentGateway.getId() != id && !paymentGateway.isDisabled() )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public PaymentGateway getForEdit(String uuid)
	{
		PaymentGateway gateway = getByUuid(uuid);
		getExtensionForGateway(gateway.getGatewayType()).loadExtra(gateway);
		return gateway;
	}

	private PaymentGatewayServiceExtension getExtensionForGateway(String type)
	{
		PaymentGatewayServiceExtension extension = typesTracker.getBeanMap().get(type);
		if( extension == null )
		{
			extension = defaultExtension;
		}
		return extension;
	}

	@Override
	public Iterable<GatewayTypeDescriptor> listAllAvailableTypes()
	{
		return getTypeDescriptorList();
	}

	private synchronized Collection<GatewayTypeDescriptor> getTypeDescriptorList()
	{
		if( typesTracker.needsUpdate() || typeDescriptorList == null )
		{
			typeDescriptorList = getTypeDescriptorMap().values();
		}
		return typeDescriptorList;
	}

	private synchronized Map<String, GatewayTypeDescriptor> getTypeDescriptorMap()
	{
		if( typesTracker.needsUpdate() || typeDescriptorMap == null )
		{
			Map<String, GatewayTypeDescriptor> tempTypeDescriptors = new HashMap<String, GatewayTypeDescriptor>();

			for( Extension ext : typesTracker.getExtensions() )
			{
				final String type = ext.getParameter("id").valueAsString();
				final String nameKey = ext.getParameter("nameKey").valueAsString();
				final String descriptionKey = ext.getParameter("descriptionKey").valueAsString();
				// final String node = ext.getParameter("node").valueAsString();

				tempTypeDescriptors.put(type, new GatewayTypeDescriptor(type, nameKey, descriptionKey));
			}

			typeDescriptorMap = Collections.unmodifiableMap(tempTypeDescriptors);
			typeDescriptorList = null;
		}

		return typeDescriptorMap;
	}

	private PaymentGatewayImplementation getImplementation(String type)
	{
		final PaymentGatewayImplementation impl = implTracker.getBeanMap().get(type);
		if( impl == null )
		{
			throw new RuntimeException("No payment gateway implementation for type " + type);
		}
		return impl;
	}

	@Override
	public List<String> getImplementationTypes()
	{
		final List<String> implTypes = new ArrayList<String>();
		for( Map.Entry<String, ?> impl : implTracker.getBeanMap().entrySet() )
		{
			implTypes.add(impl.getKey());
		}
		return implTypes;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		typesTracker = new PluginTracker<PaymentGatewayServiceExtension>(pluginService, PaymentGatewayService.class,
			"paymentGatewayType", "id");
		typesTracker.setBeanKey("serviceClass");

		implTracker = new PluginTracker<PaymentGatewayImplementation>(pluginService, PaymentGatewayService.class,
			"paymentGatewayImplementation", "type");
		implTracker.setBeanKey("class");

		infoTracker = new PluginTracker<PaymentGatewayCheckoutInfoExtension>(pluginService,
			PaymentGatewayService.class, "paymentGatewayCheckoutInfo", "type");
		infoTracker.setBeanKey("class");
	}

	private List<PaymentGateway> getRegionReferences(Region region)
	{
		return payDao.enumerateAllByRegion(region);
	}

	@Override
	public void addRegionReferencingClasses(Region region, List<Class<?>> referencingClasses)
	{
		if( !getRegionReferences(region).isEmpty() )
		{
			referencingClasses.add(PaymentGateway.class);
		}
	}

	@Override
	@Transactional
	public void removeRegionReferences(Region region)
	{
		for( PaymentGateway gateway : getRegionReferences(region) )
		{
			gateway.getRegions().remove(region);
			payDao.save(gateway);
		}
	}

	@Override
	public String testGateway(PaymentGatewayEditingBean gateway)
	{
		return getImplementation(gateway.getType()).testCredentials(gateway);
	}

	@Override
	public PaymentGatewayCheckoutInfo getCheckoutInfo(PaymentGateway gateway)
	{
		final PaymentGatewayCheckoutInfoExtension ext = infoTracker.getBeanMap().get(gateway.getGatewayType());
		if( ext != null )
		{
			return ext.getCheckoutInfo(gateway);
		}
		return null;
	}

	@Override
	public PaymentGateway getEnabledGateway(String type)
	{
		return payDao.findByCriteria(Restrictions.eq("institution", CurrentInstitution.get()),
			Restrictions.eq("gatewayType", type), Restrictions.eq("disabled", false));
	}
}
