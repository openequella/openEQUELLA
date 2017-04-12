using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;
using System.IO;

namespace Equella
{
    static class Constants
    {
        public static readonly Missing MISSING = System.Reflection.Missing.Value;
        public const string APP_NAME = "EQUELLA Office Scrapbook Integration";

        private static readonly string ADDIN_PREF_FOLDER = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), Constants.APP_NAME);

        public static DirectoryInfo AddInPreferenceFolder
        {
            get
            {
                if (!Directory.Exists(ADDIN_PREF_FOLDER))
                {
                    return Directory.CreateDirectory(ADDIN_PREF_FOLDER);
                }
                return new DirectoryInfo(ADDIN_PREF_FOLDER);
            }
        }
    }
}
