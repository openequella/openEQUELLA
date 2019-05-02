import * as ReactDOM from "react-dom";
import * as React from "react";
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

  return (
    <div className="control">
      <label>
        <h3>{p.title}</h3>
      </label>
      <input
        type="file"
        multiple
        onChange={e => {
          Array.from(e.currentTarget.files).forEach(f =>
            p.uploadFile("folder/" + f.name, f)
          );
        }}
      />
      <div>{writeDir("", files)}</div>
      <div>{attachXPath}</div>
      <div>{currentXml}</div>
      {attachments.map(a => (
        <div key={a.uuid}>
          Name {a.description}
          {a.uuid}
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
