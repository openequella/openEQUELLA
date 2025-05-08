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
import * as React from "react";
import { ShuffleBox, ShuffleBoxProps } from "../ShuffleBox";
import { WizardControlBasicProps } from "./WizardHelper";
import { WizardLabel } from "./WizardLabel";

export interface WizardShuffleBoxProps
  extends WizardControlBasicProps,
    ShuffleBoxProps {}

export const WizardShuffleBox = ({
  mandatory,
  label,
  description,
  id,
  options,
  onSelect,
  values,
}: WizardShuffleBoxProps): React.JSX.Element => {
  return (
    <>
      <WizardLabel
        mandatory={mandatory}
        label={label}
        description={description}
        labelFor={id}
      />
      <ShuffleBox
        id={id}
        options={options}
        values={values}
        onSelect={onSelect}
      />
    </>
  );
};
