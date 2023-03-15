package io.github.openequella.jwks;

import static com.tle.webtests.framework.Assert.assertTrue;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.auth0.jwk.UrlJwkProvider;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractSessionTest;
import java.util.List;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;

@TestInstitution("fiveo")
public class JwksServletTest extends AbstractSessionTest {
  @Test(description = "Retrieve a JWKS for the Institution fiveo which should have 3 public keys")
  @NewUIOnly
  public void getJwks() throws SigningKeyNotFoundException {
    UrlJwkProvider provider = new UrlJwkProvider(getTestConfig().getInstitutionUrl());
    List<Jwk> keys = provider.getAll();

    assertTrue(keys.size() == 3);
  }
}
