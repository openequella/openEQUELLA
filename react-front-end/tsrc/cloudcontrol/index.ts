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
import Axios from "axios";

import {
  Attachment,
  CloudControlRegister,
  ControlApi,
  ControlParameters,
  ControlValidator,
  FileEntries,
  ItemCommand,
  ItemCommandResponse,
  ItemState,
} from "oeq-cloudproviders/controls";

const wgxpath = require("wicked-good-xpath");
wgxpath.install(window);

interface ItemStateJSON {
  xml: string;
  attachments: Attachment[];
  files: FileEntries;
  stateVersion: number;
}

interface VersionedItemState extends ItemState {
  stateVersion: number;
}

interface ItemEdit {
  xml?: string;
  edits: ItemCommand[];
}

interface WizardIds {
  wizId: string;
  stagingId: string;
  userId: string;
}

interface CloudControlRegisterImpl extends CloudControlRegister {
  createRender: <T extends object = object>(
    data: WizardIds,
  ) => (params: ControlApi<T>) => void;

  forceReload(): void;

  sendBatch(state: VersionedItemState): Promise<VersionedItemState>;
}

interface ItemCommandResponses {
  xml: string;
  results: ItemCommandResponse[];
}

interface CommandsPromise {
  commands: ItemCommand[];
  resolved: (response: ItemCommandResponse[]) => void;
  rejected: (err: Error) => void;
}

interface Registration {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  mount: (api: ControlApi<any>) => void;
  unmount: (removed: Element) => void;
}

const registrations: {
  [key: string]: Registration | undefined;
} = {};

let listeners: ((doc: ItemState) => void)[] = [];
let controlValidators: { validator: ControlValidator; ctrlId: string }[] = [];
let commandQueue: CommandsPromise[] = [];
let transformState: ((doc: XMLDocument) => XMLDocument) | null = null;
let reloadState = false;
let wizardIds: WizardIds;
let currentState: Promise<VersionedItemState>;
let latestXml: XMLDocument;

function resetGlobalState() {
  transformState = null;
  reloadState = false;
  commandQueue = [];
  controlValidators = [];
  listeners = [];

  currentState = null!;

  latestXml = null!;
}

const parser = new DOMParser();
const serializer = new XMLSerializer();

let activeElements: {
  element: Element;
  removed: Registration;
}[] = [];

function wizardUri(path: string): string {
  return "api/wizard/" + encodeURIComponent(wizardIds.wizId) + "/" + path;
}

async function getState(): Promise<VersionedItemState> {
  const res = await Axios.get<ItemStateJSON>(wizardUri("state"));
  const nextState = {
    ...res.data,
    xml: parser.parseFromString(res.data.xml, "text/xml"),
  };
  return runListeners(nextState);
}

async function putEdits(itemEdit: ItemEdit): Promise<ItemCommandResponses> {
  const res = await Axios.put<ItemCommandResponses>(
    wizardUri("edit"),
    itemEdit,
  );
  return res.data;
}

function encodeFilepath(filepath: string): string {
  return filepath.split("/").map(encodeURIComponent).join("/");
}

const observer = new MutationObserver(function () {
  const liveElements = document.evaluate(
    "id('wizard-controls')//div[@data-clientupdate = 'true']",
    document,
    null,
    XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE,
    null,
  );
  const res: Element[] = [];
  for (let i = 0; i < liveElements.snapshotLength; i++) {
    res.push(liveElements.snapshotItem(i) as Element);
  }
  activeElements = activeElements.filter((e) => {
    if (res.indexOf(e.element) === -1) {
      e.removed.unmount(e.element);
      return false;
    }
    return true;
  });
});
observer.observe(document, {
  childList: true,
  subtree: true,
});

let allValid = true;

$(window).bind("validate", () => allValid);

$(window).bind("presubmit", () => {
  allValid = true;
  $("#cloudState").remove();
  $('<div id="cloudState"/>').appendTo("._hiddenstate");
  let editXmlFunc: (doc: XMLDocument) => XMLDocument = (x) => x;
  let xmlEdited = false;
  controlValidators.forEach(({ validator, ctrlId }) => {
    const valid = validator(
      (edit: (doc: XMLDocument) => XMLDocument) => {
        const oldXmlFunc = editXmlFunc;
        editXmlFunc = (d) => edit(oldXmlFunc(d));
        xmlEdited = true;
      },
      (required) => {
        $("<input>")
          .attr({
            type: "hidden",
            name: `${ctrlId}_required`,
            value: required.toString(),
          })
          .appendTo("#cloudState");
      },
    );
    allValid = allValid && valid;
  });
  if (xmlEdited) {
    latestXml = editXmlFunc(latestXml);
    const xmlDoc = serializer.serializeToString(latestXml);
    $("<input>")
      .attr({ type: "hidden", name: "xmldoc", value: xmlDoc })
      .appendTo("#cloudState");
    currentState = currentState.then((state) =>
      runListeners({ ...state, xml: latestXml }),
    );
  }
});

function runListeners(state: VersionedItemState): VersionedItemState {
  latestXml = state.xml;
  listeners.forEach((f) => f(state));
  return state;
}

export const CloudControl: CloudControlRegisterImpl = {
  register: function <T extends object>(
    vendorId: string,
    controlType: string,
    mount: (params: ControlApi<T>) => void,
    unmount: (removed: Element) => void,
  ) {
    registrations[vendorId + "_" + controlType] = { mount, unmount };
  },
  forceReload() {
    if (currentState) {
      reloadState = true;
      currentState = currentState.then(CloudControl.sendBatch);
    }
  },
  async sendBatch(state: VersionedItemState): Promise<VersionedItemState> {
    if (reloadState) {
      reloadState = false;
      let nextState = await getState();
      if (nextState.stateVersion < state.stateVersion) {
        console.log(
          `Out of order state detected, already had ${state.stateVersion} but got ${nextState.stateVersion}`,
        );
        console.log(
          `Already had ${serializer.serializeToString(
            state.xml,
          )} but got ${serializer.serializeToString(nextState.xml)}`,
        );
        nextState = state;
      }
      return CloudControl.sendBatch(nextState);
    }
    let edits: ItemCommand[] = [];
    const currentPromises: CommandsPromise[] = [];
    let xml;
    let newXml = state.xml;
    if (transformState) {
      newXml = transformState(newXml);
      xml = serializer.serializeToString(newXml);
    }
    commandQueue.forEach((j) => {
      edits = edits.concat(j.commands);
      currentPromises.push(j);
    });
    transformState = null;
    commandQueue = [];
    if (!edits.length && !xml) {
      return Promise.resolve(state);
    }
    try {
      const responses = await putEdits({ xml, edits });
      let att = state.attachments;
      const updateAttachments = function (change: ItemCommandResponse) {
        switch (change.type) {
          case "added":
            att.push(change.attachment);
            break;
          case "deleted":
            att = att.filter((at) => at.uuid !== change.uuid);
            break;
          case "edited": {
            const ind = att.findIndex(
              (at) => at.uuid === change.attachment.uuid,
            );
            att[ind] = change.attachment;
            break;
          }
        }
      };

      currentPromises.forEach((f) => {
        const editResponses = responses.results.splice(0, f.commands.length);
        editResponses.forEach(updateAttachments);
        f.resolved(editResponses);
      });

      newXml = parser.parseFromString(responses.xml, "text/xml");
      return runListeners({
        files: state.files,
        attachments: att,
        xml: newXml,
        stateVersion: state.stateVersion,
      });
    } catch (err) {
      const _err =
        err instanceof Error ? err : new Error("sendBatch() failed: " + err);
      currentPromises.forEach((p) => p.rejected(_err));
      return state;
    }
  },
  createRender(data) {
    const { wizId } = data;
    if (wizardIds && wizardIds.wizId !== wizId) {
      resetGlobalState();
    }
    wizardIds = data;
    if (!currentState) {
      currentState = getState();
    } else {
      CloudControl.forceReload();
    }

    function editXml(edit: (doc: XMLDocument) => XMLDocument) {
      if (transformState == null) transformState = edit;
      else {
        const oldf = transformState;
        transformState = (x) => edit(oldf(x));
      }
      currentState = currentState.then(CloudControl.sendBatch);
    }

    function edits(edits: Array<ItemCommand>) {
      return new Promise<ItemCommandResponse[]>(function (resolved, rejected) {
        commandQueue.push({ commands: edits, resolved, rejected });
        currentState = currentState.then(CloudControl.sendBatch);
      });
    }

    function subscribeUpdates(callback: (doc: ItemState) => void) {
      listeners.push(callback);
    }

    function unsubscribeUpdates(callback: (doc: ItemState) => void) {
      listeners.splice(listeners.indexOf(callback));
    }

    return function <T extends object>(params: ControlParameters<T>) {
      function stagingPath(name: string): string {
        return (
          "api/staging/" +
          encodeURIComponent(data.stagingId) +
          "/" +
          encodeFilepath(name)
        );
      }

      function uploadFile(name: string, file: File): Promise<void> {
        return Axios.put(stagingPath(name), file).then(
          CloudControl.forceReload,
        );
      }

      function deleteFile(name: string): Promise<void> {
        return Axios.delete(stagingPath(name)).then(CloudControl.forceReload);
      }

      function registerNotification() {
        Axios.post(
          wizardUri(
            "notify?providerId=" + encodeURIComponent(params.providerId),
          ),
        );
      }

      params.element.setAttribute("data-clientupdate", "true");

      const registration =
        registrations[params.vendorId + "_" + params.controlType] ||
        missingControl;
      activeElements.push({
        element: params.element,
        removed: registration,
      });

      function providerUrl(serviceId: string) {
        return wizardUri(
          "provider/" +
            encodeURIComponent(params.providerId) +
            "/" +
            encodeURIComponent(serviceId),
        );
      }

      function registerValidator(validator: ControlValidator) {
        controlValidators.push({ validator, ctrlId: params.ctrlId });
      }

      function deregisterValidator(validator: ControlValidator) {
        controlValidators.splice(
          controlValidators.findIndex((v) => v.validator === validator),
        );
      }

      currentState.then((state) => {
        const api = {
          ...state,
          ...params,
          editXml,
          subscribeUpdates,
          unsubscribeUpdates,
          edits,
          uploadFile,
          deleteFile,
          registerNotification,
          providerUrl,
          stagingId: wizardIds.stagingId,
          userId: wizardIds.userId,
          registerValidator,
          deregisterValidator,
          apiVersion: { major: 1, minor: 0, patch: 0 },
        };
        registration.mount(api);
      });
    };
  },
};

const missingControl: Registration = {
  mount: (params) => {
    const errText = $(
      `<div class="control ctrlinvalid"><p class="ctrlinvalidmessage">Failed to find registration for cloud control: "${params.vendorId}_${params.controlType}"</p></div>`,
    );
    $(params.element).append(errText);
    console.error("Parameters for failed cloud control", params);
  },
  unmount: (_) => {},
};
