import * as OEQ from '../src';
import * as TC from './TestConfig';

beforeEach(() => OEQ.Auth.logout(TC.API_PATH, true));

test("That we're able to login", async () => {
  const sessionid = await OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD);
  expect(sessionid).toBeTruthy();
  const userDetails = await OEQ.LegacyContent.getCurrentUserDetails(
    TC.API_PATH
  );
  expect(userDetails).toHaveProperty('id', TC.USERNAME);
});

test('An attempt to login with bad credentials fails', () => {
  await expect(
    OEQ.Auth.login(TC.API_PATH, 'fakeusername', 'fakepassword')
  ).rejects.toHaveProperty('status', 401);
});

test("That having logged in, we're able to properly log out.", async () => {
  await OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD);
  const userDetails = await OEQ.LegacyContent.getCurrentUserDetails(
    TC.API_PATH
  );
  expect(userDetails).toHaveProperty('id', TC.USERNAME);

  await OEQ.Auth.logout(TC.API_PATH);
  const guestDetails = await OEQ.LegacyContent.getCurrentUserDetails(
    TC.API_PATH
  );
  expect(guestDetails).toHaveProperty('id', 'guest');
});
