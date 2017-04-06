import time
import urllib
import binascii
import hashlib



def urlEncode(text):
	return urllib.urlencode ({'q': text}) [2:]

# Generate an equella token for logging in
def generateToken(username, sharedSecretId, sharedSecretValue):
	seed = str (int (time.time ())) + '000'
	id2 = urlEncode (sharedSecretId)
	if(not(sharedSecretId == '')):
		id2 += ':'

	return '%s:%s%s:%s' % (
			urlEncode(username),
			id2,
			seed,
			binascii.b2a_base64(hashlib.md5(
				username + sharedSecretId + seed + sharedSecretValue
			).digest())
		)
