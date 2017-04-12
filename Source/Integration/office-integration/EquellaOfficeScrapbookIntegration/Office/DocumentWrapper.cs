using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Equella.Office
{
    public interface DocumentWrapper
    {
        object Document
        {
            get;
        }

        string Title
        {
            get;
        }

        DocumentMetadata Metadata
        {
            get;
        }

        bool Dirty 
        { 
            get; 
        }

        bool New 
        { 
            get; 
        }

        string FileName
        {
            get;
        }
    }
}
