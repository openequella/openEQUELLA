using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Equella.Office
{
    public class DocumentMetadata
    {
        public string ItemUuid { get; set; }
        public string ItemName { get; set; }
        public string Keywords { get; set; }
        public string AttachmentFilename { get; set; }
        public string DocumentUuid { get; set; }
        public string OwnerId { get; set; }
    }
}
