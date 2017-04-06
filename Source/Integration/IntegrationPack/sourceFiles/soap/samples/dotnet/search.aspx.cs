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
using System.Text;

namespace EQUELLA
{
    public partial class search : System.Web.UI.Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            if (!IsPostBack)
            {
                chkOnlylive.Checked = true;
                txtOffset.Text = "0";
                txtMaxResults.Text = "10";
            }
        }

        protected void cmdSearch_Click(object sender, EventArgs e)
        {
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // EQUELLA SOAP Searching Code
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            EQUELLASOAP equella = new EQUELLASOAP(
                    ConfigurationManager.AppSettings["username"],
                    ConfigurationManager.AppSettings["password"]);

            XElement searchResultsXml = equella.SearchItems(
                txtQuery.Text, null, txtWhere.Text, chkOnlylive.Checked,
                Convert.ToInt32(lstSortType.SelectedValue), chkReverseSort.Checked, Convert.ToInt32(txtOffset.Text), Convert.ToInt32(txtMaxResults.Text)
                );

            StringBuilder results = new StringBuilder();

            results.Append("<hr><h3>Searching EQUELLA for \"").Append(txtQuery.Text).Append("\"</h3>");
            results.Append("<br>results returned: " + searchResultsXml.Attribute("count").Value);
            results.Append("<br>results available: " + (int)searchResultsXml.Element("available"));
            results.Append("<br>results: <ul>");

            bool useTokens = Boolean.Parse(ConfigurationManager.AppSettings["useTokens"]);
            string tokenPostfix = "";
            if (useTokens)
            {
                tokenPostfix = "?token=" + EQUELLASOAP.GenerateToken(
                    ConfigurationManager.AppSettings["tokenUser"],
                    ConfigurationManager.AppSettings["sharedSecretId"],
                    ConfigurationManager.AppSettings["sharedSecretValue"]);
            }

            foreach (XElement result in searchResultsXml.Elements("result"))
            {
                XElement resultItem = result.Element("xml").Element("item");
                results.Append("<li><a href=\"" + (string)resultItem.Element("url") + tokenPostfix + "\">"
                    + (string)resultItem.Element("name") + "</a></li>");
            }
            results.Append("</ul>");

            feedback.Text = results.ToString();
        }
    }
}