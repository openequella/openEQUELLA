using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.IO;
using System.Net;
using System.Web;
using System.Runtime.InteropServices;
using Equella.Util;

namespace Equella.GUI
{
    public partial class StandardForm : Form
    {
        private bool showLogin;
        private bool showScrapbookAfterLogin;
        public ItemSelection Selection
        {
            get;
            set;
        }

        [DllImport("wininet.dll", SetLastError = true)]
        public static extern bool InternetGetCookie(
          string URL, string cookieName, StringBuilder cookieData, ref int size);


        public StandardForm()
        {
            InitializeComponent();

            Layout += new LayoutEventHandler(StandardForm_Layout);

            browseScrapbookPanel.SelectionHandler = new OnSelectionHandler(OnScrapbookSelection);
        }

        #region Public methods

        public bool ShowLogin(string loginURL)
        {
            SetCursor(Cursors.WaitCursor);

            Selection = null;
            SetupLogin(loginURL, false);
            
            SetCursor(Cursors.Default);

            DialogResult dialogResult = ShowDialog();
            if (dialogResult == DialogResult.OK)
            {
                return true;
            }
            return false;
        }

        public void ShowBrowseScrapbook(string loginURL)
        {
            SetCursor(Cursors.WaitCursor);

            Selection = null;
            if (!Singletons.Module.LoggedIn)
            {
                SetupLogin(loginURL, true);
            }
            else
            {
                SetupScrapbook();
            }

            SetCursor(Cursors.Default);

            ShowDialog();
        }

        #endregion


        private void SetCursor(Cursor c)
        {
            Cursor = c;
            //webBrowser1.Cursor = c;
            browseScrapbookPanel.Cursor = c;
        }


        private void SetupLogin(string loginURL, bool showScrapbookAfterLogin)
        {
            browseScrapbookPanel.Hide();
            webBrowser1.Hide();

            showLogin = true;
            this.showScrapbookAfterLogin = showScrapbookAfterLogin;
            
            webBrowser1.Url = new Uri(loginURL);
            webBrowser1.Show();
        }

        private void SetupScrapbook()
        {
            webBrowser1.Hide();
            browseScrapbookPanel.Hide();

            showLogin = false;

            browseScrapbookPanel.Show();
            browseScrapbookPanel.Search();
            
        }

        private static string GetUriCookieContainer(Uri uri)
        {
            // Determine the size of the cookie
            int datasize = 256;
            StringBuilder cookieData = new StringBuilder(datasize);

            if (!InternetGetCookie(uri.ToString(), null, cookieData,
              ref datasize))
            {
                if (datasize < 0)
                    return null;

                // Allocate stringbuilder large enough to hold the cookie
                cookieData = new StringBuilder(datasize);
                if (!InternetGetCookie(uri.ToString(), null, cookieData,
                  ref datasize))
                    return null;
            }
            return cookieData.ToString();
        }

        private void OnScrapbookSelection(ItemSelection selection)
        {
            Selection = selection;
            Hide();
        }

        #region Browser Events

        private void webBrowser1_Navigated(object sender, WebBrowserNavigatedEventArgs e)
        {
            SetCursor(Cursors.Default);

            Uri uri = webBrowser1.Url;
            string uriString = uri.ToString();
            if (showLogin && uri != null &&  uriString != "about:blank" && !uriString.Contains("logon.do"))
            {
                string cookieData = GetUriCookieContainer(uri);
                if (cookieData != null)
                {
                    string cookie = cookieData;
                    Utils.SetCookie(new Uri(uri.Scheme + "://" + uri.Host), cookie);
                    Singletons.Module.StartLoginSession(Utils.CookieJar);

                    webBrowser1.Url = new Uri("about:blank");

                    if (showScrapbookAfterLogin)
                    {
                        SetupScrapbook();
                    }
                    else
                    {
                        this.DialogResult = DialogResult.OK; 
                        Close();
                    }
                }
            }
        }

        private void webBrowser1_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
        }

        private void webBrowser1_Navigating(object sender, WebBrowserNavigatingEventArgs e)
        {
            SetCursor(Cursors.WaitCursor);
        }

        #endregion

        private void StandardForm_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Escape)
            {
                Close();
            }
        }

        private void StandardForm_Layout(object sender, LayoutEventArgs e)
        {
            int w = ClientSize.Width;
            int h = ClientSize.Height;
            webBrowser1.SetBounds(0,0, w, h);
            browseScrapbookPanel.SetBounds(0, 0, w, h);
        }
    }
}
