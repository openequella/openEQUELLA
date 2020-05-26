import * as OEQ from '../src';
import * as TC from './TestConfig';
import { UISettings } from '../src/Settings';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

test("That we're able to retrieve general settings", async () => {
  const settings = await OEQ.Settings.getGeneralSettings(TC.API_PATH);
  expect(settings.length).toBeGreaterThan(0);
});

describe('UI Settings', () => {
  let settingsAtStart: UISettings;
  beforeAll(async () => {
    settingsAtStart = await OEQ.Settings.getUiSettings(TC.API_PATH);
  });

  afterAll(() => OEQ.Settings.updateUiSettings(TC.API_PATH, settingsAtStart));

  it('Should be possible to retrieve the UI settings', () =>
    expect(settingsAtStart).toBeTruthy());

  it('Should be possible to change the settings', async () => {
    await OEQ.Settings.updateUiSettings(TC.API_PATH, {
      newUI: {
        ...settingsAtStart.newUI,
        enabled: !settingsAtStart.newUI.enabled,
      },
    });
    const settings = await OEQ.Settings.getUiSettings(TC.API_PATH);
    expect(settings.newUI.enabled).toEqual(!settingsAtStart.newUI.enabled);
  });
});
