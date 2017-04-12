using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Interop.Access;
using System.IO;
using System.Web;

namespace Equella.Office
{
    /// <summary>
    /// This class is not implemented!!!
    /// 
    /// </summary>
    class AccessIntegration : BaseIntegration<Application, _CurrentProject >
    {
        private static readonly string[] MIMES = new string[] {      };

        public AccessIntegration(Application App, object addinInst)
            : base(App, addinInst)
        { 
            //App.DocumentBeforeClose +=new ApplicationEvents4_DocumentBeforeCloseEventHandler(DocumentBeforeClose);
            //App.DocumentBeforeSave +=new ApplicationEvents4_DocumentBeforeSaveEventHandler(DocumentBeforeSave);
            //App.DocumentOpen +=new ApplicationEvents4_DocumentOpenEventHandler(DocumentOpen);
        }

        private void DocumentBeforeClose(_CurrentProject doc, ref bool cancel)
        {
            InvokeClosed();
        }

        private void DocumentOpen(_CurrentProject doc)
        {
            InvokeOpened();
            /*
            "DEBUG: DocumentOpen Document variables:\n {0}={1}\n {2}={3}\n {4}={5}\n {6}={7}\n {8}={9}\n {10}={11}\n {12}={13}".Alert(
                ITEM_UUID, GetVariable(doc, ITEM_UUID), 
                ITEM_VERSION, GetVariable(doc, ITEM_VERSION),
                ITEM_NAME, GetVariable(doc, ITEM_NAME),
                ITEM_DESCRIPTION, GetVariable(doc, ITEM_DESCRIPTION),
                ATTACHMENT_FILENAME, GetVariable(doc, ATTACHMENT_FILENAME),
                ATTACHMENT_DESCRIPTION, GetVariable(doc, ATTACHMENT_DESCRIPTION),
                DOCUMENT_UUID, GetVariable(doc, DOCUMENT_UUID));*/
        }

        private void DocumentBeforeSave(_CurrentProject doc, ref bool saveAsUI, ref bool cancel)
        {
            InvokeSaved();
            /*
            "DEBUG: DocumentOpen Document variables:\n {0}={1}\n {2}={3}\n {4}={5}\n {6}={7}\n {8}={9}\n {10}={11}\n {12}={13}".Alert(
                ITEM_UUID, GetVariable(doc, ITEM_UUID),
                ITEM_VERSION, GetVariable(doc, ITEM_VERSION),
                ITEM_NAME, GetVariable(doc, ITEM_NAME),
                ITEM_DESCRIPTION, GetVariable(doc, ITEM_DESCRIPTION),
                ATTACHMENT_FILENAME, GetVariable(doc, ATTACHMENT_FILENAME),
                ATTACHMENT_DESCRIPTION, GetVariable(doc, ATTACHMENT_DESCRIPTION),
                DOCUMENT_UUID, GetVariable(doc, DOCUMENT_UUID));*/
        }

        protected override string Filename(_CurrentProject doc)
        {
            return doc.FullName;
        }

        protected override _CurrentProject BaseOpen(string filename)
        {
            throw new NotImplementedException();
        }

        protected override void Save(_CurrentProject doc)
        {
            throw new NotImplementedException();
        }

        protected override void SaveAs(_CurrentProject doc, string filename)
        {
            throw new NotImplementedException();
        }

        protected override void Close(_CurrentProject doc)
        {
            throw new NotImplementedException();
        }

        protected override bool ReadOnly(_CurrentProject doc)
        {
            throw new NotImplementedException();
        }

        protected override bool Dirty(_CurrentProject doc)
        {
            throw new NotImplementedException();
        }

        protected override bool New(_CurrentProject doc)
        {
            return string.IsNullOrEmpty(doc.Path);
        }

        protected override string Title(_CurrentProject doc)
        {
            return doc.Name;
        }

        public override DocumentWrapper CurrentDocument
        {
            get
            {
                _CurrentProject active = App.CurrentProject;
                if (active != null)
                {
                    return new BaseDocumentWrapper(this, active);
                }
                return null;
            }
        }

        public override string DefaultDocumentName
        {
            get
            {
                return "database.mdbx";
            }
        }

        public override string[] MimeTypes
        {
            get
            {
                return MIMES;
            }
        }

        public override void SetVariable(_CurrentProject doc, string name, string value)
        {
            throw new NotImplementedException();
        }

        public override string GetVariable(_CurrentProject doc, string name)
        {
            throw new NotImplementedException();
        }
    }
}
