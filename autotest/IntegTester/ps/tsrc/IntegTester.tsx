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
import { useState } from "react";
import * as React from "react";
import * as ReactDOM from "react-dom";
import {
  TextField,
  Checkbox,
  TextareaAutosize,
  Select,
  MenuItem,
  Grid,
  FormControlLabel,
  Button,
  Card,
  CardHeader,
  CardContent,
} from "@mui/material";
import { md5 } from "js-md5";

type Action =
  | "contribute"
  | "searchResources"
  | "selectOrAdd"
  | "searchThin"
  | "structured";
const actions: Action[] = [
  "contribute",
  "searchResources",
  "selectOrAdd",
  "searchThin",
  "structured",
];

type Method = "lms" | "vista";
const methods: Method[] = ["lms", "vista"];

interface IntegTesterProps {
  method: Method;
  action: Action;
  url: string;
  options: string;
  username: string;
  sharedSecret: string;
  sharedSecretId: string;
  courseId: string;
  itemOnly: boolean;
  packageOnly: boolean;
  attachmentOnly: boolean;
  selectMultiple: boolean;
  useDownloadPrivilege: boolean;
  forcePost: boolean;
  cancelDisabled: boolean;
  attachmentUuidUrls: boolean;
  makeReturn: boolean;
  itemXml: string;
  powerXml: string;
  clickUrl?: string;
}

const defaultConfiguration: IntegTesterProps = {
  method: "lms",
  action: "searchResources",
  options: "",
  url: "",
  username: "",
  sharedSecret: "",
  sharedSecretId: "",
  courseId: "",
  itemOnly: false,
  packageOnly: false,
  attachmentOnly: false,
  selectMultiple: false,
  useDownloadPrivilege: false,
  forcePost: false,
  cancelDisabled: false,
  attachmentUuidUrls: false,
  makeReturn: false,
  itemXml: "",
  powerXml: "",
};

interface ReturnProps
  extends Omit<
    IntegTesterProps,
    "username" | "sharedSecretId" | "sharedSecret"
  > {
  token?: string;
  returnurl?: string;
  returnprefix?: string;
  links?: string;
}
// variable dynamically created by IntegTester.
declare const postValues: ReturnProps;

const buildTextBoxControl = (
  label: string,
  name: string,
  onChange: (value: string) => void,
): React.JSX.Element => (
  <Grid item xs={3}>
    <TextField
      name={name}
      onChange={(e) => onChange(e.target.value)}
      label={label}
    />
  </Grid>
);

const buildCheckboxControl = (
  label: string,
  name: string,
  onChange: (value: boolean) => void,
): React.JSX.Element => (
  <Grid item xs={3}>
    <FormControlLabel
      control={
        <Checkbox name={name} onChange={(e) => onChange(e.target.checked)} />
      }
      label={label}
    />
  </Grid>
);

const buildTextareaControl = (
  label: string,
  name: string,
  onChange: (value: string) => void,
  value?: string,
): React.JSX.Element => (
  <Grid item xs={12}>
    <TextareaAutosize
      name={name}
      placeholder={label}
      onChange={(e) => onChange(e.target.value)}
      minRows={3}
      style={{ width: "90%" }}
      value={value}
    />
  </Grid>
);

const IntegTester = (props: IntegTesterProps) => {
  const [configuration, setConfiguration] = useState<IntegTesterProps>(props);
  const [form, setForm] = useState<React.JSX.Element>();

  const Method = (
    <Grid item xs={3}>
      <Select
        value={configuration.method}
        name="method"
        onChange={(v) =>
          setConfiguration({
            ...configuration,
            method: v.target.value as Method,
          })
        }
      >
        {methods.map((m) => (
          <MenuItem value={m} key={m}>
            {m}
          </MenuItem>
        ))}
      </Select>
    </Grid>
  );

  const Action = (
    <Grid item xs={3}>
      <Select
        id="select-action"
        value={configuration.action}
        name="action"
        onChange={(v) =>
          setConfiguration({
            ...configuration,
            action: v.target.value as Action,
          })
        }
      >
        {actions.map((m) => (
          <MenuItem value={m} key={m}>
            {m}
          </MenuItem>
        ))}
      </Select>
    </Grid>
  );

  const Options = buildTextBoxControl("Options", "options", (value: string) =>
    setConfiguration({ ...configuration, options: value }),
  );

  const URL = buildTextBoxControl("URL", "url", (value: string) =>
    setConfiguration({ ...configuration, url: value }),
  );

  const Username = buildTextBoxControl(
    "Username",
    "username",
    (value: string) => setConfiguration({ ...configuration, username: value }),
  );

  const SharedSecret = buildTextBoxControl(
    "Shared Secret",
    "sharedSecret",
    (value: string) =>
      setConfiguration({ ...configuration, sharedSecret: value }),
  );

  const SharedSecretId = buildTextBoxControl(
    "Shared Secret ID",
    "sharedSecretId",
    (value: string) =>
      setConfiguration({ ...configuration, sharedSecretId: value }),
  );

  const CourseId = buildTextBoxControl(
    "Course ID",
    "courseId",
    (value: string) => setConfiguration({ ...configuration, courseId: value }),
  );

  const ItemOnly = buildCheckboxControl(
    "Select Items only",
    "itemOnly",
    (value: boolean) => setConfiguration({ ...configuration, itemOnly: value }),
  );

  const PackagesOnly = buildCheckboxControl(
    "Select Package only",
    "packageOnly",
    (value: boolean) =>
      setConfiguration({ ...configuration, packageOnly: value }),
  );

  const AttachmentsOnly = buildCheckboxControl(
    "Select Attachments only",
    "attachmentOnly",
    (value: boolean) =>
      setConfiguration({ ...configuration, attachmentOnly: value }),
  );

  const SelectMultiple = buildCheckboxControl(
    "Select Multiple",
    "selectMultiple",
    (value: boolean) =>
      setConfiguration({ ...configuration, selectMultiple: value }),
  );

  const UseDownloadPrivilege = buildCheckboxControl(
    "Use Download Privilege",
    "useDownloadPrivilege",
    (value: boolean) =>
      setConfiguration({ ...configuration, useDownloadPrivilege: value }),
  );

  const ForcePost = buildCheckboxControl(
    "Force Post return",
    "forcePost",
    (value: boolean) =>
      setConfiguration({ ...configuration, forcePost: value }),
  );

  const CancelDisabled = buildCheckboxControl(
    "Disabling cancelling",
    "cancelDisabled",
    (value: boolean) =>
      setConfiguration({ ...configuration, cancelDisabled: value }),
  );

  const AttachmentUuidUrls = buildCheckboxControl(
    "Generate ?attachment.uuid=abcd URLs",
    "attachmentUuidUrls",
    (value: boolean) =>
      setConfiguration({ ...configuration, attachmentUuidUrls: value }),
  );

  const MakeReturn = buildCheckboxControl(
    "Generate Return URL",
    "makeReturn",
    (value: boolean) =>
      setConfiguration({ ...configuration, makeReturn: value }),
  );

  const ItemXml = buildTextareaControl(
    "Initial item XML",
    "itemXml",
    (value: string) => setConfiguration({ ...configuration, itemXml: value }),
  );

  const PowerXml = buildTextareaControl(
    "Initial powersearch XML",
    "powerXml",
    (value: string) => setConfiguration({ ...configuration, powerXml: value }),
  );

  const buildForm = () => {
    const now = Date.now();
    console.log(`Username ${configuration.username}`);
    console.log(`secret ${configuration.sharedSecret}`);
    console.log(`secret ID ${configuration.sharedSecretId}`);
    console.log(
      `token ${configuration.username}${configuration.sharedSecretId}${now}${configuration.sharedSecret}`,
    );
    const encoded = md5.base64(
      `${configuration.username}${configuration.sharedSecretId}${now}${configuration.sharedSecret}`,
    );
    const token = `${configuration.username}:${configuration.sharedSecretId}:${now}:${encoded}`;
    const returnUrl = `${window.location.host}${window.location.pathname}?method=showReturn`;
    const urlParams = new URLSearchParams();

    urlParams.set("token", token);
    urlParams.set("method", configuration.method);
    urlParams.set("action", configuration.action);
    urlParams.set("returnprefix", "");
    urlParams.set("returnurl", returnUrl);

    const booleanFields: [string, boolean | undefined][] = [
      ["selectMultiple", configuration.selectMultiple],
      ["itemonly", configuration.itemOnly],
      ["attachmentonly", configuration.attachmentOnly],
      ["packageonly", configuration.packageOnly],
      ["useDownloadPrivilege", configuration.useDownloadPrivilege],
      ["forcePost", configuration.forcePost],
      ["cancelDisabled", configuration.cancelDisabled],
      ["attachmentUuidUrls", configuration.attachmentUuidUrls],
    ];
    booleanFields.forEach(([field, value]) =>
      urlParams.append(field, value?.toString() ?? "false"),
    );

    [
      ["options", configuration.options],
      ["courseId", configuration.courseId],
    ].forEach(([field, value]) => urlParams.append(field, value));

    const fullUrl = `${configuration.url}?${urlParams.toString()}`;
    setForm(
      <form method="POST" action={fullUrl}>
        <input type="hidden" name="itemXml" value={configuration.itemXml} />
        <input type="hidden" name="powerXml" value={configuration.powerXml} />
        <a href={fullUrl}></a>
        <Button type="submit" variant="outlined">
          Submit
        </Button>
      </form>,
    );
  };

  const returnedValues = Object.entries(postValues);

  return (
    <Grid container spacing={2}>
      {Method}
      {Action}
      {Options}
      {URL}
      {Username}
      {SharedSecret}
      {SharedSecretId}
      {CourseId}
      {ItemOnly}
      {PackagesOnly}
      {AttachmentsOnly}
      {SelectMultiple}
      {UseDownloadPrivilege}
      {ForcePost}
      {CancelDisabled}
      {AttachmentUuidUrls}
      {MakeReturn}
      {ItemXml}
      {PowerXml}
      <Grid item xs={12}>
        <Button variant="outlined" onClick={buildForm}>
          Generate request URL
        </Button>
      </Grid>
      <Grid item xs={12}>
        {form}
      </Grid>
      {returnedValues.length > 0 && (
        <Grid item xs={12}>
          <Card>
            <CardHeader title="IntegTester Return Info" />
            <CardContent>
              <Grid container spacing={2}>
                {returnedValues.map(([field, value]) => (
                  <>
                    <Grid item xs={2}>
                      {field}
                    </Grid>
                    <Grid item xs={10}>
                      {buildTextareaControl(field, field, () => {}, value)}
                    </Grid>
                  </>
                ))}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      )}
    </Grid>
  );
};

ReactDOM.render(
  <IntegTester {...defaultConfiguration} />,
  document.getElementById("app"),
);
