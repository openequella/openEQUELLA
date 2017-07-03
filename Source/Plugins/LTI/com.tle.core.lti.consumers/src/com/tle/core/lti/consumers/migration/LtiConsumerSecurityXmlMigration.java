package com.tle.core.lti.consumers.migration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.lti.consumers.service.LtiConsumerService;

/**
 * @author Aaron
 *
 */
@Bind
@Singleton
public class LtiConsumerSecurityXmlMigration extends XmlMigrator
{
	@Inject
	private EncryptionService encryptionService;
	@Inject
	private LtiConsumerService ltiConsumerService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		// LtiConsumer secrets
		final SubTemporaryFile ltifolder = new SubTemporaryFile(staging, "lticonsumer");
		final List<String> ltientries = xmlHelper.getXmlFileList(ltifolder);
		for( String entry : ltientries )
		{
			LtiConsumer consumer = (LtiConsumer) xmlHelper.readXmlFile(ltifolder, entry,
				ltiConsumerService.getXStream());
			String encpwd = encryptionService.encrypt(consumer.getConsumerSecret());
			consumer.setConsumerSecret(encpwd);
			xmlHelper.writeXmlFile(ltifolder, entry, consumer);
		}
	}
}
