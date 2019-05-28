import Axios from "axios";

import {
  ControlApi,
  ItemCommand,
  CloudControlRegister,
  ControlParameters,
  ItemState,
  ItemCommandResponse,
  Attachment,
  FileEntries
} from "oeq-cloudproviders/controls";

const wgxpath = require("wicked-good-xpath");
wgxpath.install(window);
interface ItemStateJSON {
  xml: string;
  attachments: Attachment[];
  files: FileEntries;
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
    data: WizardIds
  ) => (params: ControlApi<T>) => void;
  forceReload(): void;
  sendBatch(state: ItemState): Promise<ItemState>;
}

interface ItemCommandResponses {
  xml: string;
  results: ItemCommandResponse[];
}

interface CommandsPromise {
  commands: ItemCommand[];
  resolved: (response: ItemCommandResponse[]) => void;
  rejected: (err: any) => void;
}

var registrations: {
  [key: string]: {
    mount: (api: ControlApi<object>) => void;
    unmount: (removed: Element) => void;
  };
} = {};
var listeners: ((doc: ItemState) => void)[] = [];
var commandQueue: CommandsPromise[] = [];
var transformState: ((doc: XMLDocument) => XMLDocument) | null = null;
var reloadState: boolean = false;
var wizardIds: WizardIds;
var currentState: Promise<ItemState>;

const parser = new DOMParser();
const serializer = new XMLSerializer();

var activeElements: {
  element: Element;
  removed: (removed: Element) => void;
}[] = [];

function wizardUri(path: string): string {
  return "api/wizard/" + encodeURIComponent(wizardIds.wizId) + "/" + path;
}

async function getState(wizid: string): Promise<ItemState> {
  const res = await Axios.get<ItemStateJSON>(wizardUri("state"));
  const nextState = {
    ...res.data,
    xml: parser.parseFromString(res.data.xml, "text/xml")
  };
  listeners.forEach(f => f(nextState));
  return nextState;
}

async function putEdits(itemEdit: ItemEdit): Promise<ItemCommandResponses> {
  const res = await Axios.put<ItemCommandResponses>(
    wizardUri("edit"),
    itemEdit
  );
  return res.data;
}

function encodeFilepath(filepath: string): string {
  return filepath
    .split("/")
    .map(encodeURIComponent)
    .join("/");
}

const observer = new MutationObserver(function() {
  const liveElements = document.evaluate(
    "id('wizard-controls')//div[@data-clientupdate = 'true']",
    document,
    null,
    XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE,
    null
  );
  const res: Element[] = [];
  for (var i = 0; i < liveElements.snapshotLength; i++) {
    res.push(liveElements.snapshotItem(i) as Element);
  }
  activeElements = activeElements.filter(e => {
    if (res.indexOf(e.element) == -1) {
      e.removed(e.element);
      return false;
    }
    return true;
  });
});
observer.observe(document, {
  childList: true,
  subtree: true
});

export const CloudControl: CloudControlRegisterImpl = {
  register: function<T extends object>(
    vendorId: string,
    controlType: string,
    mount: (params: ControlApi<T>) => void,
    unmount: (removed: Element) => void
  ) {
    registrations[vendorId + "_" + controlType] = { mount, unmount };
  },
  forceReload: function() {
    if (currentState) {
      reloadState = true;
      currentState = currentState.then(CloudControl.sendBatch);
    }
  },
  sendBatch: async function(state: ItemState): Promise<ItemState> {
    if (reloadState) {
      reloadState = false;
      var nextState = await getState(wizardIds.wizId);
      return CloudControl.sendBatch(nextState);
    }
    var edits: ItemCommand[] = [];
    var currentPromises: CommandsPromise[] = [];
    var xml;
    var newXml = state.xml;
    if (transformState) {
      newXml = transformState(newXml);
      xml = serializer.serializeToString(newXml);
    }
    commandQueue.forEach(j => {
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
      var att = state.attachments;
      const updateAttachments = function(change: ItemCommandResponse) {
        switch (change.type) {
          case "added":
            att.push(change.attachment);
            break;
          case "deleted":
            att = att.filter(at => at.uuid != change.uuid);
            break;
          case "edited":
            var ind = att.findIndex(at => at.uuid == change.attachment.uuid);
            att[ind] = change.attachment;
            break;
        }
      };

      currentPromises.forEach(f => {
        const editResponses = responses.results.splice(0, f.commands.length);
        editResponses.forEach(updateAttachments);
        f.resolved(editResponses);
      });

      newXml = parser.parseFromString(responses.xml, "text/xml");
      const nextState = { files: state.files, attachments: att, xml: newXml };
      listeners.forEach(f => f(nextState));
      return nextState;
    } catch (err) {
      currentPromises.forEach(p => p.rejected(err));
      return state;
    }
  },
  createRender: function(data) {
    const { wizId } = data;
    wizardIds = data;
    if (!currentState) {
      currentState = getState(wizId);
    } else {
      CloudControl.forceReload();
    }

    const editXml = function(edit: (doc: XMLDocument) => XMLDocument) {
      if (transformState == null) transformState = edit;
      else {
        const oldf = transformState;
        transformState = x => edit(oldf(x));
      }
      currentState = currentState.then(CloudControl.sendBatch);
    };

    const edits = function(edits: Array<ItemCommand>) {
      return new Promise<ItemCommandResponse[]>(function(resolved, rejected) {
        commandQueue.push({ commands: edits, resolved, rejected });
        currentState = currentState.then(CloudControl.sendBatch);
      });
    };
    const subscribeUpdates = function(callback: (doc: ItemState) => void) {
      listeners.push(callback);
    };
    const unsubscribeUpdates = function(callback: (doc: ItemState) => void) {
      listeners.splice(listeners.indexOf(callback));
    };
    return function<T extends object>(params: ControlParameters<T>) {
      const stagingPath = function(name: string): string {
        return (
          "api/staging/" +
          encodeURIComponent(data.stagingId) +
          "/" +
          encodeFilepath(name)
        );
      };
      const uploadFile = function(name: string, file: File): Promise<void> {
        return Axios.put(stagingPath(name), file).then(
          CloudControl.forceReload
        );
      };
      const deleteFile = function(name: string): Promise<void> {
        return Axios.delete(stagingPath(name)).then(CloudControl.forceReload);
      };
      const registerNotification = function() {
        Axios.post(
          wizardUri(
            "notify?providerId=" + encodeURIComponent(params.providerId)
          )
        );
      };
      params.element.setAttribute("data-clientupdate", "true");

      const { mount, unmount } = registrations[
        params.vendorId + "_" + params.controlType
      ];
      activeElements.push({
        element: params.element,
        removed: unmount
      });
      const providerUrl = function(serviceId: string) {
        return wizardUri(
          "provider/" +
            encodeURIComponent(params.providerId) +
            "/" +
            encodeURIComponent(serviceId)
        );
      };
      currentState.then(state => {
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
          userId: wizardIds.userId
        };
        mount(api);
      });
    };
  }
};
