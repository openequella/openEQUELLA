import { Checkbox, Grid } from "@material-ui/core";
import SearchSettingFormControl from "../../../components/SearchSettingFormControl";
import * as React from "react";
import { languageStrings } from "../../../util/langstrings";
import { SearchSettings } from "../SearchSettingsModule";

export interface GalleryViewsSettingsProps {
  searchSettings: SearchSettings;
  disabled: boolean;
  handleSearchSettingsChange: (
    key: keyof SearchSettings,
    value: boolean
  ) => void;
}
export default function GalleryViewsSettings(props: GalleryViewsSettingsProps) {
  const searchPageSettingsStrings =
    languageStrings.settings.searching.searchPageSettings;
  const { searchSettings, disabled, handleSearchSettingsChange } = props;
  return (
    <Grid container direction={"column"} spacing={8}>
      <Grid item>
        <SearchSettingFormControl
          control={
            <Checkbox
              id={"_disableGallery"}
              checked={searchSettings.searchingDisableGallery}
            />
          }
          disabled={disabled}
          title={searchPageSettingsStrings.galleryViews}
          onChange={(_, checked) =>
            handleSearchSettingsChange("searchingDisableGallery", checked)
          }
          label={searchPageSettingsStrings.disableImages}
        />
      </Grid>
      <Grid item>
        <SearchSettingFormControl
          control={
            <Checkbox
              id={"_disableVideo"}
              checked={searchSettings.searchingDisableVideos}
            />
          }
          disabled={disabled}
          onChange={(_, checked) =>
            handleSearchSettingsChange("searchingDisableVideos", checked)
          }
          label={searchPageSettingsStrings.disableVideos}
        />
      </Grid>
      <Grid item>
        <SearchSettingFormControl
          control={
            <Checkbox
              id={"_disableFileCount"}
              checked={searchSettings.fileCountDisabled}
            />
          }
          disabled={disabled}
          onChange={(_, checked) =>
            handleSearchSettingsChange("fileCountDisabled", checked)
          }
          label={searchPageSettingsStrings.disableFileCount}
        />
      </Grid>
    </Grid>
  );
}
