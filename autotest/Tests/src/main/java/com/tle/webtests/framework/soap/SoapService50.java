package com.tle.webtests.framework.soap;

/**
 * Methods added in version 5.0. Note that this interface fully encompasses all of the methods
 * already in SoapService41
 */
public interface SoapService50 extends SoapService41 {
  /**
   * * Determines if there is an existing item with the supplied itemUuid and itemVersion
   *
   * @param itemUuid UUID of the item to look for
   * @param itemVersion The version of the item to look for
   * @return true if the item can be found (by the current user)
   */
  boolean itemExists(String itemUuid, int itemVersion);

  /**
   * A list of all the files stored with an item. Some, or all, of these files may be attachments on
   * the item.
   *
   * @param itemUuid The uuid of the item to list the files of
   * @param itemVersion The version of the item to list the files of
   * @param path The folder to search through. This would be null or "" for the top level.
   * @return A list of filenames, relative to the item's URL
   */
  String[] getItemFilenames(String itemUuid, int itemVersion, String path, boolean system);
}
