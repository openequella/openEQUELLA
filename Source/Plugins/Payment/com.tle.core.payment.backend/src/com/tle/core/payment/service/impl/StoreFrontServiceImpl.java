package com.tle.core.payment.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.ValidationError;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.common.EntityPack;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.TaxType;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.oauth.dao.OAuthClientDao;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.oauth.service.OAuthClientEditingSession;
import com.tle.core.oauth.service.OAuthClientEditingSessionImpl;
import com.tle.core.oauth.service.OAuthClientReferencesListener;
import com.tle.core.oauth.service.OAuthService;
import com.tle.core.payment.dao.StoreFrontDao;
import com.tle.core.payment.events.listeners.TaxTypeReferencesListener;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.payment.service.session.StoreFrontEditingBean;
import com.tle.core.payment.service.session.StoreFrontEditingSession;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;
import com.tle.core.user.CurrentInstitution;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
@Bind(StoreFrontService.class)
@Singleton
@SecureEntity(StoreFrontService.ENTITY_TYPE)
public class StoreFrontServiceImpl
	extends
		AbstractEntityServiceImpl<StoreFrontEditingBean, StoreFront, StoreFrontService>
	implements
		StoreFrontService,
		OAuthClientReferencesListener,
		TaxTypeReferencesListener
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(StoreFrontService.class);

	private final StoreFrontDao sfDao;
	@Inject
	private OAuthClientDao oauthDao;
	@Inject
	private OAuthService oauthService;
	@Inject
	private RunAsInstitution runAs;

	@Inject
	public StoreFrontServiceImpl(StoreFrontDao sfDao)
	{
		super(Node.STOREFRONT, sfDao);
		this.sfDao = sfDao;
	}

	@Override
	public boolean storeFrontHasHistory(StoreFront storeFront)
	{
		Long count = sfDao.countSalesForStoreFront(storeFront);
		return count > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<StoreFrontEditingBean, StoreFront>> SESSION createSession(
		String sessionId, EntityPack<StoreFront> pack, StoreFrontEditingBean bean)
	{
		return (SESSION) new StoreFrontEditingSession(sessionId, pack, bean);
	}

	@Override
	protected void doValidation(EntityEditingSession<StoreFrontEditingBean, StoreFront> session, StoreFront sf,
		List<ValidationError> errors)
	{
		validateProduct(errors, sf.getProduct(), sf.getProductVersion());
		validateCountry(errors, sf.getCountry());
		validateOAuth(errors, sf.getClient());
	}

	@Override
	protected void doValidationBean(StoreFrontEditingBean sfb, List<ValidationError> errors)
	{
		super.doValidationBean(sfb, errors);
		validateProduct(errors, sfb.getProduct(), sfb.getProductVersion());
		validateCountry(errors, sfb.getCountry());
		validateOAuth(errors, sfb.getClient());
	}

	private void validateProduct(List<ValidationError> errors, String productName, String productVersion)
	{
		if( Strings.isNullOrEmpty(productName) )
		{
			errors.add(new ValidationError(FIELD_PRODUCT_NAME, resources
				.getString("storefront.validation.productname.mandatory")));
		}
		if( Strings.isNullOrEmpty(productVersion) )
		{
			errors.add(new ValidationError(FIELD_PRODUCT_VERSION, resources
				.getString("storefront.validation.productversion.mandatory")));
		}
	}

	private void validateCountry(List<ValidationError> errors, String country)
	{
		if( Strings.isNullOrEmpty(country) )
		{
			errors.add(new ValidationError(FIELD_COUNTRY, resources
				.getString("storefront.validation.country.mandatory")));
		}
		else
		{
			ArrayList<String> countries = Lists.newArrayList(Locale.getISOCountries());
			if( !countries.contains(country) )
			{
				errors.add(new ValidationError(FIELD_COUNTRY, resources
					.getString("storefront.validation.country.invalid")));
			}
		}
	}

	private void validateOAuth(List<ValidationError> errors, OAuthClient client)
	{
		if( client == null )
		{
			// !! This isn't validation, it's a serious error
			throw new RuntimeException("OAuth client was null");
			//
			// errors.add(new ValidationError(FIELD_CLIENT_ENTITY, resources
			// .getString("storefront.validation.cliententity.mandatory")));
		}
		else
		{
			validateOAuthClientId(errors, client.getClientId(), client.getId());
			validateOAuthUrlAndUser(errors, client.getRedirectUrl(), client.getUserId());
		}
	}

	private void validateOAuth(List<ValidationError> errors, OAuthClientEditingBean clientBean)
	{
		validateOAuthClientId(errors, clientBean.getClientId(), clientBean.getId());
		validateOAuthUrlAndUser(errors, clientBean.getRedirectUrl(), clientBean.getUserId());
	}

	private void validateOAuthClientId(List<ValidationError> errors, String clientId, long clientDbId)
	{
		if( Strings.isNullOrEmpty(clientId) )
		{
			errors.add(new ValidationError(FIELD_CLIENT_ID, resources
				.getString("storefront.validation.clientid.mandatory")));
		}
		else
		{
			final OAuthClient client = oauthDao.getByClientIdOnly(clientId);
			if( client != null && client.getId() != clientDbId )
			{
				errors.add(new ValidationError(FIELD_CLIENT_ID, resources
					.getString("storefront.validation.clientid.unique")));
			}
		}
	}

	private void validateOAuthUrlAndUser(List<ValidationError> errors, String redirectUrl, String userId)
	{
		if( Strings.isNullOrEmpty(redirectUrl) )
		{
			errors.add(new ValidationError(FIELD_REDIRECT_URL, resources
				.getString("storefront.validation.redirecturl.mandatory")));
		}

		if( Strings.isNullOrEmpty(userId) )
		{
			errors.add(new ValidationError(FIELD_USER, resources.getString("storefront.validation.user.mandatory")));
		}
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected StoreFrontEditingBean createEditingBean()
	{
		return new StoreFrontEditingBean();
	}

	@Override
	protected void afterDelete(final StoreFront entity)
	{
		// This client is no longer valid
		runAs.executeAsSystem(CurrentInstitution.get(), new Runnable()
		{
			@Override
			public void run()
			{
				oauthService.delete(entity.getClient(), false);
			}
		});
	}

	@Override
	protected void populateEditingBean(StoreFrontEditingBean sfb, StoreFront sf)
	{
		super.populateEditingBean(sfb, sf);
		sfb.setAllowFree(sf.isAllowFree());
		sfb.setAllowPurchase(sf.isAllowPurchase());
		sfb.setAllowSubscription(sf.isAllowSubscription());
		sfb.setContactPhone(sf.getContactPhone());
		sfb.setCountry(sf.getCountry());
		sfb.setProduct(sf.getProduct());
		sfb.setProductVersion(sf.getProductVersion());
		sfb.setEnabled(!sf.isDisabled());
		sfb.setTaxType(sf.getTaxType());

		final OAuthClient client = sf.getClient();
		if( client != null )
		{
			sfb.setClient(convertOAuthClient(client));
		}
	}

	private OAuthClientEditingBean convertOAuthClient(OAuthClient client)
	{
		final OAuthClientEditingBean clientBean = new OAuthClientEditingBean();
		// Non editable, but must keep state
		clientBean.setId(client.getId());
		clientBean.setUuid(client.getUuid());
		clientBean.setName(LangUtils.convertBundleToBean(client.getName()));
		clientBean.setClientSecret(client.getClientSecret());
		clientBean.setDateCreated(client.getDateCreated());
		// Editable
		clientBean.setClientId(client.getClientId());
		clientBean.setRedirectUrl(client.getRedirectUrl());
		clientBean.setUserId(client.getUserId());
		return clientBean;
	}

	@Override
	protected void populateEntity(StoreFrontEditingBean sfb, StoreFront sf)
	{
		super.populateEntity(sfb, sf);
		sf.setAllowFree(sfb.isAllowFree());
		sf.setAllowPurchase(sfb.isAllowPurchase());
		sf.setAllowSubscription(sfb.isAllowSubscription());
		sf.setContactPhone(sfb.getContactPhone());
		sf.setCountry(sfb.getCountry());
		sf.setProduct(sfb.getProduct());
		sf.setProductVersion(sfb.getProductVersion());
		sf.setTaxType(sfb.getTaxType());

		// try to locate the OAuth client
		OAuthClient client = null;
		final OAuthClientEditingBean clientBean = sfb.getClient();
		// set the name on the client
		clientBean.setName(LanguageBundleBean.clone(sfb.getName()));

		if( clientBean.getUuid() != null )
		{
			// update this client!
			client = runAs.executeAsSystem(CurrentInstitution.get(), new Callable<OAuthClient>()
			{
				@Override
				public OAuthClient call() throws Exception
				{
					final OAuthClient updateClient = oauthDao.getByUuid(clientBean.getUuid());
					final EntityPack<OAuthClient> clientPack = new EntityPack<OAuthClient>(updateClient, null);
					final OAuthClientEditingSession oauthSession = new OAuthClientEditingSessionImpl(null, clientPack,
						clientBean);
					oauthService.commitSession(oauthSession);
					return updateClient;
				}
			});
		}
		else
		{
			// create a new client (should it be done here?)
			client = runAs.executeAsSystem(CurrentInstitution.get(), new Callable<OAuthClient>()
			{
				@Override
				public OAuthClient call() throws Exception
				{
					final OAuthClient client = new OAuthClient();
					final EntityPack<OAuthClient> clientPack = new EntityPack<OAuthClient>(client, null);
					clientBean.setUuid(UUID.randomUUID().toString());
					clientBean.setClientSecret(UUID.randomUUID().toString());
					clientBean.setSystemType(true);

					final OAuthClientEditingSession oauthSession = new OAuthClientEditingSessionImpl(null, clientPack,
						clientBean);
					oauthService.commitSession(oauthSession);
					clientBean.setId(client.getId());
					return client;
				}
			});
		}
		sf.setClient(client);
	}

	@Override
	public void addOAuthClientReferencingClasses(OAuthClient client, List<Class<?>> referencingClasses)
	{
		final StoreFront sf = sfDao.getByOAuthClient(client);
		if( sf != null )
		{
			referencingClasses.add(StoreFront.class);
		}
	}

	@Override
	public void addTaxTypeReferencingClasses(TaxType taxType, List<Class<?>> referencingClasses)
	{
		final List<StoreFront> storefronts = sfDao.enumerateForTaxType(taxType);
		if( !storefronts.isEmpty() )
		{
			referencingClasses.add(StoreFront.class);
		}
	}
}
