using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Equella
{
    /// <summary>
    /// Any method marked with EntryPoint *must* catch all exceptions and not let them trickle back to Office.  
    /// If Office sees your exceptions it will prompt the user to disable the Add-in.  So hide those exceptions!
    /// </summary>
    public class EntryPoint : Attribute
    {
    }
}
