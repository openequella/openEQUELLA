package com.tle.webtests.framework.setup;

import com.google.common.io.Closeables;
import com.tle.webtests.framework.setup.InstitutionModel.InstitutionData;
import com.tle.webtests.framework.setup.gui.ButtonEditor;
import com.tle.webtests.framework.setup.gui.ButtonRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

@SuppressWarnings("serial")
public class Setup extends JPanel {
  // General note on the queries in this class - With the advent of hibernate 5,
  // queries with '?' in them need to be ordinal ( ie `?4` ).  However, this class
  // does not leverage the JPA / Hibernate logic, so we can leave the `?`s as-is.

  private static final String CONNECTION_URL = "jdbc:postgresql://appserver01:5432/autotestsync";
  private JTable institutionTable;
  private InstitutionModel institutionModel;
  private ProgressMonitor progressMonitor;
  private Connection conn;

  public Setup() {

    Properties props = new Properties();
    try {
      props.load(new FileInputStream(new File("config/localserver.properties")));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    File institutionsFolder;
    if (props.containsKey("institutions.folder")) {
      institutionsFolder = new File((String) props.get("institutions.folder"));
    } else {
      institutionsFolder = new File("tests");
    }
    init(institutionsFolder);
  }

  private void init(File institutionsFolder) {
    institutionModel = new InstitutionModel(institutionsFolder);
    institutionTable = new JTable(institutionModel);
    ButtonRenderer cellRenderer = new ButtonRenderer();
    ButtonEditor cellEditor = new ButtonEditor(new JCheckBox());
    TableColumnModel columnModel = institutionTable.getColumnModel();
    TableColumn col = columnModel.getColumn(0);
    col.setPreferredWidth(100);

    col = columnModel.getColumn(1);
    col.setCellRenderer(cellRenderer);
    col.setCellEditor(cellEditor);
    col = columnModel.getColumn(2);
    col.setCellRenderer(cellRenderer);
    col.setCellEditor(cellEditor);
    col = columnModel.getColumn(3);
    col.setCellRenderer(cellRenderer);
    col.setCellEditor(cellEditor);
    col = columnModel.getColumn(4);
    col.setCellRenderer(cellRenderer);
    col.setCellEditor(cellEditor);

    col = columnModel.getColumn(5);
    col.setPreferredWidth(250);
    institutionTable.setRowSelectionAllowed(false);
    institutionTable.setColumnSelectionAllowed(false);

    ActionListener listener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            final InstitutionData inst = (InstitutionData) e.getSource();
            String command = e.getActionCommand();
            if (command.equals("Export")) {
              doExport(inst);
            } else if (command.equals("Import")) {
              doSync(inst);
            } else if (command.equals("Lock")) {
              doLock(inst);
            } else if (command.equals("Unlock")) {
              doUnlock(inst);
            }
          }
        };
    institutionModel.setActionListener(listener);

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JScrollPane scrollPane = new JScrollPane(institutionTable);
    institutionTable.setFillsViewportHeight(true);
    institutionTable.setPreferredScrollableViewportSize(new Dimension(700, 70));
    add(scrollPane, BorderLayout.CENTER);

    updateLockedStatus(institutionModel.getInstitutionRows());
  }

  private Connection getConnection() {
    try {
      if (conn != null && !conn.isClosed()) {
        return conn;
      }

      Properties lockProps = new Properties();
      lockProps.setProperty("user", "equellauser");
      lockProps.setProperty("password", "tle010");
      conn = DriverManager.getConnection(CONNECTION_URL, lockProps);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "An error occured. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
    }
    return conn;
  }

  public void close() {
    if (conn != null) {
      try {
        conn.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  protected void doExport(final InstitutionData inst) {
    String message = "";
    if (!updateLockedStatus(inst)) {
      message =
          "WARNING: This institution is not locked, someone else might lock it while you are using"
              + " it. ";
    } else {
      String user = System.getProperty("user.name");
      if (!user.equalsIgnoreCase(inst.getUser())) {
        message = "WARNING: This institution is locked by someone else. ";
      }
    }

    message += "Are you sure you want to export '" + inst.getShortName() + "'";

    if (JOptionPane.showConfirmDialog(this, message, "Export", JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION) {
      SyncData i2sync = new SyncData(inst.getShortName(), inst.getInstitutionFile());
      doTask(
          new SyncCallback() {
            @Override
            public void callback(SyncToLocalServer syncer, SyncData institutionData) {
              syncer.exportInstitution(inst);
              institutionModel.getRowChangeCallback(inst);
            }
          },
          "Export From Local Server",
          "Exporting",
          Arrays.asList(i2sync));
    }
  }

  protected void doSync(final InstitutionData inst) {
    if (JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sync '" + inst.getShortName() + "'",
            "Synchronize",
            JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION) {
      SyncData i2sync = new SyncData(inst.getShortName(), inst.getInstitutionFile());
      doTask(
          new SyncCallback() {
            @Override
            public void callback(SyncToLocalServer syncer, SyncData institutionData) {
              syncer.importInstitution(inst);
              institutionModel.getRowChangeCallback(inst);
            }
          },
          "Syncing to Local Server",
          "Synchronising",
          Arrays.asList(i2sync));
    }
  }

  protected void doLock(final InstitutionData inst) {

    if (updateLockedStatus(inst)) {
      JOptionPane.showMessageDialog(
          this,
          "This institution is already locked by '"
              + inst.getUser()
              + "' on "
              + inst.getLockedDate());
    } else {
      if (JOptionPane.showConfirmDialog(
              this,
              "Are you sure you want to lock '" + inst.getShortName() + "'",
              "Lock",
              JOptionPane.OK_CANCEL_OPTION)
          == JOptionPane.OK_OPTION) {
        lock(inst);
      }
    }
  }

  protected void doUnlock(final InstitutionData inst) {

    if (!updateLockedStatus(inst)) {
      JOptionPane.showMessageDialog(this, "This institution is not locked");
    } else {
      String user = System.getProperty("user.name");
      String message;
      if (user.equalsIgnoreCase(inst.getUser())) {
        message = "Are you sure you want to unlock '" + inst.getShortName() + "'";
      } else {
        message =
            "WARNING: This institution was not locked by you, it was by '"
                + inst.getUser()
                + "'. Are you sure you want to unlock '"
                + inst.getShortName()
                + "'";
      }
      if (JOptionPane.showConfirmDialog(this, message, "Lock", JOptionPane.OK_CANCEL_OPTION)
          == JOptionPane.OK_OPTION) {
        unlock(inst);
      }
    }
  }

  private void lock(InstitutionData inst) {

    try {
      PreparedStatement st =
          getConnection()
              .prepareStatement(
                  "INSERT INTO locks(institution, locked_by, timestamp) VALUES (?, ?, ?)");
      st.setString(1, inst.getShortName());
      String user = System.getProperty("user.name");
      st.setString(2, user);
      Timestamp date = new Timestamp(new Date().getTime());
      st.setTimestamp(3, date);
      st.executeUpdate();
      st.close();
      inst.setUser(user);
      inst.setLocked(true);
      inst.setLockedDate(date);
      msgHubot(
          "'"
              + System.getProperty("user.name")
              + "' just locked the institution '"
              + inst.getShortName()
              + "'");
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "An error occured. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
    }
  }

  private void unlock(InstitutionData inst) {
    try {
      PreparedStatement st =
          getConnection().prepareStatement("DELETE FROM locks WHERE institution = ?");
      st.setString(1, inst.getShortName());
      st.executeUpdate();
      st.close();
      inst.setUser(null);
      inst.setLocked(false);
      msgHubot(
          "'"
              + System.getProperty("user.name")
              + "' just unlocked the institution '"
              + inst.getShortName()
              + "'");
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "An error occured. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
    }
  }

  private void msgHubot(String msg) {
    final String message = "Sync Tool Update: " + msg;
    javax.swing.SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            InputStream inputStream = null;
            try {
              String target = "http://high-warrior-6993.herokuapp.com/hubot/say";
              HttpClient client = new DefaultHttpClient();
              HttpPost httpPost = new HttpPost(target);
              BasicNameValuePair[] params = {
                new BasicNameValuePair("room", "developers@conference.wbowling.info"),
                new BasicNameValuePair("message", message),
              };
              UrlEncodedFormEntity urlEncodedFormEntity =
                  new UrlEncodedFormEntity(Arrays.asList(params));
              urlEncodedFormEntity.setContentEncoding(HTTP.UTF_8);
              httpPost.setEntity(urlEncodedFormEntity);
              HttpResponse response = client.execute(httpPost);
              inputStream = response.getEntity().getContent();
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              Closeables.closeQuietly(inputStream);
            }
          }
        });
  }

  private boolean updateLockedStatus(InstitutionData inst) {
    try {
      PreparedStatement st =
          getConnection().prepareStatement("SELECT * FROM locks WHERE institution = ?");
      st.setString(1, inst.getShortName());
      ResultSet rs = st.executeQuery();
      inst.setLocked(false);
      inst.setUser(null);
      while (rs.next()) {
        inst.setLocked(true);
        inst.setUser(rs.getString(2));
        inst.setLockedDate(rs.getTimestamp(3));
      }
      rs.close();
      st.close();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          this, "An error occured. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
    }

    return inst.isLocked();
  }

  private void updateLockedStatus(List<InstitutionData> insts) {
    try {
      HashMap<String, InstitutionData> instMap = new HashMap<String, InstitutionData>();
      for (InstitutionData inst : insts) {
        instMap.put(inst.getShortName(), inst);
      }
      PreparedStatement st = getConnection().prepareStatement("SELECT * FROM locks");
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        String name = rs.getString("institution");
        if (instMap.containsKey(name)) {
          InstitutionData inst = instMap.get(name);
          inst.setLocked(true);
          inst.setUser(rs.getString("locked_by"));
          inst.setLockedDate(rs.getTimestamp("timestamp"));
        }
      }
      rs.close();
      st.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void doTask(SyncCallback callback, String title, String taskType, List<SyncData> toSync) {
    progressMonitor = new ProgressMonitor(this, title, "", 0, toSync.size());
    progressMonitor.setMillisToDecideToPopup(0);
    progressMonitor.setMillisToPopup(0);
    progressMonitor.setProgress(0);
    final InstTask task = new InstTask(toSync, taskType, callback);
    task.addPropertyChangeListener(
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            if ("progress" == evt.getPropertyName()) {
              Integer progress = (Integer) evt.getNewValue();
              progressMonitor.setProgress(progress);
              if (progressMonitor.isCanceled()) {
                task.cancel(true);
              }
            } else if ("state" == evt.getPropertyName()) {
              if ((StateValue) evt.getNewValue() == StateValue.DONE) {
                progressMonitor.close();
                if (task.getException() != null) {
                  task.getException().printStackTrace();
                  JOptionPane.showMessageDialog(
                      null,
                      "Error",
                      "Please check error log for stack trace",
                      JOptionPane.ERROR_MESSAGE);
                }
              }
            }
          }
        });
    task.execute();
  }

  private static class SyncData {
    private final String name;
    private final File institutionFile;

    public SyncData(String name, File institutionFile) {
      this.name = name;
      this.institutionFile = institutionFile;
    }

    public String getName() {
      return name;
    }

    public File getInstitutionFile() {
      return institutionFile;
    }
  }

  class InstTask extends SwingWorker<Object, SyncData> {
    private final List<SyncData> institutions;
    private final String taskType;
    private final SyncCallback callback;
    private Throwable exception;

    public InstTask(List<SyncData> institutions, String taskType, SyncCallback callback) {
      this.institutions = institutions;
      this.taskType = taskType;
      this.callback = callback;
    }

    public Throwable getException() {
      return exception;
    }

    @Override
    protected Object doInBackground() throws Exception {
      try {
        SyncToLocalServer syncer = new SyncToLocalServer();
        int i = 0;
        for (SyncData institutionData : institutions) {
          publish(institutionData);
          callback.callback(syncer, institutionData);
          setProgress(++i);
        }
      } catch (Throwable e) {
        this.exception = e;
      }
      return null;
    }

    @Override
    protected void process(List<SyncData> chunks) {
      progressMonitor.setNote(
          String.format("%s %s", taskType, chunks.get(chunks.size() - 1).getName()));
    }

    @Override
    protected void done() {
      // nothing
    }
  }
  ;

  interface SyncCallback {
    void callback(SyncToLocalServer syncer, SyncData data);
  }

  /**
   * Create the GUI and show it. For thread safety, this method should be invoked from the
   * event-dispatching thread.
   */
  private static void createAndShowGUI() {
    // Create and set up the window.
    JFrame frame = new JFrame("Sync Tool");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Create and set up the content pane.
    final Setup newContentPane = new Setup();
    newContentPane.setOpaque(true); // content panes must be opaque
    frame.setContentPane(newContentPane);

    // Display the window.
    frame.setSize(new Dimension(850, 480));
    frame.setVisible(true);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            newContentPane.close();
          }
        });
  }

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    javax.swing.SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            createAndShowGUI();
          }
        });
  }
}
