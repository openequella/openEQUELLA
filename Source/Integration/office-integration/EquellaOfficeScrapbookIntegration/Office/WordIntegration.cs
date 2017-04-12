using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Interop.Word;
using System.IO;
using System.Web;

namespace Equella.Office
{
    class WordIntegration : BaseIntegration<Application, Document>
    {
        private static readonly string[] MIMES = new string[] { 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-word.template.macroEnabled.12",
            "application/vnd.ms-word.document.macroEnabled.12",
            "application/msword"
        };

        public WordIntegration(Application App, object addinInst) : base(App, addinInst)
        { 
            App.DocumentBeforeClose +=new ApplicationEvents4_DocumentBeforeCloseEventHandler(DocumentBeforeClose);
            App.DocumentBeforeSave +=new ApplicationEvents4_DocumentBeforeSaveEventHandler(DocumentBeforeSave);
            App.DocumentOpen +=new ApplicationEvents4_DocumentOpenEventHandler(DocumentOpen);
            App.WindowSelectionChange += new ApplicationEvents4_WindowSelectionChangeEventHandler(WindowSelectionChange);
        }

        private void WindowSelectionChange(Selection Sel)
        {
            InvokeOpened();
        }

        private void DocumentBeforeClose(Document doc, ref bool cancel)
        {
            InvokeClosed();
        }

        private void DocumentOpen(Document doc)
        {
            InvokeOpened();
        }

        private void DocumentBeforeSave(Document doc, ref bool saveAsUI, ref bool cancel)
        {
            InvokeSaved();
        }

        protected override string Filename(Document doc)
        {
            return doc.FullName;
        }

        protected override Document BaseOpen(string filename)
        {
            return App.Documents.Open(filename);
        }

        protected override void Save(Document doc)
        {
            doc.Save();
        }

        protected override void SaveAs(Document doc, string filename)
        {
            doc.SaveAs(filename);
        }

        protected override void Close(Document doc)
        {
            ((_Document)doc).Close(false);
        }

        protected override bool ReadOnly(Document doc)
        {
            return doc.ReadOnly;
        }

        protected override bool Dirty(Document doc)
        {
            return !doc.Saved;
        }

        protected override bool New(Document doc)
        {
            return string.IsNullOrEmpty(doc.Path);
        }

        protected override string Title(Document doc)
        {
            return doc.Name;
        }

        public override DocumentWrapper CurrentDocument
        {
            get
            {
                Document active = App.ActiveDocument;
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
                return "document.docx";
            }
        }

        public override string[] MimeTypes
        {
            get
            {
                return MIMES;
            }
        }

        public override void SetVariable(Document doc, string name, string value)
        {
            string nsName = "EQUELLA." + name;
            foreach (Variable v in doc.Variables)
            {
                if (v.Name.Equals(nsName))
                {
                    v.Value = value;
                    return;
                }
            }
            doc.Variables.Add(nsName, value);
        }

        public override string GetVariable(Document doc, string name)
        {
            string nsName = "EQUELLA." + name;
            Variable v = doc.Variables[nsName];
            if (v != null)
            {
                try
                {
                    return v.Value;
                }
                catch (Exception)
                {
                    //dodge-o-rama.  If the variable doesn't exist you get an "Object deleted" exception
                    return null;
                }
            }
            return null;
        }
    }
}
