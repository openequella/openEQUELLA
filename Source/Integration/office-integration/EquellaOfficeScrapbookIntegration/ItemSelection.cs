using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Equella
{
    public delegate void OnSelectionHandler(ItemSelection selection);

    public interface ItemSelection
    {
        string ItemName
        {
            get;
        }

        string Keywords
        {
            get;
        }

        string ItemUuid
        {
            get;
            set;
        }

        string ItemURL
        {
            get;
            set;
        }

        string AttachmentFilename
        {
            get;
        }

        string AttachmentUuid
        {
            get;
            set;
        }
    }
}
