import * as OEQ from '../src';

const API_PATH = 'http://localhost:8080/vanilla/api';
const USERNAME = 'TLE_ADMINISTRATOR';
const PASSWORD = 'abc';

beforeEach(() => OEQ.Auth.logout(API_PATH, true));

test("That we're able to login", () =>
  OEQ.Auth.login(API_PATH, USERNAME, PASSWORD)
    .then((sessionid: string | undefined) => expect(sessionid).toBeTruthy())
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe(USERNAME)
    ));

test('An attempt to login with bad credentials fails', () => {
  expect.assertions(1);
  return OEQ.Auth.login(API_PATH, 'fakeusername', 'fakepassword').catch(
    (error: OEQ.Errors.ApiError) => {
      expect(error.status).toBe(401);
    }
  );
});

test("That having login, we're able to properly log out.", () =>
  OEQ.Auth.login(API_PATH, USERNAME, PASSWORD)
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe(USERNAME)
    )
    .then(() => OEQ.Auth.logout(API_PATH))
    .then(() => OEQ.LegacyContent.getCurrentUserDetails(API_PATH))
    .then((userDetails: OEQ.LegacyContent.CurrentUserDetails) =>
      expect(userDetails.id).toBe('guest')
    ));
