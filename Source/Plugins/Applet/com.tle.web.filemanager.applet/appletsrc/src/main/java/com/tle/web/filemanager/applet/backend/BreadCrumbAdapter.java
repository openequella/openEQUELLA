package com.tle.web.filemanager.applet.backend;

import com.tle.web.filemanager.common.FileInfo;
import java.util.ArrayList;
import java.util.List;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbBarCallBack;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbBarException;
import org.pushingpixels.flamingo.api.bcb.BreadcrumbItem;
import org.pushingpixels.flamingo.api.common.StringValuePair;

/** @author Nicholas Read */
public class BreadCrumbAdapter extends BreadcrumbBarCallBack<FileInfo> {
  private final Backend backend;

  public BreadCrumbAdapter(Backend backend) {
    this.backend = backend;
  }

  public List<StringValuePair<FileInfo>> getPathChoices(List<BreadcrumbItem<FileInfo>> paths)
      throws BreadcrumbBarException {
    String folderPath = ""; // $NON-NLS-1$
    if (paths != null) {
      folderPath = paths.get(paths.size() - 1).getData().getFullPath();
    }

    ArrayList<StringValuePair<FileInfo>> results = new ArrayList<>();
    for (FileInfo child : backend.listFiles(folderPath)) {
      if (child.isDirectory()) {
        StringValuePair<FileInfo> kvp = new StringValuePair<>(child.getName(), child);
        results.add(kvp);
      }
    }
    return results;
  }
}
