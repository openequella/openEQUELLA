import * as OEQ from '../src';
import * as TC from './TestConfig';
import { GeneralSettings, UISettings } from '../src/Settings';

beforeAll(() => OEQ.Auth.login(TC.API_PATH, TC.USERNAME, TC.PASSWORD));

afterAll(() => OEQ.Auth.logout(TC.API_PATH, true));

test('Able to retrieve general settings', () =>
  OEQ.Settings.getGeneralSettings(
    TC.API_PATH
  ).then((settings: GeneralSettings[]) =>
    expect(settings.length).toBeGreaterThan(0)
  ));

describe('UI Settings', () => {
  let settingsAtStart: UISettings;
  beforeAll(() =>
    OEQ.Settings.getUiSettings(TC.API_PATH).then(
      (settings: UISettings) => (settingsAtStart = settings)
    )
  );

  afterAll(() => OEQ.Settings.updateUiSettings(TC.API_PATH, settingsAtStart));

  it('Should be possible to retrieve the UI settings', () =>
    expect(settingsAtStart).toBeTruthy());

  it('Should be possible to change the settings', () =>
    OEQ.Settings.updateUiSettings(TC.API_PATH, {
      newUI: {
        ...settingsAtStart.newUI,
        enabled: !settingsAtStart.newUI.enabled,
      },
    })
      .then(() => OEQ.Settings.getUiSettings(TC.API_PATH))
      .then((settings: UISettings) =>
        expect(settings.newUI.enabled).toEqual(!settingsAtStart.newUI.enabled)
      ));
});
