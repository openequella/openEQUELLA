package com.tle.com.tle.integration.lti.generic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.integration.lti.generic.GenericLtiWrapperExtension;
import org.apache.struts.mock.MockHttpServletRequest;
import org.junit.Test;

@SuppressWarnings("nls")
public class GenericLtiWrapperExtensionTest {
  @Test
  public void testGetUserIdRequestNull() {
    LtiConsumer consumer = setupLtiConsumerCustomBoth();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(null, consumer);
    assertNull(s);
  }

  @Test
  public void testGetUserIdConsumerNull() {
    MockHttpServletRequest req = setupMockRequestAllValues();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, null);
    assertNull(s);
  }

  @Test
  public void testGetUserIdCustom() {
    MockHttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals("custom-user-id-val", s);
  }

  @Test
  public void testGetUserIdCustomNotInRequest() {
    MockHttpServletRequest req = setupMockRequestOnlyDefault();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals("default-user-id-val", s);
  }

  @Test
  public void testGetUserIdCustomEmpty() {
    MockHttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUserId(req, consumer);
    assertEquals("default-user-id-val", s);
  }

  @Test
  public void testGetUserIdDefaultNotInRequest() {
    MockHttpServletRequest req = setupMockRequestNoValues();

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
    MockHttpServletRequest req = setupMockRequestAllValues();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, null);
    assertNull(s);
  }

  @Test
  public void testGetUsernameCustom() {
    MockHttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals("custom-username-val", s);
  }

  @Test
  public void testGetUsernameCustomNotInRequest() {
    MockHttpServletRequest req = setupMockRequestOnlyDefault();

    LtiConsumer consumer = setupLtiConsumerCustomUsername();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals("default-username-val", s);
  }

  @Test
  public void testGetUsernameCustomEmpty() {
    MockHttpServletRequest req = setupMockRequestAllValues();

    LtiConsumer consumer = setupLtiConsumerCustomId();

    GenericLtiWrapperExtension genericLti = new GenericLtiWrapperExtension();
    final String s = genericLti.getUsername(req, consumer);
    assertEquals("default-username-val", s);
  }

  @Test
  public void testGetUsernameDefaultNotInRequest() {
    MockHttpServletRequest req = setupMockRequestNoValues();

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

  private MockHttpServletRequest setupMockRequestAllValues() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addParameter("mycustomid", "custom-user-id-val");
    req.addParameter("mycustomname", "custom-username-val");
    req.addParameter(ExternalToolConstants.LIS_PERSON_SOURCEDID, "default-username-val");
    req.addParameter(ExternalToolConstants.USER_ID, "default-user-id-val");

    return req;
  }

  private MockHttpServletRequest setupMockRequestOnlyDefault() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addParameter(ExternalToolConstants.LIS_PERSON_SOURCEDID, "default-username-val");
    req.addParameter(ExternalToolConstants.USER_ID, "default-user-id-val");

    return req;
  }

  private MockHttpServletRequest setupMockRequestNoValues() {
    return new MockHttpServletRequest();
  }

  private LtiConsumer setupLtiConsumerCustomBoth() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, "mycustomid");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, "mycustomname");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, true);
    return consumer;
  }

  private LtiConsumer setupLtiConsumerCustomId() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, "mycustomid");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, "");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, false);
    return consumer;
  }

  private LtiConsumer setupLtiConsumerCustomUsername() {
    LtiConsumer consumer = new LtiConsumer();
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USER_ID, "");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_USERNAME, "mycustomname");
    consumer.setAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, false);
    return consumer;
  }
}
