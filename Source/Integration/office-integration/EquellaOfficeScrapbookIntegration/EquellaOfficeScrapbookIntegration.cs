using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.EnterpriseServices;
using Microsoft.Office.Core;
using System.Diagnostics;
using System.Runtime.InteropServices;
using Extensibility;
using Microsoft.Office.Interop;
using System.Reflection;
using System.Xml;
using System.IO;
using System.Drawing;
using System.Web;
using Equella.Office;
using System.Windows.Forms;
using System.Net;
using Equella.GUI;
using System.Xml.Linq;
using Equella.Util;
using Equella.Soap;
using System.Threading;
using Equella.Config;

namespace Equella
{
    [ComVisible(true)]
    [Guid("002b5dd8-803c-11e0-bdd2-69824824019b")]
    public class EquellaOfficeScrapbookIntegration : IDTExtensibility2, IRibbonExtensibility 
    {
        private const string BAD_URL_MESSAGE = "There was a problem contacting the institution URL ({0}) associated with the currently active profile.  Perhaps the server is currently unavailable?";

        private Thread keepAlive;

        private Integration Integ
        {
            get;
            set;
        }

        private IRibbonUI Ribbon
        {
            get;
            set;
        }

        private EQUELLASOAP _Soap;
        private EQUELLASOAP Soap
        {
            get
            {
                if (_Soap == null)
                {
                    MakeNewSoap(Utils.CookieJar);
                }
                return _Soap;
            }
        }


        private void MakeNewSoap(CookieContainer cookieJar)
        {
            if (_Soap != null)
            {
                _Soap.Logout();
            }
            _Soap = new EQUELLASOAP(
                        InstitutionURL,
                        null, null,
                        null, null, null, cookieJar);
            //keep alive
            if (keepAlive != null)
            {
                keepAlive.Abort();
                keepAlive.Join();
            }
            keepAlive = new Thread(new ThreadStart(DoKeepAlive));
            keepAlive.Name = "Keep Alive";
            keepAlive.Start();
        }


        private void DoKeepAlive()
        {
            Thread.Sleep(1800000);
            try
            {
                bool loggedIn = Soap.LoggedIn;
            }
            catch (WebException)
            {
                // We don't want to spam the user, they may already know it's broken
            }
        }


        /// <summary>
        /// Gets the institution URL based on the current profile.  If there is no profile it displays a message and launched the config dialog.  
        /// If user cancels the dialog without creating a profile then the NoProfileException exception is thrown.
        /// </summary>
        /// <exception cref="NoProfileException">Throws this exception to immediately jump out of code.  I.e. you catch it and return from your method.</exception>
        private string InstitutionURL
        {
            get
            {
                if (Singletons.Config.CurrentProfile == null)
                {
                    new ConfigForm().ShowProfiles(true);
                    if (Singletons.Config.CurrentProfile == null)
                    {
                        Utils.Alert("You cannot use this functionality without an active EQUELLA profile.");
                        
                        throw new NoProfileException("No current profile URL");
                    }
                }
                return Singletons.Config.CurrentProfile.URL;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <exception cref="WebException">As thrown by SOAP.LoggedIn</exception>
        public bool LoggedIn
        {
            get
            {
                return Soap.LoggedIn;
            }
        }

        [EntryPoint]
        public void OnConnection(object application, Extensibility.ext_ConnectMode connectMode, object addInInst, ref System.Array custom)
        {
            try
            {
                Singletons.Module = this;

                if (application is Microsoft.Office.Interop.Word.Application)
                {
                    Integ = new WordIntegration(application as Microsoft.Office.Interop.Word.Application, addInInst);
                }
                else if (application is Microsoft.Office.Interop.Excel.Application)
                {
                    Integ = new ExcelIntegration(application as Microsoft.Office.Interop.Excel.Application, addInInst);
                }
                else if (application is Microsoft.Office.Interop.PowerPoint.Application)
                {
                    Integ = new PowerPointIntegration(application as Microsoft.Office.Interop.PowerPoint.Application, addInInst);
                }

                Integ.DocumentClosed += new DocumentEventHandler(Integ_DocumentClosed);
                Integ.DocumentOpened += new DocumentEventHandler(Integ_DocumentOpened);
                Integ.DocumentSaved += new DocumentEventHandler(Integ_DocumentSaved);

                if (connectMode != Extensibility.ext_ConnectMode.ext_cm_Startup)
                {
                    OnStartupComplete(ref custom);
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
            }
        }

        private void Integ_DocumentSaved()
        {
            InvalidateUpdate();
        }

        private void Integ_DocumentOpened()
        {
            InvalidateUpdate();
        }

        private void Integ_DocumentClosed()
        {
            InvalidateUpdate();
        }

        private void InvalidateUpdate()
        {
            if (Ribbon != null)
            {
                Ribbon.InvalidateControl("EquellaUpdate");
            }
        }

        [EntryPoint]
        public void OnDisconnection(Extensibility.ext_DisconnectMode disconnectMode, ref System.Array custom)
        {
            if (disconnectMode != Extensibility.ext_DisconnectMode.ext_dm_HostShutdown)
            {
                OnBeginShutdown(ref custom);
            }
            Integ = null;
        }

        [EntryPoint]
        public void OnAddInsUpdate(ref System.Array custom)
        {
        }

        [EntryPoint]
        public void OnStartupComplete(ref System.Array custom)
        {
        }

        [EntryPoint]
        public void OnBeginShutdown(ref System.Array custom)
        {
            try
            {
                if (_Soap != null && _Soap.LoggedIn)
                {
                    _Soap.Logout();
                }
                _Soap = null;
            }
            catch (WebException)
            {
                //Ignore it
            }
        }
         
        [EntryPoint]
        public void OnLoad(IRibbonUI ribbon)
        {
            Ribbon = ribbon;
        }

        [EntryPoint]
        public Bitmap LoadImage(string imageName)
        {
            try
            {
                Assembly assembly = Assembly.GetExecutingAssembly();
                using (Stream stream = Utils.LoadResource(imageName))
                {
                    return new Bitmap(stream);
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
                return null;
            }
        }

        [EntryPoint]
        public string GetCustomUI(string RibbonID)
        {
            try
            {
                Assembly assembly = Assembly.GetExecutingAssembly();
                using (Stream stream = Utils.LoadResource("UI.xml"))
                {
                    return new StreamReader(stream).ReadToEnd();
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
                return null;
            }
        }

        [EntryPoint]
        public void Configure(IRibbonControl button)
        {
            try
            {
                new ConfigForm().ShowProfiles(false);
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
            }
        }

        [EntryPoint]
        public void ShowBrowse(IRibbonControl button)
        {
            try
            {
                Singletons.BrowseForm.ShowBrowseScrapbook(InstitutionURL + "logon.do");
                if (Singletons.BrowseForm.Selection != null)
                {
                    HandleSelection(Singletons.BrowseForm.Selection);
                }
            }
            catch (NoProfileException)
            {
            }
            catch (WebException)
            {
                Utils.Alert(String.Format(BAD_URL_MESSAGE, InstitutionURL));
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
            }
        }

        [EntryPoint]
        public void EquellaCreate_DoCreate(IRibbonControl button)
        {
            try
            {
                if (!EnsureLogin())
                {
                    return;
                }
            }
            catch (NoProfileException)
            {
                return;
            }
            catch (WebException)
            {
                Utils.Alert(String.Format(BAD_URL_MESSAGE, InstitutionURL));
                return;
            }

            try
            {
                DocumentWrapper doc = Integ.CurrentDocument;
                DocumentMetadata meta = doc.Metadata;

                string itemName = meta.ItemName;
                string keywords = meta.Keywords ?? "";
                string attachmentFilename = meta.AttachmentFilename;
                string documentUuid = System.Guid.NewGuid().ToString();
                string ownerId = Soap.LoggedInUser;
                
                string docFullPath = null;
                if (!doc.New)
                {
                    docFullPath = doc.FileName;
                    if (attachmentFilename == null)
                    {
                        attachmentFilename = Path.GetFileName(docFullPath);
                    }
                }
                else
                {
                    attachmentFilename = Integ.DefaultDocumentName;
                    docFullPath = Path.Combine(Path.GetTempPath(), attachmentFilename);
                }
                if (itemName == null)
                {
                    itemName = attachmentFilename;
                }

                if (AskDetails(ref itemName, ref keywords, ref attachmentFilename))
                {
                    //unfortunately it's a two step process.... we need the itemUuid from the new item, but we need to also store
                    //it in the document.

                    //saves a placeholder attachment with a single byte in it.  better than uploading twice
                    XElement item = Soap.NewScrapbookItem("TemporaryResource", keywords, attachmentFilename, new byte[] { 0 });
                    XElement itemPart = item.Element("item");
                    string itemUuid = (string)itemPart.Attribute("id");

                    //Re-save the document with updated metadata
                    Integ.AssociateMetadata(doc, new DocumentMetadata { ItemUuid = itemUuid, ItemName = itemName, 
                        Keywords = keywords, AttachmentFilename = attachmentFilename, DocumentUuid = documentUuid,
                        OwnerId = ownerId });
                    doc = Integ.Save(doc, Path.Combine(Path.GetTempPath(), attachmentFilename));

                    byte[] bytes = Integ.ReadFile(doc);
                    Soap.UpdateScrapbookItem(itemUuid, itemName, keywords, attachmentFilename, bytes);

                    Utils.Alert("A new scrapbook resource titled \"" + itemName + "\" has been created in EQUELLA");
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
            }
        }

        [EntryPoint]
        public bool EquellaUpdate_Enabled(IRibbonControl button)
        {
            try
            {
                DocumentMetadata meta = Integ.CurrentDocument.Metadata;
                if (_Soap != null)
                {
                    string currentUserId = Soap.LoggedInUser;
                    string ownerId = meta.OwnerId;
                    if (!string.IsNullOrEmpty(ownerId) && currentUserId != ownerId)
                    {
                        return false;
                    }
                }
                return !string.IsNullOrEmpty(meta.ItemUuid);
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
                return false;
            }
        }

        [EntryPoint]
        public string EquellaUpdate_Label(IRibbonControl button)
        {
            return "Update EQUELLA scrapbook resource";
        }

        [EntryPoint]
        public string EquellaUpdate_Description(IRibbonControl button)
        {
            try
            {
                if (Integ.CurrentDocument != null)
                {
                    if (string.IsNullOrEmpty(Integ.CurrentDocument.Metadata.ItemUuid))
                    {
                        return "You need to have an existing scrapbook document open to use the update feature";
                    }
                    else
                    {
                        DocumentWrapper doc = Integ.CurrentDocument;
                        DocumentMetadata meta = doc.Metadata;
                        string name = (string.IsNullOrEmpty(meta.ItemName) ? doc.FileName : meta.ItemName);
                        return "Updates the existing \"" + name + "\" scrapbook resource on the EQUELLA server";
                    }
                }
                else
                {
                    return "You have no open document to publish";
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
                return "Updates an existing scrapbook resource on the EQUELLA server";
            }
        }

        [EntryPoint]
        public void EquellaUpdate_DoUpdate(IRibbonControl button)
        {
            try
            {
                if (!EnsureLogin())
                {
                    return;
                }
            }
            catch (NoProfileException)
            {
                return;
            }
            catch (WebException)
            {
                Utils.Alert(String.Format(BAD_URL_MESSAGE, InstitutionURL));
                return;
            }

            try
            {
                DocumentWrapper doc = Integ.CurrentDocument;
                DocumentMetadata meta = doc.Metadata;

                string ownerId = meta.OwnerId;
                string currentUserId = Soap.LoggedInUser;
                if (currentUserId != ownerId)
                {
                    //make a new item regardless.  this document was owned by someone else
                    Utils.Alert("You are not the owner of this document.  A new scrapbook resource will be created instead.");
                    EquellaCreate_DoCreate(null);
                    return;
                }

                string itemUuid = meta.ItemUuid;
                if (!Soap.ScrapbookItemExists(itemUuid))
                {
                    Utils.Alert("The scrapbook resource associated with this document could not be found.  A new scrapbook resource will be created instead.");
                    EquellaCreate_DoCreate(null);
                    return;
                }
                
                string docFullPath = doc.FileName;
                string keywords = meta.Keywords ?? "";
                string attachmentFilename = meta.AttachmentFilename ??  Path.GetFileName(docFullPath);
                string itemName = meta.ItemName ?? attachmentFilename;
                string documentUuid = meta.DocumentUuid;
                

                if (AskDetails(ref itemName, ref keywords, ref attachmentFilename))
                {
                    //Save the document with updated metadata
                    Integ.AssociateMetadata(doc, new DocumentMetadata { ItemUuid = itemUuid, ItemName = itemName, Keywords = keywords, AttachmentFilename = attachmentFilename, DocumentUuid = documentUuid });
                    doc = Integ.Save(doc, Path.Combine(Path.GetTempPath(), attachmentFilename));
                    
                    byte[] bytes = Integ.ReadFile(doc);
                    Soap.UpdateScrapbookItem(itemUuid, itemName, keywords, attachmentFilename, bytes);

                    Utils.Alert("A scrapbook resource has been updated in EQUELLA");
                }
            }
            catch (Exception e)
            {
                Utils.ShowError(e);
            }
        }

        private bool AskDetails(ref string itemName, ref string keywords, ref string attachmentFilename)
        {
            //get details as entered by user (or cancel)
            ItemDetailsForm details = new ItemDetailsForm();
            DialogResult res = details.ShowDetails(itemName, keywords, attachmentFilename);
            if (res == DialogResult.Cancel)
            {
                return false;
            }
            itemName = details.ItemName;
            keywords = details.Keywords;
            attachmentFilename = details.AttachmentFilename;
            return true;
        }

        public XElement SearchScrapbook(string query, int sortOrder)
        {
            return Soap.SearchScrapbook(query, Integ.MimeTypes, sortOrder, 0, -1);
        }

        public void HandleSelection(ItemSelection selection)
        {
            string tempfile = Path.Combine(Path.GetTempPath(), selection.AttachmentFilename);
            int i = 2;
            while (File.Exists(tempfile))
            {
                tempfile =Path.Combine(Path.GetTempPath(), Path.GetFileNameWithoutExtension(tempfile) + i + Path.GetExtension(tempfile));
                i++;
            }

            SaveURLToDisk(new Uri(selection.ItemURL + "?attachment.uuid=" + selection.AttachmentUuid), tempfile);

            //open it up in associated application
            DocumentWrapper doc = Integ.Open(tempfile);

            //save metadata if necessary
            DocumentMetadata meta = doc.Metadata;
            if (meta.ItemName == null)
            {
                meta.ItemName = selection.ItemName;
            }
            if (meta.ItemUuid == null)
            {
                meta.ItemUuid = selection.ItemUuid;
            }
            if (meta.Keywords == null)
            {
                meta.Keywords = selection.Keywords;
            }
            if (meta.AttachmentFilename == null)
            {
                meta.AttachmentFilename = selection.AttachmentFilename;
            }
            meta.OwnerId = Soap.LoggedInUser;
            Integ.AssociateMetadata(doc, meta);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="profile"></param>
        /// <returns>false to invalidate the change request</returns>
        public bool ProfileChanged(Profile oldProfile, Profile newProfile)
        {
            //MakeNewSoap(Utils.CookieJar);
            _Soap = null;
            return true;
        }


        public void StartLoginSession(CookieContainer cookieJar)
        {
            //MakeNewSoap(cookieJar);
            _Soap = null;
        }


        private void SaveURLToDisk(Uri URL, string filename)
        {
            //there shouldn't be a way of getting here *without* a cookie!
            try
            {
                if (!EnsureLogin())
                {
                    return;
                }
            }
            catch (NoProfileException)
            {
                return;
            }
            catch (WebException)
            {
                Utils.Alert(String.Format(BAD_URL_MESSAGE, InstitutionURL));
                return;
            }
            
            using (Stream stream = Utils.LoadURL(URL))
            {
                using (
                FileStream file = new FileStream(filename, FileMode.Create))
                {
                    byte[] buffer = new byte[65000];

                    int bytesRead;
                    while ((bytesRead = stream.Read(buffer, 0, buffer.Length)) > 0)
                    {
                        file.Write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <exception cref="WebException">As thrown by LoggedIn</exception>
        private bool EnsureLogin()
        {
            if (!LoggedIn)
            {
                return Singletons.BrowseForm.ShowLogin(InstitutionURL + "logon.do");
            }
            return true;
        }

        private class NoProfileException : InvalidOperationException
        {
            public NoProfileException(string msg)
                : base(msg)
            {
            }
        }
    }
}
