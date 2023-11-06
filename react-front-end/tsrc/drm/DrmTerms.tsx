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
import { Grid, Typography } from "@mui/material";
import { styled } from "@mui/material/styles";
import * as OEQ from "@openequella/rest-api-client";
import { pipe } from "fp-ts/function";
import * as O from "fp-ts/Option";
import * as React from "react";

const StyledLi = styled("li")(({ theme }) => ({
  marginTop: theme.spacing(1),
  marginBottom: theme.spacing(1),
}));

/**
 * Child component for displaying non-standard DRM term which usually has a title and a list of terms.
 */
export const NonStandardDrmTerms = ({
  title,
  terms,
}: {
  title: string;
  terms: string[];
}) => {
  return (
    <StyledLi key={title}>
      <Grid container direction="column">
        <Grid item>
          <Typography>{title}</Typography>
        </Grid>
        <Grid item>
          {terms.map((term) => (
            <Typography key={term}>{term}</Typography>
          ))}
        </Grid>
      </Grid>
    </StyledLi>
  );
};

/**
 * Child component for displaying all DRM terms.
 */
export const DrmTerms = ({
  regularPermission,
  additionalPermission,
  educationSector,
  parties,
  customTerms,
}: OEQ.Drm.DrmAgreements) => {
  const standardTerms = [
    regularPermission,
    additionalPermission,
    educationSector,
  ]
    .filter((term) => term !== undefined)
    .map((term) => (
      <StyledLi key={term}>
        <Typography>{term}</Typography>
      </StyledLi>
    ));

  const buildNonStandardDrmTerms = <T,>(
    terms: T | undefined,
    transformer: (p: T) => {
      title: string;
      terms: string[];
    },
  ) =>
    pipe(
      terms,
      O.fromNullable,
      O.map(transformer),
      O.map(({ title, terms }) => (
        <NonStandardDrmTerms title={title} terms={terms} />
      )),
      O.toUndefined,
    );

  const partyDetails = buildNonStandardDrmTerms<OEQ.Drm.DrmParties>(
    parties,
    ({ title, partyList }: OEQ.Drm.DrmParties) => ({
      title,
      terms: partyList,
    }),
  );

  const customTermDetails = buildNonStandardDrmTerms<OEQ.Drm.DrmCustomTerms>(
    customTerms,
    ({ title, terms: customTerms }: OEQ.Drm.DrmCustomTerms) => ({
      title,
      terms: customTerms.split("\n"),
    }),
  );

  return (
    <ol className="skip-css-reset" start={1}>
      {standardTerms}
      {partyDetails}
      {customTermDetails}
    </ol>
  );
};
