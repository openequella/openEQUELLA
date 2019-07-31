package com.tle.web.filemanager.applet;

import com.tle.web.appletcommon.AbstractAppletLauncher;
import com.tle.web.filemanager.applet.FileManager.Parameters;
import com.tle.web.filemanager.applet.backend.AbstractRemoteBackendImpl;
import javax.swing.JComponent;

@SuppressWarnings("nls")
public class AppletLauncher extends AbstractAppletLauncher {
  private static final String BACKEND_PARAMETER = PARAMETER_PREFIX + "BACKEND";
  private static final String WIZARD_PARAMETER = PARAMETER_PREFIX + "WIZARD";
  private static final String AUTOMARK_PARAMETER = PARAMETER_PREFIX + "AUTOMARK";

  @Override
  protected JComponent initAndCreateRootComponent() throws Exception {
    Parameters params =
        new Parameters(getEndpoint(), getBackendClass(), getWizardId(), isAutoMarkAsResource());

    return new FileManager(params).getComponent();
  }

  @Override
  protected String[] getBundleGroups() {
    return new String[] {"file-manager"};
  }

  @Override
  protected String getI18nKeyPrefix() {
    return "com.tle.web.applet.filemanager.";
  }

  private Class<? extends AbstractRemoteBackendImpl> getBackendClass()
      throws ClassNotFoundException {
    return Class.forName(getParameter(BACKEND_PARAMETER))
        .asSubclass(AbstractRemoteBackendImpl.class);
  }

  private boolean isAutoMarkAsResource() {
    return Boolean.valueOf(getParameter(AUTOMARK_PARAMETER));
  }

  private String getWizardId() {
    return getParameter(WIZARD_PARAMETER);
  }
}
