using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Interop.Excel;
using System.IO;
using System.Web;
using System.Reflection;
using Microsoft.Office.Core;

namespace Equella.Office
{
    class ExcelIntegration : BaseIntegration<Application, Workbook>
    {
        private static readonly string[] MIMES = new string[] { 
            "application/vnd.ms-excel.addin.macroEnabled.12",
            "application/vnd.ms-excel.sheet.binary.macroEnabled.12",
            "application/vnd.ms-excel.sheet.macroEnabled.12",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel.template.macroEnabled.12",
            "application/vnd.openxmlformats-officedocument.spreadsheetml",
            "application/vnd.ms-excel"
        };

        public ExcelIntegration(Application excelApp, object addIn)
            : base(excelApp, addIn)
        {
            App.WorkbookBeforeClose += new AppEvents_WorkbookBeforeCloseEventHandler(WorkbookBeforeClose);
            App.WorkbookBeforeSave += new AppEvents_WorkbookBeforeSaveEventHandler(WorkbookBeforeSave);
            App.WorkbookOpen += new AppEvents_WorkbookOpenEventHandler(WorkbookOpen);
            App.WindowActivate += new AppEvents_WindowActivateEventHandler(WindowActivate);
            App.AlertBeforeOverwriting = false;
        }

        private void WindowActivate(Workbook Wb, Window Wn)
        {
            InvokeOpened();
        }

        private void WorkbookBeforeClose(Workbook doc, ref bool cancel)
        {
            InvokeClosed();
        }

        private void WorkbookOpen(Workbook doc)
        {
            InvokeOpened();
        }

        private void WorkbookBeforeSave(Workbook doc, bool saveAsUI, ref bool cancel)
        {
            InvokeSaved();
        }

        protected override string Filename(Workbook doc)
        {
            return doc.FullName;
        }

        protected override Workbook BaseOpen(string filename)
        {
            return App.Workbooks.Open(filename);
        }

        protected override void Save(Workbook doc)
        {
            doc.Save();
        }

        protected override void SaveAs(Workbook doc, string filename)
        {
            doc.SaveAs(filename);
        }

        protected override void Close(Workbook doc)
        {
            doc.Close(false);
        }

        protected override bool ReadOnly(Workbook doc)
        {
            return doc.ReadOnly;
        }

        protected override bool Dirty(Workbook doc)
        {
            return !doc.Saved;
        }

        protected override bool New(Workbook doc)
        {
            return string.IsNullOrEmpty(doc.Path);
        }

        protected override string Title(Workbook doc)
        {
            return doc.Name;
        }

        public override DocumentWrapper CurrentDocument
        {
            get
            {
                Workbook active = App.ActiveWorkbook;
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
                return "workbook.xlsx";
            }
        }

        public override string[] MimeTypes
        {
            get
            {
                return MIMES;
            }
        }

        public override void SetVariable(Workbook doc, string name, string value)
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

        public override string GetVariable(Workbook doc, string name)
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
