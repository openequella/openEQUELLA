import { createMuiTheme } from "@material-ui/core";
import { ThemeOptions } from "@material-ui/core/styles/createMuiTheme";

export interface IThemeSettings {
  primaryColor: string;
  secondaryColor: string;
  backgroundColor: string;
  menuItemColor: string;
  menuItemTextColor: string;
  menuItemIconColor: string;
  primaryTextColor: string;
  menuTextColor: string;
  fontSize: number;
}

declare const themeSettings: IThemeSettings;

export const oeqTheme = createMuiTheme({
  palette: {
    primary: {
      main: themeSettings.primaryColor
    },
    secondary: {
      main: themeSettings.secondaryColor
    },
    background: {
      default: themeSettings.backgroundColor
    },
    text: {
      primary: themeSettings.primaryTextColor,
      secondary: themeSettings.menuTextColor
    },
    menu: {
      text: themeSettings.menuItemTextColor,
      icon: themeSettings.menuItemIconColor,
      background: themeSettings.menuItemColor
    }
  },
  typography: {
    useNextVariants: true,
    fontSize: themeSettings.fontSize
  }
} as ThemeOptions);
