/*
Copyright (c) 2011, EQUELLA
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of EQUELLA nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
using System;
using System.Collections;
using System.Configuration;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.UI;
using System.Web.UI.HtmlControls;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;
using System.Xml.Linq;
using System.Xml;
using System.IO;
using System.Collections.Generic;

namespace EQUELLA
{
    public partial class contribute : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            if (!IsPostBack)
            {
                EQUELLASOAP equella = new EQUELLASOAP(ConfigurationManager.AppSettings["username"],
                    ConfigurationManager.AppSettings["password"]);

                XElement collectionsXml = equella.ContributableCollections();
                List<Collection> collections = new List<Collection>();
                foreach (XElement collectionNode in collectionsXml.Elements("itemdef"))
                {
                    string collectionUuid = (string)collectionNode.Element("uuid");
                    collections.Add(new Collection { Name = (string)collectionNode.Element("name"), Uuid = collectionUuid });
                }
                collections.Sort(new Comparison<Collection>(sortCollection));

                foreach (Collection collection in collections)
                {
                    lstCollections.Items.Add(new ListItem(collection.Name, collection.Uuid));
                }

                equella.Logout();
            }
        }

        private int sortCollection(Collection c1, Collection c2)
        {
            return c1.Name.CompareTo(c2.Name);
        }

        protected void cmdSubmit_Click(object sender, EventArgs e)
        {
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // EQUELLA SOAP Contribution Code
            /////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Create the item on the server.  The general process is: 
            // 1. newItem (creates a new item in the staging folder, doesn't get committed until saveItem is called)
            // 2. set metadata on the item XML (some are system fields e.g. /xml/item/attachments, others could be custom schema fields)
            // 3. saveItem (commits the changes to a previous newItem or editItem call)

            string collectionUuid = lstCollections.SelectedValue;
            string itemName = txtItemName.Text;
            string itemDescription = txtItemDescription.Text;
            string attachmentDescription = txtAttachmentDescription.Text;
            int filesize = fileAttach.FileBytes.Length;

            EQUELLASOAP equella = new EQUELLASOAP(
                    ConfigurationManager.AppSettings["username"],
                    ConfigurationManager.AppSettings["password"]);
            XElement itemXml = equella.NewItem(collectionUuid);
            XElement item = itemXml.Element("item");

            //set item name and description
            string stagingUuid = (string)item.Element("staging");

            //name and description xpaths are collection dependent!  You need to find the correct xpath to use for the name and description nodes
            XElement collection = equella.GetCollection(collectionUuid);
            XElement schema = equella.GetSchema((string)collection.Element("schemaUuid"));

            string itemNameXPath = (string)schema.Element("itemNamePath");
            string itemDescriptionXPath = (string)schema.Element("itemDescriptionPath");
            itemXml.ElementOrCreateXpath(itemNameXPath).SetValue(itemName);
            itemXml.ElementOrCreateXpath(itemDescriptionXPath).SetValue(itemDescription);

            //check attachments
            if (fileAttach.HasFile && filesize > 0)
            {
                string attachmentFilename = Path.GetFileName(fileAttach.FileName);
                equella.UploadFile(stagingUuid, attachmentFilename, fileAttach.FileBytes);

                //create the attachment object on the item
                XElement attachmentsNode = item.ElementOrCreate("attachments");

                XElement attachmentNode = new XElement("attachment");
                attachmentNode.Add(new XAttribute("type", "local"));
                attachmentNode.Add(new XElement("file", attachmentFilename));
                attachmentNode.Add(new XElement("description", attachmentDescription));
                attachmentNode.Add(new XElement("size", filesize.ToString()));
                attachmentsNode.Add(attachmentNode);
            }

            //save and submit
            equella.SaveItem(itemXml, true);

            feedback.Text = "<h3>Item \"" + itemName + "\" contributed</h3>";

            equella.Logout();
        }


        private class Collection
        {
            public string Uuid { get; set; }
            public string Name { get; set; }
        }
    }
}