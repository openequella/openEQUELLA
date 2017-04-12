
#An example institution URL of http://www.myhost.com/path/to/institution/ will be broken down us such:
institutionUrl = 'http://www.myhost.com/path/to/institution/'
username = 'username'
password = 'password'

#use this if your network connection requires use of proxy.  if the proxy requires authentication the url will of the form http://username:password@proxyurl:proxyport
proxyUrl = None

#You can specify useTokens = True to append a single-sign-on token to the results of the search result URLs.  
#Note that to use this functionality, the Shared Secrets user management plugin must be enabled (see User Management in the EQUELLA Administration Console)
#and a shared secret must be configured.
useTokens = False
tokenUser = 'someUsername'
sharedSecretId = 'someSecretId'
sharedSecretValue = 'someSecretValue'