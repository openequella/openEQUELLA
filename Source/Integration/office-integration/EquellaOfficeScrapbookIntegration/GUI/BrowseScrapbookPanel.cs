using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.Xml.Linq;
using Equella.Office;
using Equella.Config;
using Equella.Util;

namespace Equella.GUI
{
    public partial class BrowseScrapbookPanel : UserControl
    {
        private const int CONTROL_PADDING_LEFT = 15;
        private const int CONTROL_PADDING_RIGHT = 15;
        private const int CONTROL_PADDING_TOP = 15;
        private const int CONTROL_PADDING_BOTTOM = 15;

        private const int BUTTON_MARGIN_LEFT = 15;

        private const int SEARCH_BAR_TOP = 49;

        private const string TITLE_PREFIX = "Searching scrapbook of ";

        private readonly KeyValuePair<int, string> SORT_LAST_MODIFIED = new KeyValuePair<int, string>(1, "Last Modified");
        private readonly KeyValuePair<int, string> SORT_RELEVANCE = new KeyValuePair<int, string>(0, "Relevance");
        private readonly KeyValuePair<int, string> SORT_TITLE = new KeyValuePair<int, string>(2, "Title");

        public BrowseScrapbookPanel()
        {
            InitializeComponent();
            Layout += new LayoutEventHandler(OnLayout);
            listSortBy.ValueMember = "Key";
            listSortBy.DisplayMember = "Value";
            listSortBy.Items.Clear();
            listSortBy.Items.Add(SORT_LAST_MODIFIED);
            listSortBy.Items.Add(SORT_RELEVANCE);
            listSortBy.Items.Add(SORT_TITLE);
            listSortBy.SelectedItem = SORT_LAST_MODIFIED;
            listSortBy.SelectedIndexChanged += new System.EventHandler(listSortBy_SelectedIndexChanged);
        }

        public OnSelectionHandler SelectionHandler
        {
            set
            {
                resultsPanel.OnSelection += value;
            }
        }

        private void OnLayout(object sender, LayoutEventArgs e)
        {
            int newControlWidth = ClientSize.Width;
            int newControlHeight = ClientSize.Height;

            resultsPanel.SuspendLayout();
            resultsPanel.SetBounds(
                                CONTROL_PADDING_LEFT, 
                                resultsPanel.Top,
                                newControlWidth - (CONTROL_PADDING_LEFT + CONTROL_PADDING_RIGHT), 
                                newControlHeight - (resultsPanel.Top + CONTROL_PADDING_BOTTOM));
            resultsPanel.ResumeLayout();
            resultsPanel.PerformLayout();

            int searchBarTop = CONTROL_PADDING_TOP + linkProfile.Size.Height + CONTROL_PADDING_TOP;

            txtSearch.SuspendLayout();
            txtSearch.Location = new Point(CONTROL_PADDING_LEFT,searchBarTop);
            txtSearch.Width = newControlWidth - (CONTROL_PADDING_LEFT + CONTROL_PADDING_RIGHT + BUTTON_MARGIN_LEFT + cmdSearch.Width);
            txtSearch.ResumeLayout();
            txtSearch.PerformLayout();

            listSortBy.SuspendLayout();
            listSortBy.Location = new Point(txtSearch.Right - listSortBy.Width, listSortBy.Top);
            listSortBy.ResumeLayout();
            listSortBy.PerformLayout();

            lblSortBy.SuspendLayout();
            lblSortBy.Location = new Point(listSortBy.Left - lblSortBy.Width - CONTROL_PADDING_LEFT, lblSortBy.Top);
            lblSortBy.ResumeLayout();
            lblSortBy.PerformLayout();

            cmdSearch.Location = new Point(txtSearch.Width + CONTROL_PADDING_LEFT + BUTTON_MARGIN_LEFT, searchBarTop);
        }

        public void Search()
        {
            Cursor = Cursors.WaitCursor;

            Profile profile = Singletons.Config.CurrentProfile;
            linkProfile.Text = TITLE_PREFIX + profile.Name;
            linkProfile.Links.Clear();
            linkProfile.Links.Add(TITLE_PREFIX.Length, profile.Name.Length, profile.URL);

            resultsPanel.ClearResults();

            resultsPanel.SuspendLayout();
            KeyValuePair<int, string> sortBy = (KeyValuePair<int, string>)listSortBy.SelectedItem;
            XElement xml = Singletons.Module.SearchScrapbook(txtSearch.Text, sortBy.Key);
            int resultCount = (int)xml.Attribute("count");
            int available = (int)xml.Element("available");

            if (available > resultCount)
            {
                lblResultCount.Text = String.Format("Showing {0} results ({1} available)", resultCount, available);
            }
            else
            {
                lblResultCount.Text = String.Format("{0} results", resultCount);
            }

            foreach (XElement result in  xml.Elements("result") )
            {
                XElement resultXml = result.Element("xml");
                XElement item = resultXml.Element("item");

                string filename = null;
                string attachmentUuid = null;
                string thumbURL = null;
                XElement attachment = (from a in item.Element("attachments").Elements("attachment") select a).FirstOrDefault();
                if (attachment != null)
                {
                    filename = (string)attachment.Element("file");
                    attachmentUuid = (string)attachment.Element("uuid");
                    thumbURL = (string)attachment.Element("thumbnail");
                }
                string itemURL = (string)item.Element("url");
                if (thumbURL != null)
                {
                    thumbURL = itemURL + thumbURL;
                }
                string lastModifiedString = (string)item.Element("datemodified");
                DateTime lastModified = DateTime.ParseExact(lastModifiedString, "yyyy-MM-ddTHH:mm:sszzz", null);

                resultsPanel.AddResult((string)item.Attribute("id"),itemURL,
                    (string)resultXml.Element("name"),
                    (string)resultXml.Element("keywords"), filename, attachmentUuid, lastModified, thumbURL);
            }
            resultsPanel.ResumeLayout();
            resultsPanel.PerformLayout();

            Cursor = Cursors.Default;
        }

        private void cmdSearch_Click(object sender, EventArgs e)
        {
            Search();
        }

        private void txtSearch_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                Search();
            }
        }

        private void linkProfile_LinkClicked(object sender, LinkLabelLinkClickedEventArgs e)
        {
            string url = (string)e.Link.LinkData;
            try
            {
                System.Diagnostics.Process.Start(url);
            }
            catch (Exception)
            {
                Utils.Alert("There was an error contacting the configured URL.  Perhaps the server is currently unavailable?");
            }
        }

        private void listSortBy_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (Visible)
            {
                Search();
            }
        }
    }
}
