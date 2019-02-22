package com.tle.web.filemanager.applet.actions;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.filemanager.common.FileInfo;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;

public final class Utils {
  public static boolean confirmOverwrite(Component parent, String messageKey) {
    Object[] options =
        new Object[] {
          CurrentLocale.get("common.overwrite.yes"), // $NON-NLS-1$
          CurrentLocale.get("common.overwrite.no"),
        }; //$NON-NLS-1$
    if (CurrentLocale.isRightToLeft()) {
      parent.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }
    return JOptionPane.showOptionDialog(
            parent,
            CurrentLocale.get(messageKey),
            CurrentLocale.get("common.overwrite.title"),
            JOptionPane.YES_NO_OPTION, // $NON-NLS-1$
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1])
        == JOptionPane.YES_OPTION;
  }

  /**
   * Now case insensitive!
   *
   * @param files
   * @param infos
   * @return
   */
  public static boolean filenamesClash(File[] files, List<FileInfo> infos) {
    return filenamesClashP(
        Lists.newArrayList(
            Collections2.transform(
                Lists.newArrayList(files),
                new Function<File, String>() {
                  @Override
                  public String apply(File info) {
                    return info.getName().toLowerCase();
                  }
                })),
        infos);
  }

  /**
   * Now case insensitive!
   *
   * @param files
   * @param infos
   * @return
   */
  public static boolean filenamesClash(String[] files, List<FileInfo> infos) {
    return filenamesClashP(
        Lists.transform(
            Arrays.asList(files),
            new Function<String, String>() {
              @Override
              public String apply(String filename) {
                return filename.toLowerCase();
              }
            }),
        infos);
  }

  /**
   * @param files Must be lowercase filenames
   * @param infos
   * @return
   */
  private static boolean filenamesClashP(List<String> files, List<FileInfo> infos) {
    return !Collections.disjoint(
        files,
        Lists.transform(
            infos,
            new Function<FileInfo, String>() {
              @Override
              public String apply(FileInfo info) {
                return info.getName().toLowerCase();
              }
            }));
  }

  private Utils() {
    throw new Error();
  }
}
