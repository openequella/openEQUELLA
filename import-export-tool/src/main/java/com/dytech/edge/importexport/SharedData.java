package com.dytech.edge.importexport;

import com.dytech.edge.importexport.types.Item;
import com.dytech.edge.importexport.types.ItemDef;
import java.io.File;
import java.net.URL;
import java.util.List;

public class SharedData {
  public static final int SAVE_IN_NATIVE = 0;
  public static final int SAVE_APPLYING_XSLT = 1;
  public static final int SAVE_AS_IMS = 2;

  private int saveAs = SAVE_IN_NATIVE;
  private URL institutionUrl;
  private SoapSession soapSession;
  private File saveFolder;
  private String xslt;
  private List<Item> items;
  private List<ItemDef> itemDefs;

  private Completion completion;

  public int getSaveAs() {
    return saveAs;
  }

  public void setSaveAs(int saveAs) {
    this.saveAs = saveAs;
  }

  public URL getInstitutionUrl() {
    return institutionUrl;
  }

  public void setInstitutionUrl(URL institutionUrl) {
    this.institutionUrl = institutionUrl;
  }

  public SoapSession getSoapSession() {
    return soapSession;
  }

  public void setSoapSession(SoapSession soapSession) {
    this.soapSession = soapSession;
  }

  public File getSaveFolder() {
    return saveFolder;
  }

  public void setSaveFolder(File saveFolder) {
    this.saveFolder = saveFolder;
  }

  public String getXslt() {
    return xslt;
  }

  public void setXslt(String xslt) {
    this.xslt = xslt;
  }

  public List<Item> getItems() {
    return items;
  }

  public void setItems(List<Item> items) {
    this.items = items;
  }

  public List<ItemDef> getItemDefs() {
    return itemDefs;
  }

  public void setItemDefs(List<ItemDef> itemDefs) {
    this.itemDefs = itemDefs;
  }

  public Completion getCompletion() {
    return completion;
  }

  public void setCompletion(Completion completion) {
    this.completion = completion;
  }

  public static class Completion {
    private final int successful;
    private final int failed;
    private final String message;

    public Completion(int succesful, int failed, String message) {
      this.successful = succesful;
      this.failed = failed;
      this.message = message;
    }

    public int getSuccessful() {
      return successful;
    }

    public int getFailed() {
      return failed;
    }

    public String getMessage() {
      return message;
    }
  }
}
