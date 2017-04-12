using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Reflection;
using System.IO;
using System.Net;
using Equella.Util;
using Microsoft.Win32;
using System.Runtime.InteropServices;

namespace Equella.GUI
{
    public partial class ResultPanel : UserControl, ItemSelection
    {
        [DllImport("shell32.dll", CharSet = CharSet.Auto)]
        private static extern uint ExtractIconEx(string szFileName, int nIconIndex, IntPtr[] phiconLarge, IntPtr[] phiconSmall, uint nIcons);
        [DllImport("user32.dll", EntryPoint = "DestroyIcon", SetLastError = true)]
        private static /*unsafe*/ extern int DestroyIcon(IntPtr hIcon);

        private static IDictionary<string, Bitmap> extensionIconCache = new Dictionary<string, Bitmap>();

        public ResultPanel()
        {
            InitializeComponent();
        }

        public void SetThumbnail(string filename, string thumbURL)
        {
            Bitmap thumb = null;
            if (filename != null)
            {
                thumb = GetThumbFromExtension(filename);
            }
            /*
            if (thumb == null && thumbURL != null)
            {
                //TODO: some sort of caching
                using (Stream stream = Utils.LoadURL(new Uri(thumbURL)))
                {
                    thumb = new Bitmap(stream);
                }
            }
            */
            

            picThumb.Image = thumb;
        }

        private Bitmap GetThumbFromExtension(string filename)
        {
            string ext = Path.GetExtension(filename);
            if (extensionIconCache.ContainsKey(ext))
            {
                return extensionIconCache[ext];
            }
            else
            {
                RegistryKey rootKey = Registry.ClassesRoot;
                RegistryKey extKey = rootKey.OpenSubKey(ext, RegistryKeyPermissionCheck.ReadSubTree, System.Security.AccessControl.RegistryRights.ReadKey);
                if (extKey != null)
                {
                    string typeName = (string)extKey.GetValue("", null);
                    extKey.Close();
                    if (!string.IsNullOrEmpty(typeName))
                    {
                        RegistryKey defaultIconKey = rootKey.OpenSubKey(typeName + "\\DefaultIcon", RegistryKeyPermissionCheck.ReadSubTree, System.Security.AccessControl.RegistryRights.ReadKey);
                        if (defaultIconKey != null)
                        {
                            string defaultIconPath = (string)defaultIconKey.GetValue("", null);
                            defaultIconKey.Close();
                            defaultIconPath = defaultIconPath.Replace("\"", "");

                            string path = defaultIconPath;
                            int iconIndex = 0;
                            int commaIndex = defaultIconPath.LastIndexOf(",");
                            if (commaIndex > 0)
                            {
                                path = path.Substring(0, commaIndex);
                                iconIndex = Convert.ToInt32(defaultIconPath.Substring(commaIndex + 1));
                            }

                            IntPtr[] iconHandles = new IntPtr[] { IntPtr.Zero };
                            uint readIconCount = ExtractIconEx(path, iconIndex, iconHandles, null, 1);
                            if (readIconCount > 0)
                            {
                                Icon icon = Icon.FromHandle(iconHandles[0]);
                                Bitmap bitmap = icon.ToBitmap();
                                DestroyIcon(iconHandles[0]);

                                extensionIconCache.Add(ext, bitmap);
                                return bitmap;
                            }
                        }
                    }
                }
            }
            return null;
        }

        public string ItemName
        {
            get
            {
                return lblTitle.Text;
            }

            set
            {
                lblTitle.Text = value;
            }
        }

        public string Keywords
        {
            get
            {
                return lblKeywords.Text;
            }

            set
            {
                lblKeywords.Text = value;
            }
        }

        public DateTime LastModifiedDate
        {
            //get
            //{
            //    return lblKeywords.Text;
            //}

            set
            {
                lblLastModified.Text = String.Format("{0:f}", value);
            }
        }

        public string AttachmentFilename
        {
            get
            {
                return lblFilename.Text;
            }

            set
            {
                lblFilename.Text = value;
            }
        }

        public string ItemUuid
        {
            get;
            set;
        }

        public string ItemURL
        {
            get;
            set;
        }

        public string AttachmentUuid
        {
            get;
            set;
        }
    }
}
