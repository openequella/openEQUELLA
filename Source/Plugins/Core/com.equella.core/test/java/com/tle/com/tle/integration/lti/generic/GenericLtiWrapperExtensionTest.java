package com.tle.com.tle.integration.lti.generic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.integration.lti.generic.GenericLtiWrapperExtension;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;

@SuppressWarnings("nls")
public class GenericLtiWrapperExtensionTest {
  private static final String USERID_CUSTOM = "custom-user-id-val";
  private static final String USERID_DEFAULT = "default-user-id-val";
  private static final String USERNAME_CUSTOM = "custom-username-val";
  private static final String USERNAME_DEFAULT = "default-username-val";

  @Test
  public void testGetUserIdRequestNull() {
    LtiConsumer consumer = setupLtiConsumerCustomBoth();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(null, consumer);
    assertNull(s);
  }

  @Test
  public void testGetUserIdConsumerNull() {
    HttpServletRequest req = setupMockRequestAllValues();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, null);
    assertNull(s);
  }

  @Test
  public void testGetUserIdCustom() {
    HttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals(USERID_CUSTOM, s);
  }

  @Test
  public void testGetUserIdCustomNotInRequest() {
    HttpServletRequest req = setupMockRequestOnlyDefault();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals(USERID_DEFAULT, s);
  }

  @Test
  public void testGetUserIdCustomEmpty() {
    HttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals(USERID_DEFAULT, s);
  }

  @Test
  public void testGetUserIdDefaultNotInRequest() {
    HttpServletRequest req = setupMockRequestNoValues();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertNull(s);
  }

  @Test
  public void testGetUsernameRequestNull() {
    LtiConsumer consumer = setupLtiConsumerCustomBoth();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(null, consumer);
    assertNull(s);
  }

  @Test
  public void testGetUsernameConsumerNull() {
    HttpServletRequest req = setupMockRequestAllValues();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, null);
    assertNull(s);
  }

  @Test
  public void testGetUsernameCustom() {
    HttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals(USERNAME_CUSTOM, s);
  }

  @Test
  public void testGetUsernameCustomNotInRequest() {
    HttpServletRequest req = setupMockRequestOnlyDefault();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals(USERNAME_DEFAULT, s);
  }

  @Test
  public void testGetUsernameCustomEmpty() {
    HttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals(USERNAME_DEFAULT, s);
  }

  @Test
  public void testGetUsernameDefaultNotInRequest() {
    HttpServletRequest req = setupMockRequestNoValues();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertNull(s);
  }

  @Test
  public void testIsPrefixUserIdFromNullConsumer() {
    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();

    final boolean b = genericLti.isPrefixUserId(null);
    assertTrue(b);
  }

  @Test
  public void testIsPrefixUserIdTrue() {
    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();

    LtiConsumer consumer = setupLtiConsumerCustomBoth();

    final boolean b = genericLti.isPrefixUserId(consumer);
    assertTrue(b);
  }

  @Test
  public void testIsPrefixUserIdDefault() {
    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();

    LtiConsumer consumer = setupLtiConsumerCustomBoth();

    final boolean b = genericLti.isPrefixUserId(consumer);
    assertTrue(b);
  }

  @Test
  public void testIsPrefixUserIdFalse() {
    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    final boolean b = genericLti.isPrefixUserId(consumer);
    assertFalse(b);
  }

  private HttpServletRequest setupMockRequestNoValues() {
    return mock(HttpServletRequest.class);
  }

  private HttpServletRequest setupMockRequestOnlyDefault() {
    HttpServletRequest req = setupMockRequestNoValues();
    when(req.getParameter(ExternalToolConstants.LIS_PERSON_SOURCEDID)).thenReturn(USERNAME_DEFAULT);
    when(req.getParameter(ExternalToolConstants.USER_ID)).thenReturn(USERID_DEFAULT);

    return req;
  }

  private HttpServletRequest setupMockRequestAllValues() {
    HttpServletRequest req = setupMockRequestOnlyDefault();
    when(req.getParameter("mycustomid")).thenReturn(USERID_CUSTOM);
    when(req.getParameter("mycustomname")).thenReturn(USERNAME_CUSTOM);

    return req;
  }

  private static final String ATT_CUSTOM_ID = "mycustomid";
  private static final String ATT_CUSTOM_NAME = "mycustomname";

  private LtiConsumer setupLtiConsumerCustomBoth() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, ATT_CUSTOM_ID);
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, ATT_CUSTOM_NAME);
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, true);
    return consumer;
  }

  private LtiConsumer setupLtiConsumerCustomId() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, ATT_CUSTOM_ID);
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, "");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, false);
    return consumer;
  }

  private LtiConsumer setupLtiConsumerCustomUsername() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, "");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, ATT_CUSTOM_NAME);
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, false);
    return consumer;
  }
}
