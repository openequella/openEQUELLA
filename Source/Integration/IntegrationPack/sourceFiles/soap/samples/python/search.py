import settings
import equellasoap
from util import generateToken
from util import urlEncode

equella = equellasoap.EquellaSoap(settings.institutionUrl, settings.username, settings.password, proxyUrl = settings.proxyUrl)

results = equella.searchItems('test')

tokenPostfix = ''
if settings.useTokens == True:
	tokenPostfix = '?token=%s' % urlEncode( generateToken(settings.tokenUser, settings.sharedSecretId, settings.sharedSecretValue) )

for result in results.iterate('result/xml/item'):
	print '%s: %s%s' % (result.getNode('name'), result.getNode('url'), tokenPostfix)

equella.logout()