import * as OEQ from '../src';
import * as TC from './TestConfig';

beforeEach(() => OEQ.Auth.logout(TC.API_PATH, true));

test("That we're able to login", () =>
  OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD)
    .then((sessionid: string | undefined) => expect(sessionid).toBeTruthy())
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(TC.API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe(TC.USERNAME)
    ));

test('An attempt to login with bad credentials fails', () => {
  expect.assertions(1);
  return OEQ.Auth.login(TC.API_PATH, 'fakeusername', 'fakepassword').catch(
    (error: OEQ.Errors.ApiError) => {
      expect(error.status).toBe(401);
    }
  );
});

test("That having login, we're able to properly log out.", () =>
  OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD)
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(TC.API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe(TC.USERNAME)
    )
    .then(() => OEQ.Auth.logout(TC.API_PATH))
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(TC.API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe('guest')
    ));
