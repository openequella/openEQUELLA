import * as ReactDOM from "react-dom";
import * as React from "react";
import axios from "axios";

import {
  ControlApi,
  CloudControlRegister,
  ItemState,
  Attachment,
  FileEntries
} from "oeq-cloudproviders/controls";

declare const CloudControl: CloudControlRegister;

interface MyConfig {
  somethingElse: string[];
}

const xmlSerializer = new XMLSerializer();

function textValues(result: XPathResult): string[] {
  const res = [];
  var thisNode = result.iterateNext();
  while (thisNode) {
    res.push(thisNode.textContent);
    thisNode = result.iterateNext();
  }
  return res;
}

function TestControl(p: ControlApi<MyConfig>) {
  const myConfig = p.config;
  const rootNode = myConfig.somethingElse[0];
  const attachXPath = "/xml" + rootNode + "/one//uuid";
  const myAttachments = function(
    xml: XMLDocument,
    all: Attachment[]
  ): Attachment[] {
    var uuids = textValues(
      xml.evaluate(
        attachXPath,
        xml.documentElement,
        null,
        XPathResult.ORDERED_NODE_ITERATOR_TYPE,
        null
      )
    );
    const filtered = all.filter(a => uuids.indexOf(a.uuid) != -1);
    return filtered;
  };
  const [files, setFiles] = React.useState(p.files);
  const [attachments, setAttachments] = React.useState(() => {
    return myAttachments(p.xml, p.attachments);
  });
  const [currentXml, setCurrentXml] = React.useState(() =>
    xmlSerializer.serializeToString(p.xml)
  );
  const [serviceId, setServiceId] = React.useState("myService");
  const [serviceContent, setServiceContent] = React.useState("");
  const [queryString, setQueryString] = React.useState(
    "param1=single&param2=more&param2=than&param2=two"
  );
  const [serviceResponse, setServiceResponse] = React.useState(null as
    | null
    | any);
  React.useEffect(() => {
    p.registerNotification();
    const updateHandler = function(state: ItemState) {
      setCurrentXml(xmlSerializer.serializeToString(state.xml));
      setAttachments(myAttachments(state.xml, state.attachments));
      setFiles(state.files);
    };
    p.subscribeUpdates(updateHandler);
    return function cleanup() {
      p.unsubscribeUpdates(updateHandler);
    };
  }, []);

  function writeDir(parentPath: string, entries: FileEntries) {
    return Object.keys(entries).map(function(filename) {
      let entry = entries[filename];
      let newParent = parentPath + filename + "/";
      return (
        <div>
          <div onClick={_ => p.deleteFile(parentPath + filename)}>
            {filename} - {entry.size}
          </div>
          {entry.files ? (
            <div style={{ marginLeft: "20px" }}>
              {writeDir(newParent, entry.files)}
            </div>
          ) : null}
        </div>
      );
    });
  }

  const renderService = (
    <div>
      <div>
        ServiceId:
        <input
          type="text"
          value={serviceId}
          onChange={e => setServiceId(e.target.value)}
        />
      </div>
      <div>
        QueryString:
        <input
          type="text"
          value={queryString}
          onChange={e => setQueryString(e.target.value)}
        />
      </div>
      <div>
        Payload:
        <textarea
          value={serviceContent}
          onChange={e => setServiceContent(e.target.value)}
          cols={100}
          rows={10}
        />
      </div>
      {serviceResponse && (
        <div>
          Response:
          <textarea
            value={JSON.stringify(serviceResponse)}
            suppressContentEditableWarning
            cols={100}
            rows={10}
          />
        </div>
      )}
      <button
        onClick={_ => {
          return axios
            .post(p.providerUrl(serviceId) + "?" + queryString, {
              data: serviceContent
            })
            .then(resp => setServiceResponse(resp.data))
            .catch((err: Error) => {
              setServiceResponse(err.message);
            });
        }}
      >
        Execute
      </button>
    </div>
  );

  return (
    <div className="control">
      <label>
        <h3>{p.title}</h3>
      </label>
      <h4>UserID: {p.userId}</h4>
      <h4>File tree</h4>
      <div>{writeDir("", files)}</div>
      <h4>Metadata</h4>
      <div>{currentXml}</div>
      <h4>Attachments</h4>
      {attachments.map(a => (
        <div key={a.uuid}>
          Name: {a.description}&nbsp;UUID: {a.uuid}
          <button
            onClick={() =>
              p.edits([
                {
                  command: "deleteAttachment",
                  uuid: a.uuid,
                  xmlPath: rootNode + "/one/uuid"
                }
              ])
            }
          >
            Delete
          </button>
          <button
            onClick={() => {
              p.edits([
                {
                  command: "editAttachment",
                  attachment: {
                    ...a,
                    description: a.description + " (EDITED)"
                  }
                }
              ]);
            }}
          >
            Edit
          </button>
        </div>
      ))}
      <h4>Upload file</h4>
      <input
        type="file"
        multiple
        onChange={e => {
          Array.from(e.currentTarget.files).forEach(f =>
            p.uploadFile("folder/" + f.name, f)
          );
        }}
      />
      <h4>Meddle with attachments</h4>
      <button
        onClick={_ => {
          p.editXml(doc => {
            const frogs = doc.createElement("frogs");
            frogs.appendChild(new Text("hi"));
            doc.firstChild.appendChild(frogs);
            return doc;
          });
          p.edits([
            {
              command: "addAttachment",
              attachment: {
                type: "file",
                description: "This is a file",
                filename: "hello.txt"
              },
              xmlPath: rootNode + "/one/uuid"
            },
            {
              command: "addAttachment",
              attachment: {
                type: "youtube",
                description: "This is a youtube video",
                videoId: "27awNyz-qdQ",
                uploadedDate: new Date()
              },
              xmlPath: rootNode + "/two/uuid"
            }
          ]);
        }}
      >
        Append some text
      </button>
      <h4>Communicate with provider</h4>
      {renderService}
    </div>
  );
}

CloudControl.register<MyConfig>(
  "oeq_autotest",
  "testcontrol",
  function(params) {
    ReactDOM.render(<TestControl {...params} />, params.element);
  },
  function(elem) {
    ReactDOM.unmountComponentAtNode(elem);
  }
);
