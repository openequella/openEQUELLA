using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Web;
using System.Security.Cryptography;
using System.IO;
using System.Reflection;
using System.Windows.Forms;
using System.Net;

namespace Equella.Util
{
    public static class Utils
    {
        private static CookieContainer _CookieJar;
        public static CookieContainer CookieJar
        {
            get
            {
                if (_CookieJar == null)
                {
                    _CookieJar = new CookieContainer();
                }
                return _CookieJar;
            }
        }

        public static void SetCookie(Uri uri, string cookie)
        {
            CookieJar.SetCookies(uri, cookie);
        }

        public static Stream LoadResource(string resource)
        {
            Assembly assembly = Assembly.GetExecutingAssembly();
            return assembly.GetManifestResourceStream(
                "Equella.Resources." + resource);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="URL"></param>
        /// <returns></returns>
        /// <exception cref="Equella.HttpException" />
        public static Stream LoadURL(Uri URL)
        {
            return LoadURL(URL, true);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="URL"></param>
        /// <param name="followRedirects"></param>
        /// <returns></returns>
        /// <exception cref="Equella.HttpException" />
        public static Stream LoadURL(Uri URL, bool followRedirects)
        {
            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(URL);
            req.CookieContainer = CookieJar;
            req.AllowAutoRedirect = followRedirects;
            HttpWebResponse response = (HttpWebResponse)req.GetResponse();
            if (response.StatusCode < HttpStatusCode.OK || response.StatusCode > HttpStatusCode.PartialContent)
            {
                throw new HttpException { Code = response.StatusCode };
            }
            return response.GetResponseStream();
        }


        public static void Alert(string message)
        {
            MessageBox.Show(message, Constants.APP_NAME);
        }

        public static bool Confirm(string message)
        {
            DialogResult result = MessageBox.Show(message, Constants.APP_NAME, MessageBoxButtons.OKCancel);
            return result == DialogResult.OK;
        }

        public static void ShowError(Exception e)
        {
            new ErrorDialog().ShowError(e);
        }
    }
}
