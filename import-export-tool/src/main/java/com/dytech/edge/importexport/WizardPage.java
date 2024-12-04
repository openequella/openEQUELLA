package com.dytech.edge.importexport;

import javax.swing.JPanel;

public abstract class WizardPage extends JPanel {
  protected SharedData data;
  protected WizardFrame frame;

  public WizardPage(SharedData data) {
    this.data = data;
  }

  public abstract boolean onNext();

  public abstract boolean onBack();

  public abstract void onShow();

  public void setFrame(WizardFrame frame) {
    this.frame = frame;
  }

  public SharedData getData() {
    return data;
  }
}
