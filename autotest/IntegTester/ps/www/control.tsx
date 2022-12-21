import * as ReactDOM from "react-dom";
import * as React from "react";
import axios, { AxiosResponse } from "axios";

import {
  ControlApi,
  Attachment,
  ItemState,
  FileEntries,
  ControlValidator
} from "oeq-cloudproviders/controls";

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
  const [failValidation, setFailValidation] = React.useState(false);
  const [required, setRequired] = React.useState(false);
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
  const [serviceResponse, setServiceResponse] = React.useState<
    string | JSON | null
  >(null);
  const [postRequest, setPostRequest] = React.useState(true);
  const [indexText, setIndexText] = React.useState("");
  const [indexFiles, setIndexFiles] = React.useState<string[]>([]);
  React.useEffect(() => {
    p.registerNotification();
    const updateHandler = function(state: ItemState) {
      setCurrentXml(xmlSerializer.serializeToString(state.xml));
      setAttachments(myAttachments(state.xml, state.attachments));
      setFiles(state.files);
    };
    p.subscribeUpdates(updateHandler);
    return () => {
      p.unsubscribeUpdates(updateHandler);
    };
  }, []);

  const validator: ControlValidator = React.useCallback(
    (editXml, setRequired) => {
      editXml(d => {
        let elem = d.createElement("validated");
        elem.appendChild(d.createTextNode((!failValidation).toString()));
        d.documentElement.appendChild(elem);
        return d;
      });
      setRequired(required);
      return !failValidation;
    },
    [failValidation, required]
  );

  interface TestingButtonProps {
    buttonName: string;
    onClick: () => Promise<AxiosResponse<any>>;
  }
  const TestingButton = TestingButtonProps => (
    <button
      onClick={() => {
        TestingButtonProps.onClick()
          .then(resp => setServiceResponse(resp.data))
          .catch((err: Error) => {
            setServiceResponse(err.message);
          });
      }}
    >
      {TestingButtonProps.buttonName}
    </button>
  );

  const requestUrl = (query: String = queryString) => {
    return p.providerUrl(serviceId) + "?" + query;
  };

  React.useEffect(() => {
    p.registerValidator(validator);
    return () => p.deregisterValidator(validator);
  }, [validator]);

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
      {postRequest && (
        <div>
          Payload:
          <textarea
            value={serviceContent}
            onChange={e => setServiceContent(e.target.value)}
            cols={100}
            rows={10}
          />
        </div>
      )}
      {serviceResponse && (
        <div>
          Response:
          <textarea
            value={JSON.stringify(serviceResponse)}
            readOnly
            cols={100}
            rows={10}
          />
        </div>
      )}

      <TestingButton buttonName="GET" onClick={() => axios.get(requestUrl())} />

      <TestingButton
        buttonName="POST"
        onClick={() =>
          axios.post(requestUrl(), {
            data: serviceContent
          })
        }
      />

      <TestingButton
        buttonName="DELETE"
        onClick={() => axios.delete(requestUrl("param1=test_param_one"))}
      />

      <TestingButton
        buttonName="PUT"
        onClick={() =>
          axios.put(requestUrl(), {
            data: serviceContent
          })
        }
      />
    </div>
  );

  function makeAttachments() {
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
          type: "youtube",
          description: "This is a youtube video",
          videoId: "27awNyz-qdQ",
          uploadedDate: new Date()
        },
        xmlPath: rootNode + "/two/uuid"
      },
      {
        command: "addAttachment",
        attachment: {
          type: "cloud",
          description: "This is a cloud attachment",
          providerId: p.providerId,
          cloudType: "simple",
          vendorId: p.vendorId,
          display: {
            Arbitrary: "Field",
            Size: 0,
            Ordered: true
          },
          meta: {
            viewer: "Something"
          },
          indexText,
          indexFiles
        },
        xmlPath: rootNode + "/one/uuid"
      }
    ]).then(_ => {
      setIndexFiles([]);
      setIndexText("");
    });
  }

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
          Array.from(e.currentTarget.files).forEach(f => {
            const filePath = "folder/" + f.name;
            p.uploadFile(filePath, f).then(_ =>
              setIndexFiles(files => [...files, filePath])
            );
          });
        }}
      />
      <h4>Meddle with attachments</h4>
      IndexText:{" "}
      <textarea
        value={indexText}
        onChange={e => setIndexText(e.target.value)}
        cols={100}
        rows={10}
      />
      <div>
        <button onClick={makeAttachments}>Make some attachments</button>
      </div>
      <h4>Communicate with provider</h4>
      {renderService}
      <div>
        <button onClick={_ => setRequired(v => !v)}>
          Toggle requires filling out - (
          {required ? "Required" : "Not Required"})
        </button>
        <button onClick={_ => setFailValidation(v => !v)}>
          Toggle validator - ({failValidation ? "fail" : "succeed"})
        </button>
      </div>
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
