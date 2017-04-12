using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Equella.GUI;
using Equella.Config;

namespace Equella
{
    /// <summary>
    /// In the absence of using Spring.NET
    /// </summary>
    public class Singletons
    {
        private static StandardForm _BrowseForm;
        /// <summary>
        /// The standard login and scrapbook browse form.
        /// </summary>
        public static StandardForm BrowseForm
        {
            get
            {
                if (_BrowseForm == null)
                {
                    _BrowseForm = new StandardForm();
                }
                return _BrowseForm;
            }
        }

        /// <summary>
        /// Should only be set by the module itself
        /// </summary>
        public static EquellaOfficeScrapbookIntegration Module
        {
            get;
            set;
        }


        private static ProfileConfiguration _Config;
        public static ProfileConfiguration Config
        {
            get
            {
                if (_Config == null)
                {
                    _Config = new ProfileConfiguration();
                }
                return _Config;
            }
        }
    }
}
