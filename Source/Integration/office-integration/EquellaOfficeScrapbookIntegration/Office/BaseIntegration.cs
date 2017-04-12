using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using Equella.Util;

namespace Equella.Office
{
    abstract class BaseIntegration<AppType,DocType> : Integration
    {
        protected readonly AppType App;
        protected readonly object AddIn;
        
        protected BaseIntegration(AppType app, object addIn)
        {
            this.App = app;
            this.AddIn = addIn;
        }

        public event DocumentEventHandler DocumentClosed;

        public event DocumentEventHandler DocumentOpened;

        public event DocumentEventHandler DocumentSaved;

        protected void InvokeClosed()
        {
            DocumentClosed();
        }

        protected void InvokeOpened()
        {
            DocumentOpened();
        }

        protected void InvokeSaved()
        {
            DocumentSaved();
        }

        public DocumentWrapper Open(string filename)
        {
            return new BaseDocumentWrapper(this, BaseOpen(filename));
        }

        public DocumentWrapper Save(DocumentWrapper docWrapper, string docFullPath)
        {
            DocType doc = (DocType)docWrapper.Document;

            //Remove read-onlyness from file...
            if (ReadOnly(doc))
            {
                Utils.Alert("Document is read-only.  Please wait while the document is re-opened in read mode");
                string filename = Filename(doc);
                string extension = Path.GetExtension(filename);
                string temp = Path.Combine(Path.GetTempPath(), Path.GetRandomFileName() + extension);

                SaveAs(doc, temp);
                Close(doc);

                return new BaseDocumentWrapper(this, BaseOpen(temp));
            }
            else if (docFullPath == null)
            {
                Save(doc);
            }
            else
            {
                SaveAs(doc, docFullPath);
            }
            return docWrapper;
        }

        public void AssociateMetadata(DocumentWrapper docWrapper, DocumentMetadata meta)
        {
            DocType doc = (DocType)docWrapper.Document;

            SetVariable(doc, Properties.ITEM_UUID, meta.ItemUuid);
            SetVariable(doc, Properties.ITEM_NAME, meta.ItemName);
            SetVariable(doc, Properties.KEYWORDS, meta.Keywords);
            SetVariable(doc, Properties.ATTACHMENT_FILENAME, meta.AttachmentFilename);
            SetVariable(doc, Properties.DOCUMENT_UUID, meta.DocumentUuid);
            SetVariable(doc, Properties.OWNER_ID, meta.OwnerId);
        }

        public byte[] ReadFile(DocumentWrapper docWrapper)
        {
            DocType doc = (DocType)docWrapper.Document;

            string docFilename = Filename(doc);
            string tempfile = Path.Combine(Path.GetTempPath(), Path.GetRandomFileName() + Path.GetExtension(docFilename));
            File.Copy(docFilename, tempfile, true);
            return File.ReadAllBytes(tempfile);
        }

        protected abstract string Filename(DocType doc);

        protected abstract DocType BaseOpen(string filename);

        protected abstract void Save(DocType doc);

        protected abstract void SaveAs(DocType doc, string filename);

        protected abstract void Close(DocType doc);

        protected abstract bool ReadOnly(DocType doc);

        protected abstract bool Dirty(DocType doc);

        protected abstract bool New(DocType doc);

        protected abstract string Title(DocType doc);
        

        //TODO: un-publicerise
        public abstract void SetVariable(DocType doc, string name, string value);

        //TODO: un-publicerise
        public abstract string GetVariable(DocType doc, string name);
        
        public abstract DocumentWrapper CurrentDocument
        {
            get;
        }

        public abstract string DefaultDocumentName
        {
            get;
        }

        public abstract string[] MimeTypes
        {
            get;
        }

        protected class BaseDocumentWrapper : DocumentWrapper
        {
            readonly BaseIntegration<AppType, DocType> integ;
            readonly DocType doc;
            
            public BaseDocumentWrapper(BaseIntegration<AppType, DocType> integ, DocType doc)
            {
                this.integ = integ;
                this.doc = doc;
            }

            public object Document
            {
                get
                {
                    return doc;
                }
            }

            public string Title
            {
                get
                {
                    return integ.Title(doc);
                }
            }

            public bool Dirty
            {
                get
                {
                    return integ.Dirty(doc);
                }
            }

            public bool New
            {
                get
                {
                    return integ.New(doc);
                }
            }

            public string FileName
            {
                get
                {
                    return integ.Filename( doc);
                }
            }

            public DocumentMetadata Metadata
            {
                get
                {
                    string attachmentFilename = integ.GetVariable(doc, Properties.ATTACHMENT_FILENAME);
                    return new DocumentMetadata {
                        ItemUuid = integ.GetVariable(doc, Properties.ITEM_UUID),
                        ItemName = integ.GetVariable(doc, Properties.ITEM_NAME),
                        Keywords= integ.GetVariable(doc, Properties.KEYWORDS),
                        AttachmentFilename = integ.GetVariable(doc, Properties.ATTACHMENT_FILENAME),
                        DocumentUuid = integ.GetVariable(doc, Properties.DOCUMENT_UUID),
                        OwnerId = integ.GetVariable(doc, Properties.OWNER_ID)
                    };
                }
            }
        }
    }
}
