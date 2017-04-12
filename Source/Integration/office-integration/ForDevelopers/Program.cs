using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Win32;
using System.Runtime.InteropServices;
using System.Diagnostics;

namespace ForDevelopers
{
    class Program
    {
        [DllImport("kernel32.dll", SetLastError = true, CallingConvention = CallingConvention.Winapi)]
        [return: MarshalAs(UnmanagedType.Bool)]
        public static extern bool IsWow64Process([In] IntPtr hProcess, [Out] out bool lpSystemInfo);

        static void Main(string[] args)
        {
            new Program().registerModule();
        }

        public void registerModule()
        {
            makeKeyIfNotExist("SOFTWARE\\Wow6432Node\\Microsoft\\Office\\Word\\Addins\\Equella.EquellaOfficeScrapbookIntegration");
            makeKeyIfNotExist("SOFTWARE\\Microsoft\\Office\\Word\\Addins\\Equella.EquellaOfficeScrapbookIntegration");
        }

        private void makeKeyIfNotExist(string key)
        {
            RegistryKey existingKey = Registry.CurrentUser.OpenSubKey(key);
            if (existingKey == null)
            {
                RegistryKey addinKey = Registry.CurrentUser.CreateSubKey(key);
                if (addinKey == null)
                {
                    Console.WriteLine("Null Masterkey!");
                }
                else
                {
                    try
                    {
                        addinKey.SetValue("CommandLineSafe", 0, RegistryValueKind.DWord);
                        addinKey.SetValue("Description", "EQUELLA Office Scrapbook Addin", RegistryValueKind.String);
                        addinKey.SetValue("FriendlyName", "EQUELLA Office Scrapbook Addin", RegistryValueKind.String);
                        addinKey.SetValue("LoadBehavior", 3, RegistryValueKind.DWord);
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine(ex.Message);
                    }
                    finally
                    {
                        addinKey.Close();
                    }
                }
            }
            else
            {
                existingKey.Close();
            }
        }

        private bool Is64Bit()
        {
            if (IntPtr.Size == 8 || (IntPtr.Size == 4 && Is32BitProcessOn64BitProcessor()))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        private bool Is32BitProcessOn64BitProcessor()
        {
            bool retVal;

            IsWow64Process(Process.GetCurrentProcess().Handle, out retVal);

            return retVal;
        }
    }
}
