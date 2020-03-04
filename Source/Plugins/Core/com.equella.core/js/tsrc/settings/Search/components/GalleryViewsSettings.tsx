import { Grid } from "@material-ui/core";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { SearchSettings } from "../SearchSettingsModule";
import SettingsCheckbox from "../../../components/SettingsCheckbox";

export interface GalleryViewsSettingsProps {
  disabled: boolean;
  searchSettings: SearchSettings;
  setSearchSettings: (searchSettings: SearchSettings) => void;
}
export default function GalleryViewsSettings(props: GalleryViewsSettingsProps) {
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const { searchSettings, disabled, setSearchSettings } = props;
  return (
    <Grid container direction={"column"} spacing={8}>
      <Grid item>
        <SettingsCheckbox
          value={searchSettings.searchingDisableGallery}
          setValue={value =>
            setSearchSettings({
              ...searchSettings,
              searchingDisableGallery: value
            })
          }
          disabled={disabled}
          title={searchPageSettingsStrings.galleryViews}
          label={searchPageSettingsStrings.disableImages}
          id={"_disableGallery"}
        />
      </Grid>
      <Grid item>
        <SettingsCheckbox
          value={searchSettings.searchingDisableVideos}
          setValue={value =>
            setSearchSettings({
              ...searchSettings,
              searchingDisableVideos: value
            })
          }
          disabled={disabled}
          label={searchPageSettingsStrings.disableVideos}
          id={"_disableVideo"}
        />
      </Grid>
      <Grid item>
        <SettingsCheckbox
          value={searchSettings.fileCountDisabled}
          setValue={value =>
            setSearchSettings({ ...searchSettings, fileCountDisabled: value })
          }
          disabled={disabled}
          label={searchPageSettingsStrings.disableFileCount}
          id={"_disableFileCount"}
        />
      </Grid>
    </Grid>
  );
}
