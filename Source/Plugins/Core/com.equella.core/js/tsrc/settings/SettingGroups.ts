import { languageStrings } from "../util/langstrings";
import { GeneralSetting } from "./SettingsPageEntry";

interface SettingCategory {
  name: string;
  desc: string;
}

export interface SettingGroup {
  category: SettingCategory;
  settings: GeneralSetting[];
}

// Group settings by their category and sort each group by setting name
export const groupMap = (settings: GeneralSetting[]): SettingGroup[] => {
  const settingCategories: { [key: string]: SettingCategory } =
    languageStrings.settings;
  return Object.keys(settingCategories).map(key => {
    const settingsOfCategory = settings
      .filter(setting => setting.group === key)
      .sort((s1, s2) => {
        return s1.name > s2.name ? 1 : -1;
      });
    return { category: settingCategories[key], settings: settingsOfCategory };
  });
};
