package io.github.openequella.jwks;

import static com.tle.webtests.framework.Assert.assertTrue;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractSessionTest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class JwksServletTest extends AbstractSessionTest {
  final UrlJwkProvider provider = new UrlJwkProvider(getTestConfig().getInstitutionUrl());

  @Test(description = "Retrieve a JWKS for the Institution fiveo which should have 3 public keys")
  public void getJwks() throws JwkException {
    List<Jwk> keys = provider.getAll();

    assertTrue(keys.size() == 3);
  }

  @Test(description = "Retrieve a specific key by key ID and validate the key")
  public void getJwk()
      throws JwkException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final String keyID = "173a5dd5-acb5-4c94-97d6-5aab79a598e9";
    Jwk jwk = provider.get(keyID);

    assertTrue("RS256".equals(jwk.getAlgorithm()));

    // Signature generated with the private key of the above JWK.
    final String signatureBase64 =
        "FuHpbQpr9pXfr71CCWDf08P1x+zXzTIYpnK7wWUqZyygrUld31gMyOVAdk7sr63FgKe3ljX7yqjRP7/6+WMEruVJ5PvcluNVX2xR8pSrElYl3nW0vQWHHYok3ArWVzmUX4+6mKddxGNqrAl81JYPmxpW5udcBe0NZ/1ZBzvPKdi2efe/8D4JxZ/Y2ixS5rNWE2yN1fj+vent3p2ppvJd8wTfFJPBQj8dHJ1/FrkwXtebxQ2/aotiDkGmfRoC7pZAQ99fVf2tthtpFwSDtDtzEpFoP3oOKr+DSkov0STNxjXCgTBmQ8nyh73+/zLEppGOWeV90eZEFcxVj36owf34nA==";
    // Verify the signature.
    Signature sig = Signature.getInstance("SHA256withRSA");
    byte[] signature = Base64.getDecoder().decode(encrypted);
    sig.initVerify(jwk.getPublicKey());

    assertTrue(sig.verify(signature));
  }
}
