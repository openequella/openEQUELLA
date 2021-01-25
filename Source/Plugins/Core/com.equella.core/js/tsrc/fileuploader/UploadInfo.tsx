/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Grid, LinearProgress, Typography } from "@material-ui/core";
import * as React from "react";
import {
  isUploadedFile,
  UploadedFile,
  UploadingFile,
} from "../modules/FileUploaderModule";

interface UploadInfoProps {
  /**
   * A file that's either an UploadedFile or UploadingFile
   */
  file: UploadedFile | UploadingFile;
}

/**
 * For an UploadingFile, display a progress bar when the upload is in progress. Show
 * the error message if the upload fails.
 * For an UploadedFile, show nothing.
 */
export const UploadInfo = ({ file }: UploadInfoProps) => {
  if (isUploadedFile(file)) {
    return <div />;
  }
  const { uploadPercentage, status, failedReason } = file;
  return status === "uploading" ? (
    <Grid container alignItems="center" spacing={1}>
      <Grid item xs={10}>
        <LinearProgress variant="determinate" value={uploadPercentage} />
      </Grid>
      <Grid item xs={2}>
        {`${uploadPercentage}%`}
      </Grid>
    </Grid>
  ) : (
    <Typography role="alert" color="error">
      {failedReason}
    </Typography>
  );
};
