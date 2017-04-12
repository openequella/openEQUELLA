using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Equella.Office
{
    public delegate void DocumentEventHandler();

    public interface Integration
    {
        event DocumentEventHandler DocumentClosed;

        event DocumentEventHandler DocumentOpened;

        event DocumentEventHandler DocumentSaved;

        /// <summary>
        /// 
        /// </summary>
        /// <param name="docHandle"></param>
        /// <param name="docFullPath"></param>
        /// <returns></returns>
        DocumentWrapper Save(DocumentWrapper docHandle, string docFullPath);

        DocumentWrapper Open(string filename);

        byte[] ReadFile(DocumentWrapper docHandle);

        void AssociateMetadata(DocumentWrapper docHandle, DocumentMetadata meta);

        DocumentWrapper CurrentDocument
        {
            get;
        }

        string DefaultDocumentName
        {
            get;
        }

        string[] MimeTypes
        {
            get;
        }
    }
}
