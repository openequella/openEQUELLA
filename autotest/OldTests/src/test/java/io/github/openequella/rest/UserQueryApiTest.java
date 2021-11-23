package io.github.openequella.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.type.TypeReference;
import org.testng.annotations.Test;

public class UserQueryApiTest extends AbstractRestApiTest {

  private final String USERQUERY_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/userquery";

  private final String GROUPID_AUTOGROUP_1 = "d72eb802-0ea6-4384-907a-341ee60628c0";
  private final String GROUPID_OUTGROUP_1 = "33faa43a-fd68-4145-a022-7162215c70a2";
  private final String USERID_AUTOTEST = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
  private final String USERID_DONOTUSE = "fc9629b0-8bb7-5099-edd3-cf7a42c350fa";

  private final String REASON_UNEXPECTED_NUM_USERS = "Unexpected number of users returned";
  private final String REASON_MISSING_USERS = "Expected users missing!";

  @Test
  public void filter_unfilteredQueryTest() throws IOException {
    // Search for all the users with 'e'
    final List<UserDetails> result = filterEndpointQuery(200, "e", null);
    // ... for which there's many
    assertThat(REASON_UNEXPECTED_NUM_USERS, result.size(), is(8));
  }

  @Test
  public void filter_singleFilterQueryTest() throws IOException {
    // Search for all the users with 'e', but only in the AutoGroup1
    final List<UserDetails> result =
        filterEndpointQuery(200, "e", Collections.singleton(GROUPID_AUTOGROUP_1));
    // ... for which there's only 1
    assertThat(REASON_UNEXPECTED_NUM_USERS, result.size(), is(1));
    assertTrue(REASON_MISSING_USERS, collectUserIds(result).contains(USERID_AUTOTEST));
  }

  @Test
  public void filter_multiFilterQueryTest() throws IOException {
    // Search for all the users with 'e' across two groups
    final List<UserDetails> result =
        filterEndpointQuery(
            200, "e", new HashSet<>(Arrays.asList(GROUPID_AUTOGROUP_1, GROUPID_OUTGROUP_1)));
    assertThat(REASON_UNEXPECTED_NUM_USERS, result.size(), is(2));
    assertTrue(
        REASON_MISSING_USERS,
        collectUserIds(result).containsAll(Arrays.asList(USERID_AUTOTEST, USERID_DONOTUSE)));
  }

  @Test
  public void filter_nothingFoundTest() throws IOException {
    // Search for a user that could never possibly exist
    filterEndpointQuery(404, "This user could never possibly exist", null);
  }

  private List<UserDetails> filterEndpointQuery(int expectedCode, String q, Set<String> byGroups)
      throws IOException {
    final HttpMethod method = new GetMethod(USERQUERY_API_ENDPOINT + "/filtered");
    final List<NameValuePair> queryParams = new LinkedList<>();

    // The query is kind of mandatory, so always set
    queryParams.add(new NameValuePair("q", q));
    // If one or more filters have been specified, add them too
    Optional.ofNullable(byGroups)
        .orElse(Collections.emptySet())
        .forEach(uuid -> queryParams.add(new NameValuePair("byGroups", uuid)));

    method.setQueryString(queryParams.toArray(new NameValuePair[0]));

    int statusCode = makeClientRequest(method);
    assertThat("Unexpected response from server", statusCode, is(expectedCode));

    return statusCode == 200
        ? mapper.readValue(
            method.getResponseBodyAsString(), new TypeReference<List<UserDetails>>() {})
        : Collections.emptyList();
  }

  private Set<String> collectUserIds(List<UserDetails> users) {
    return users.stream().map(UserDetails::getId).collect(Collectors.toSet());
  }

  // Mirror of com.tle.web.api.users.UserDetails
  private static class UserDetails {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }
}
