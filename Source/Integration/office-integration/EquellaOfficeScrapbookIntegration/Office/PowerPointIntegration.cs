using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Interop.PowerPoint;
using System.IO;
using System.Web;
using System.Reflection;
using Microsoft.Office.Core;

namespace Equella.Office
{
    class PowerPointIntegration : BaseIntegration<Application, Presentation>
    {
        private static readonly string[] MIMES = new string[] { 
            "application/vnd.ms-powerpoint.template.macroEnabled.12",
            "application/vnd.openxmlformats-officedocument.presentationml.template",
            "application/vnd.ms-powerpoint.addin.macroEnabled.12",
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/powerpoint"
        };

        public PowerPointIntegration(Application powerPointApp, object addinInst) : base(powerPointApp, addinInst)
        {
            App.PresentationClose+=new EApplication_PresentationCloseEventHandler(PresentationClose);
            App.PresentationOpen+=new EApplication_PresentationOpenEventHandler(PresentationOpen);
            App.PresentationBeforeSave+=new EApplication_PresentationBeforeSaveEventHandler(PresentationBeforeSave);
            App.WindowSelectionChange += new EApplication_WindowSelectionChangeEventHandler(WindowSelectionChange);
        }

        private void WindowSelectionChange(Selection Sel)
        {
            InvokeOpened();
        }

        private void PresentationClose(Presentation doc)
        {
            InvokeClosed();
        }

        private void PresentationOpen(Presentation doc)
        {
            InvokeOpened();
        }

        private void PresentationBeforeSave(Presentation doc, ref bool cancel)
        {
            InvokeSaved();
        }

        protected override string Filename(Presentation doc)
        {
            return doc.FullName;
        }

        protected override Presentation BaseOpen(string filename)
        {
            return App.Presentations.Open(filename);
        }

        protected override void Save(Presentation doc)
        {
            doc.Save();
        }

        protected override void SaveAs(Presentation doc, string filename)
        {
            doc.SaveAs(filename, PpSaveAsFileType.ppSaveAsDefault, MsoTriState.msoTriStateMixed);
        }

        protected override void Close(Presentation doc)
        {
            doc.Close();
        }

        protected override bool ReadOnly(Presentation doc)
        {
            return doc.ReadOnly == MsoTriState.msoTrue;
        }

        protected override bool Dirty(Presentation doc)
        {
            return doc.Saved != MsoTriState.msoTrue;
        }

        protected override bool New(Presentation doc)
        {
            return string.IsNullOrEmpty(doc.Path);
        }

        protected override string Title(Presentation doc)
        {
            return doc.Name;
        }

        public override DocumentWrapper CurrentDocument
        {
            get
            {
                Presentation active = App.ActivePresentation;
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
                return "presentation.pptx";
            }
        }

        public override string[] MimeTypes
        {
            get
            {
                return MIMES;
            }
        }

        public override void SetVariable(Presentation doc, string name, string value)
        {
            string nsName = "EQUELLA." + name;
            DocumentProperties customProperties = doc.CustomDocumentProperties;
            foreach (DocumentProperty v in customProperties)
            {
                if (v.Name.Equals(nsName))
                {
                    v.Value = value;
                    return;
                }
            }
            customProperties.Add(nsName, false, MsoDocProperties.msoPropertyTypeString, value, Constants.MISSING);
        }

        public override string GetVariable(Presentation doc, string name)
        {
            string nsName = "EQUELLA." + name;
            DocumentProperties customProperties = doc.CustomDocumentProperties;
            foreach (DocumentProperty v in customProperties)
            {
                if (v.Name.Equals(nsName))
                {
                    return v.Value;
                }
            }
            return null;
        }
    }
}
