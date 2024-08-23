/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.reporting;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.ImmutableMap;
import com.tle.beans.entity.report.Report;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.reporting.RemoteReportingService;
import com.tle.common.reporting.ReportingException;
import com.tle.common.reporting.ReportingException.Type;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.filesystem.CachedFile;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.reporting.dao.ReportingDao;
import com.tle.core.reporting.web.GenerateReportsAction;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.ValidationHelper;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;
import org.java.plugin.util.IoUtil;
import org.mozilla.javascript.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
@SecureEntity(RemoteReportingService.ENTITY_TYPE)
@Bind(ReportingService.class)
@Singleton
public class ReportingServiceImpl
    extends AbstractEntityServiceImpl<EntityEditingBean, Report, ReportingService>
    implements ReportingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReportingService.class);

  private static final String[] BLANKS = {"name"};

  private static final long CACHE_TIME = TimeUnit.MINUTES.toMillis(30);

  private final ReportingDao reportingDao;
  private IReportEngine reportEngine;

  @Inject private LearningEdgeOdaDelegate odaInterface;
  @Inject private PluginService pluginService;
  @Inject private ConfigurationService configService;

  @Inject
  public ReportingServiceImpl(ReportingDao dao) {
    super(Node.REPORT, dao);

    this.reportingDao = dao;
  }

  @SecureOnReturn(priv = ReportPrivileges.EXECUTE_REPORT)
  @Override
  public List<Report> enumerateExecutable() {
    return getDao().enumerateAll();
  }

  @Override
  protected void doValidation(
      EntityEditingSession<EntityEditingBean, Report> session,
      Report entity,
      List<ValidationError> errors) {
    ValidationHelper.checkBlankFields(entity, BLANKS, errors);
  }

  @SuppressWarnings("unchecked")
  private synchronized IReportEngine getReportEngine() {
    try {
      Level logLevel = LOGGER.isDebugEnabled() ? Level.FINE : Level.SEVERE;
      if (reportEngine == null) {
        LOGGER.info("Loading Birt engine");
        EngineConfig engineConfig = new EngineConfig();

        if (configService.isDebuggingMode()) {
          engineConfig.setOSGiConfig(ImmutableMap.of("osgi.dev", "true"));
        }
        URL resource = getClass().getClassLoader().getResource("ReportEngine/");
        String birtHome = IoUtil.url2file(resource).getAbsolutePath();
        engineConfig.setBIRTHome(birtHome);

        HTMLRenderOption emitterConfig = new HTMLRenderOption();
        HTMLServerImageHandler imageHandler = new HTMLServerImageHandler();
        emitterConfig.setImageHandler(imageHandler);
        engineConfig.getEmitterConfigs().put("html", emitterConfig);

        // get log info from LOG4J
        java.util.logging.Logger birtLogger =
            java.util.logging.Logger.getLogger("org.eclipse.birt");
        if (birtLogger != null) {
          birtLogger.addHandler(new Log4JHandler());
        }

        // It was either use these extended classes or modify birt.
        Platform.startup(engineConfig);
        IReportEngineFactory factory =
            (IReportEngineFactory)
                Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
        if (factory == null) {
          throw new BirtException("Could not get report engine factory");
        }
        reportEngine = factory.createReportEngine(engineConfig);
        reportEngine.changeLogLevel(logLevel);
      }
      return reportEngine;

    } catch (BirtException e) {
      Platform.shutdown();
      LOGGER.error("Error with Birt", e);
      throw new RuntimeApplicationException("Error loading reporting engine", e);
    }
  }

  public static class Log4JHandler extends Handler {
    @Override
    public void close() throws SecurityException {
      // Nothing to do here
    }

    @Override
    public void flush() {
      // Nothing to do here
    }

    @Override
    public void publish(LogRecord record) {
      String loggerName = record.getLoggerName();
      Logger logger = loggerName != null ? LoggerFactory.getLogger(loggerName) : LOGGER;
      Level level = record.getLevel();
      String message = record.getMessage();
      try {
        message = MessageFormat.format(record.getMessage(), record.getParameters());
      } catch (IllegalArgumentException ignore) {
        // message will possibly have placeholder format for its
        // parameters, such as
        // "frog went {0} on a {1} day", "walking", "summer's",
        // If format fails, send message in the raw.
      }
      if (record.getThrown() != null && !Check.isEmpty(record.getThrown().getLocalizedMessage())) {
        message += '\n' + record.getThrown().getLocalizedMessage();
      }

      if (Level.SEVERE.equals(level)) {
        logger.error(message);
      } else if (Level.INFO.equals(level)) {
        logger.info(message);
      } else if (Level.WARNING.equals(level)) {
        logger.warn(message);
      } else if (Level.FINE.equals(level)
          || Level.FINER.equals(level)
          || Level.FINEST.equals(level)) {
        logger.trace(message);
      }
    }
  }

  @Override
  @SecureOnCall(priv = ReportPrivileges.EXECUTE_REPORT)
  public String executeReport(
      SectionInfo info,
      Report report,
      String designFile,
      String format,
      IHTMLActionHandler actionHandler,
      Map<String, String[]> parameters,
      Map<String, String[]> parameterDisplayTexts,
      boolean forceExecution) {
    String extension = ".html";
    if (format.equals(IRenderOption.OUTPUT_FORMAT_PDF)) {
      extension = ".pdf";
    } else if (format.equals(GenerateReportsAction.XLS_SPUDSOFT)) {
      extension = ".xls";
    } else if (format.equals("doc")) {
      extension = ".doc";
    }

    // some hash of the parameters
    final String hash =
        report.getUuid()
            + SectionUtils.getParameterString(
                SectionUtils.getParameterNameValues(parameters, false))
            + CurrentUser.getUserID();
    String outName = designFile != null ? designFile.replace('/', '_') : "report";
    final String outfile = outName + Math.abs(hash.hashCode() % 10000) + extension;
    String reportSignature = report.getReportSignature() + ", format=[" + format + "]";
    try {
      final CachedFile entityFile = new CachedFile(report.getUuid());

      if (forceExecution && fileSystemService.fileExists(entityFile, outfile)) {
        // sometimes it can't be deleted because it's still serving the
        // last one.
        // would be cool to do some sort of file-lock capability in
        // fileSystemService
        fileSystemService.removeFile(entityFile, outfile);
      }

      // if this file exists, serve it
      if (!fileSystemService.isFileCached(entityFile, outfile, CACHE_TIME)) {
        final IReportEngine engine = getReportEngine();

        LOGGER.info("Started Report - " + reportSignature);
        final IReportRunnable design = getReportDesign(engine, report, designFile);
        final IRunAndRenderTask task = engine.createRunAndRenderTask(design);
        RenderOption options = new RenderOption();

        HTMLRenderOption html = new HTMLRenderOption(options);
        html.setImageDirectory(
            getFileSystemService().getExternalFile(entityFile, "image").getAbsolutePath());
        html.setBaseImageURL("image");

        new PDFRenderOption(options);
        new EXCELRenderOption(options);

        HashMap<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put(com.tle.reporting.Constants.DELEGATE_APP_CONTEXT_KEY, odaInterface);
        task.setAppContext(contextMap);
        task.setParameterValues(prepareReportParameters(engine, design, parameters));

        for (String key : parameters.keySet()) {
          String[] texts = parameterDisplayTexts.get(key);
          if (texts == null) {
            Object value = task.getParameterValue(key);
            // This could be anything, or an array of multiple types
            // of anything
            if (value instanceof Object[]) {
              Object[] arrayOfObjects = (Object[]) value;
              String[] castTexts = new String[arrayOfObjects.length];

              for (int i = 0; i < arrayOfObjects.length; i++) {
                castTexts[i] = arrayOfObjects[i].toString();
              }
              task.setParameterDisplayText(key, castTexts);
            } else {
              task.setParameterDisplayText(key, value.toString());
            }
            continue;
          }

          if (texts.length == 1) {
            task.setParameterDisplayText(key, texts[0]);
            // BIRT is fussy about this
          } else {
            task.setParameterDisplayText(key, texts);
          }
        }

        try (OutputStream out =
            getFileSystemService().getOutputStream(entityFile, outfile, false)) {
          options.setActionHandler(actionHandler != null ? actionHandler : new HTMLActionHandler());
          options.setOutputStream(out);
          options.setOutputFormat(format);

          task.setRenderOption(options);
          task.setLocale(CurrentLocale.getLocale());
          task.run();
          task.close();

          LOGGER.info("Finished report - " + reportSignature);
        }
      }
      return outfile;
    } catch (BirtException be) {
      LOGGER.warn("Failed running report - " + reportSignature);
      throw new RuntimeException(be);
    } catch (IOException ioe) {
      LOGGER.warn("Failed running report - " + reportSignature);
      throw new RuntimeException(ioe);
    } catch (Exception e) {
      LOGGER.warn("Failed running report - " + reportSignature);
      throw new RuntimeException(
          "executeReport threw up (not BirtException nor IOException) ...", e.getCause());
    } finally {
      // Birt is not exiting the context...
      while (Context.getCurrentContext() != null) {
        Context.exit();
      }
    }
  }

  private IReportRunnable getReportDesign(IReportEngine engine, Report report, String designFile)
      throws IOException, EngineException {
    final String design = (designFile == null ? report.getFilename() : designFile);
    final EntityFile file = new EntityFile(report);
    final InputStream inStream = getFileSystemService().read(file, design);
    final int lastSlash = design.indexOf('/');
    final String folder = (lastSlash > -1 ? design.substring(0, lastSlash + 1) : "");

    return engine.openReportDesign(
        null,
        inStream,
        new IResourceLocator() {
          @Override
          public URL findResource(
              ModuleHandle module,
              String filename,
              int arg2,
              @SuppressWarnings("rawtypes") Map arg3) {
            return findResource(module, filename, arg2);
          }

          @Override
          public URL findResource(ModuleHandle module, String filename, int arg2) {
            try {
              FileSystemService fsys = getFileSystemService();
              String fullFile = folder + filename;
              if (fsys.fileExists(file, fullFile)) {
                return fsys.getExternalFile(file, fullFile).toURI().toURL();
              }
              Report otherReport = getReportForFilename(filename);
              if (otherReport != null) {
                return fsys.getExternalFile(new EntityFile(otherReport), otherReport.getFilename())
                    .toURI()
                    .toURL();
              }
              return null;
            } catch (MalformedURLException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }

  private Map<String, ?> prepareReportParameters(
      IReportEngine engine, IReportRunnable design, Map<String, String[]> parameters)
      throws BirtException {
    Map<String, Object> results = new HashMap<String, Object>();

    IGetParameterDefinitionTask task = createReportParametersTask(engine, design);

    @SuppressWarnings("unchecked")
    Collection<IParameterDefnBase> parameterDefns = task.getParameterDefns(false);

    for (IParameterDefnBase param : parameterDefns) {
      if (param.getParameterType() == IParameterDefnBase.SCALAR_PARAMETER) {
        String paramName = param.getName();
        String[] currentValue = parameters.get(paramName);
        if (currentValue != null) {
          Object[] converted = new Object[currentValue.length];
          String dataType = ((ScalarParameterHandle) param.getHandle()).getDataType();
          IScalarParameterDefn scalar = (IScalarParameterDefn) param;
          for (int i = 0; i < currentValue.length; i++) {
            converted[i] =
                ParameterValidationUtil.validate(
                    dataType,
                    getDefaultDateFormat(dataType),
                    currentValue[i],
                    CurrentLocale.getLocale());
          }
          if (scalar.getScalarParameterType().equals("multi-value")) {
            results.put(paramName, converted);
          } else {
            results.put(paramName, converted[0]);
          }
        }
      }
    }
    task.close();
    return results;
  }

  private static String getDefaultDateFormat(String dataType) {
    String defFormat = null;
    if (DesignChoiceConstants.PARAM_TYPE_DATETIME.equalsIgnoreCase(dataType)) {
      defFormat = ParameterValidationUtil.DEFAULT_DATETIME_FORMAT;
    } else if (DesignChoiceConstants.PARAM_TYPE_DATE.equalsIgnoreCase(dataType)) {
      defFormat = ParameterValidationUtil.DEFAULT_DATE_FORMAT;
    } else if (DesignChoiceConstants.PARAM_TYPE_TIME.equalsIgnoreCase(dataType)) {
      defFormat = ParameterValidationUtil.DEFAULT_TIME_FORMAT;
    }

    return defFormat;
  }

  @Override
  public Report getReportForFilename(String filename) {
    return reportingDao.findByReportFilename(filename);
  }

  @Override
  public IGetParameterDefinitionTask createReportParametersTask(Report report, String designFile) {
    IReportEngine engine = getReportEngine();
    try {
      return createReportParametersTask(engine, getReportDesign(engine, report, designFile));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private IGetParameterDefinitionTask createReportParametersTask(
      IReportEngine engine, IReportRunnable design) {
    try {
      IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(design);
      HashMap<String, Object> contextMap = new HashMap<String, Object>();
      contextMap.put(com.tle.reporting.Constants.DELEGATE_APP_CONTEXT_KEY, odaInterface);
      task.setAppContext(contextMap);
      return task;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> getReportDesignFiles(String stagingId) {
    return fileSystemService.grep(new StagingFile(stagingId), "", "**/*.rpt*");
  }

  @Override
  public void processReportDesign(String stagingId, String filename) throws IOException {
    StagingFile stagingFile = new StagingFile(stagingId);
    String lowerName = filename.toLowerCase();
    if (lowerName.endsWith(".rptdesign") || lowerName.endsWith(".rptlibrary")) {
      String nameOnly = new File(filename).getName();
      fileSystemService.mkdir(stagingFile, DIR_DESIGN);
      fileSystemService.rename(stagingFile, filename, DIR_DESIGN + '/' + nameOnly);
    } else {
      fileSystemService.unzipFile(stagingFile, filename, DIR_DESIGN);
      fileSystemService.removeFile(stagingFile, filename);
      List<String> reports = fileSystemService.grep(new StagingFile(stagingId), "", "**/*.rpt*");
      if (reports.isEmpty()) {
        fileSystemService.removeFile(stagingFile, DIR_DESIGN);
        throw new ReportingException(
            "Archive contains no report design files (.rptdesign)", Type.NODESIGNS);
      }
      for (String reportFilename : reports) {
        cleanReport(stagingFile, reportFilename);
      }
    }
  }

  private void cleanReport(StagingFile stagingFile, String reportFilename) {
    try (InputStream reportStream = fileSystemService.read(stagingFile, reportFilename)) {
      PropBagEx xml = new PropBagEx(reportStream);

      boolean cleaned = false;
      for (PropBagEx dataSource : xml.iterator("data-sources/oda-data-source")) {
        if (dataSource.getNode("@extensionID").equals("com.tle.reporting.oda.datasource")) {
          for (PropBagEx property : dataSource.iterator("property")) {
            if (property.getNode("@name").equals("webserviceUser")) {
              property.setNode("/", "");
              cleaned = true;
            }
          }

          for (PropBagEx property : dataSource.iterator("encrypted-property")) {
            if (property.getNode("@name").equals("webservicePassword")) {
              property.setNode("/", "");
              cleaned = true;
            }
          }
        }
      }
      if (cleaned) {
        fileSystemService.write(
            stagingFile, reportFilename, new StringReader(xml.toString()), false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String findDesignFile(Report report, String filename) {
    EntityFile handle = new EntityFile(report);
    List<String> designFiles = fileSystemService.grep(handle, "", "**/" + filename);
    if (designFiles.isEmpty()) {
      return null;
    }
    return designFiles.get(0);
  }

  @Override
  public String prepareDownload(Report report, String stagingId, String filename) {
    StagingFile stagingFile = new StagingFile(stagingId);
    StagingFile outStaging = stagingService.createStagingArea();
    String dirToZip = DIR_DESIGN;
    if (!fileSystemService.fileExists(stagingFile, DIR_DESIGN)) {
      dirToZip = "";
    }
    try {
      fileSystemService.zipFile(stagingFile, dirToZip, outStaging, filename, ArchiveType.ZIP);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return outStaging.getUuid();
  }

  @Override
  public void cleanDownload(String stagingId) {
    stagingService.removeStagingArea(new StagingFile(stagingId), true);
  }
}
