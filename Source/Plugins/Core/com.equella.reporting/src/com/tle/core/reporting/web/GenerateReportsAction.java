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

package com.tle.core.reporting.web;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.entity.report.Report;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.i18n.BundleCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.reporting.ReportingService;
import com.tle.core.reporting.birttypes.AbstractBirtType;
import com.tle.core.reporting.birttypes.BirtTypeUtils;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.equella.component.NavBarBuilder;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.sections.header.FormTag;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.FullScreen;
import com.tle.web.wizard.page.WebWizardPageState;
import com.tle.web.wizard.page.WizardPage;
import com.tle.web.wizard.page.WizardPageService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.birt.report.engine.api.IAction;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IParameterDefn;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class GenerateReportsAction extends AbstractPrototypeSection<ReportingForm>
    implements HtmlRenderer {
  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateReportsAction.class);

  private static final String PFX_PARAM = "p.";
  private static final String PFX_PARAM_TEXT = "pt.";

  public static final String XLS_SPUDSOFT = "xls_spudsoft";

  static {
    PluginResourceHandler.init(GenerateReportsAction.class);
  }

  @PlugKey("list.title")
  private static Label LABEL_TITLE;

  @PlugKey("list.confirm")
  private static String KEY_CONFIRM;

  @PlugKey("reports.viewer.title")
  private static Label LABEL_VIEWERTITLE;

  @PlugKey("list.report")
  private static Label LABEL_REPORT;

  @PlugKey("pleaseupload")
  private static String ALERT_UPLOAD;

  @PlugKey("reportformat")
  private static Label LABEL_FORMAT;

  @PlugURL("js/reporting.js")
  private static String URL_REPORTING_JS;

  private static final IncludeFile INCLUDE_FILE =
      new IncludeFile(URL_REPORTING_JS, JQueryCore.PRERENDER);

  private static final JSCallable POPUP_REPORT =
      new ExternallyDefinedFunction("popupReport", INCLUDE_FILE);

  private static final JSCallable PRINT_REPORT =
      new ExternallyDefinedFunction("printReport", INCLUDE_FILE);

  @Inject private BundleCache bundleCache;
  @Inject private WizardPageService wizardPageService;
  @Inject private ReportingService reportingService;
  @Inject private InstitutionService institutionService;

  @ViewFactory private FreemarkerFactory viewFactory;
  @EventFactory private EventGenerator events;
  @AjaxFactory private AjaxGenerator ajax;

  @Inject @Component private NavBar navBar;

  @Component
  @PlugKey("showparams")
  private Link paramsButton;

  @Component
  @PlugKey("reports.generatereports.execute")
  private Link submitButton;

  @Component
  @PlugKey(value = "reports.print", icon = Icon.PRINT)
  private Link printButton;

  @Component private Link titleLink;
  @Component private SingleSelectionList<Format> formatList;

  @Component
  @PlugKey(value = "forceregen", icon = Icon.REFRESH)
  private Link forceRegenButton;

  @Component(name = "r")
  private Table reportTable;

  private JSCallable showReportFunction;
  private JSCallable refreshParametersFunction;

  @Override
  public String getDefaultPropertyName() {
    return "";
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    paramsButton.setDisplayed(false);
    forceRegenButton.setDisplayed(false);
    printButton.setDisplayed(false);

    formatList.setAlwaysSelect(true);
    formatList.setEventHandler(
        JSHandler.EVENT_CHANGE,
        events.getNamedHandler("changeType", formatList.createGetExpression()));
    formatList.setListModel(
        new SimpleHtmlListModel<Format>(
            new Format("HTML", "html", true, false),
            new Format("PDF", "pdf"),
            new Format("Excel", XLS_SPUDSOFT),
            new Format("Word", "doc")));

    refreshParametersFunction =
        ajax.getAjaxUpdateDomFunction(
            tree,
            this,
            events.getEventHandler("refreshParameters"),
            ajax.getEffectFunction(EffectType.FADEOUTIN),
            "report-params");
    submitButton.setClickHandler(events.getNamedHandler("execute"));
    paramsButton.setClickHandler(events.getNamedHandler("showParams"));
    titleLink.setClickHandler(events.getNamedHandler("indexReport"));

    showReportFunction =
        ajax.getAjaxUpdateDomFunction(
            tree,
            this,
            events.getEventHandler("showReport"),
            ajax.getEffectFunction(EffectType.FADEOUTIN),
            "reportContent",
            "report-button-bar");
    forceRegenButton.setClickHandler(showReportFunction, true);
    printButton.setClickHandler(new OverrideHandler(PRINT_REPORT, "reportFrame"));

    reportTable.setColumnHeadings(LABEL_REPORT);
    reportTable.setColumnSorts(Sort.PRIMARY_ASC);

    ParametersHandler paramHandler = new ParametersHandler();
    tree.addListener(null, BookmarkEventListener.class, paramHandler);
    tree.addListener(null, ParametersEventListener.class, paramHandler);

    navBar.setTitle(titleLink);
    NavBarBuilder nbb = navBar.buildRight();
    nbb.divider();
    nbb.action(paramsButton);
    nbb.action(forceRegenButton);
    nbb.action(printButton);
    nbb.divider().dropDown(LABEL_FORMAT, formatList, false);
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) throws MalformedURLException {
    ReportingForm model = getModel(context);

    String reportUuid = model.getReportUuid();
    Decorations decs = Decorations.getDecorations(context);
    if (reportUuid == null) {
      decs.setTitle(LABEL_TITLE);

      List<Report> reports = new ArrayList<Report>(enumerateReports());
      model.setReports(reports.size() > 0);

      TableState reportTableState = reportTable.getState(context);
      for (Report report : reports) {
        BookmarkAndModify runUrl =
            new BookmarkAndModify(context, events.getNamedModifier("runReport", report.getUuid()));
        OverrideHandler handler = new OverrideHandler(POPUP_REPORT, runUrl.getHref());
        handler.addValidator(
            new Confirm(new KeyLabel(KEY_CONFIRM, new BundleLabel(report.getName(), bundleCache))));

        HtmlLinkState name = new HtmlLinkState();
        name.setLabel(new BundleLabel(report.getName(), bundleCache));
        name.setClickHandler(handler);

        LabelRenderer description =
            new LabelRenderer(new BundleLabel(report.getDescription(), bundleCache));

        reportTableState.addRow(
            viewFactory.createResultWithModelMap(
                "reporting/reportListReport.ftl", "name", name, "description", description));
      }

      return viewFactory.createResult("reporting/reportList.ftl", context);
    }

    decs.setFullscreen(FullScreen.YES_WITH_TOOLBAR);
    decs.clearAllDecorations();
    decs.setTitle(LABEL_VIEWERTITLE);

    FormTag form = context.getForm();
    form.setName("WizardForm");

    titleLink.setLabel(context, new BundleLabel(getReport(context).getName(), bundleCache));
    if (model.isShowWizard()) {
      WizardPage wizPage = getWizardPage(context);
      wizPage.ensureTreeAdded(context);
      model.setWizard(wizPage.renderPage(context));
    } else if (model.isShowReport()) {
      final Report report = getReport(context);
      final Format format = formatList.getSelectedValue(context);
      printButton.setDisplayed(context, format.isPrintable());

      if (model.isContainsParameters()) {
        boolean visibleParams = containsVisibleParams(context);
        if (visibleParams) {
          paramsButton.setDisplayed(context, true);
        }
      }
      forceRegenButton.setDisplayed(context, true);

      StringBuilder url = new StringBuilder();
      url.append("cache/");
      url.append(report.getUuid());
      url.append('/');
      url.append(model.getGeneratedReportId());
      if (format.isDownloadOnly()) {
        url.append("?disposition=attachment");
      }

      model.setReportUrl(new URL(institutionService.getInstitutionUrl(), url.toString()).getFile());
    } else {
      JQueryCore.appendReady(context, new FunctionCallStatement(showReportFunction, false));
    }

    return viewFactory.createResult("reporting/report.ftl", context);
  }

  @EventHandlerMethod
  public void indexReport(SectionInfo info) throws Exception {
    ReportingForm model = getModel(info);
    model.setDesignFile(null);
    model.setForceParams(containsVisibleParams(info));
  }

  @EventHandlerMethod
  public void showReport(SectionInfo info, boolean forceExecution) throws Exception {
    final String logPrefix =
        "showReport(info=" + info + ", forceExecution=" + forceExecution + ") - ";
    LOGGER.debug(logPrefix + "START");

    ReportingForm model = getModel(info);
    model.setShowReport(true);

    Report report = getReport(info);
    String generatedReportId =
        reportingService.executeReport(
            info,
            report,
            model.getDesignFile(),
            formatList.getSelectedValueAsString(info),
            new LinkHandler(report),
            model.getParameters(),
            model.getParameterDisplayTexts(),
            forceExecution);
    LOGGER.debug(logPrefix + "generatedReportId=" + generatedReportId);
    model.setGeneratedReportId(generatedReportId);
  }

  /**
   * Bit of a misnomer. It doesn't actually run the report, it just brings up the report frameset
   *
   * @param info
   * @param reportUuid
   * @throws Exception
   */
  @EventHandlerMethod
  public void runReport(SectionInfo info, String reportUuid) throws Exception {
    ReportingForm model = getModel(info);
    model.setReportUuid(reportUuid);
    boolean showParams = containsVisibleParams(info);
    model.setParametersValid(!showParams);
    model.setForceParams(showParams);
  }

  @EventHandlerMethod
  public void changeType(SectionInfo info, String type) {
    formatList.setSelectedStringValue(info, type);
  }

  @EventHandlerMethod
  public void showParams(SectionInfo info) {
    getModel(info).setForceParams(true);
  }

  private WizardPage getWizardPage(SectionInfo info) {
    return getModel(info)
        .getWizardPage()
        .get(
            info,
            new CacheFiller<WizardPage>() {
              @Override
              public WizardPage get(SectionInfo info) {
                try {
                  DefaultWizardPage page = new DefaultWizardPage();
                  int paramNum = 1;
                  List<WizardControl> controls = page.getControls();
                  IGetParameterDefinitionTask paramTask = getParametersTask(info);

                  @SuppressWarnings("unchecked")
                  Collection<IParameterDefnBase> parameterDefns = paramTask.getParameterDefns(true);

                  ReportingForm model = getModel(info);
                  Map<String, String[]> params = model.getParameters();
                  PropBagEx docXml = new PropBagEx();
                  List<AbstractBirtType> paramControls = new ArrayList<AbstractBirtType>();
                  boolean hasGroups = false;

                  for (IParameterDefnBase parameterDefn : parameterDefns) {
                    if (parameterDefn.getParameterType()
                        == IParameterDefnBase.CASCADING_PARAMETER_GROUP) {
                      IParameterGroupDefn group = (IParameterGroupDefn) parameterDefn;
                      hasGroups = true;

                      for (Object o : group.getContents()) {
                        IParameterDefnBase param = (IParameterDefnBase) o;
                        addParameter(
                            param,
                            paramControls,
                            controls,
                            paramNum,
                            docXml,
                            params,
                            paramTask,
                            group);
                        paramNum++;
                      }
                    } else {
                      addParameter(
                          parameterDefn,
                          paramControls,
                          controls,
                          paramNum,
                          docXml,
                          params,
                          paramTask,
                          null);
                      paramNum++;
                    }
                  }
                  paramTask.close();
                  model.setParamControls(paramControls);
                  model.setHasGroups(hasGroups);
                  WizardPage webPage =
                      wizardPageService.createSimplePage(
                          page, docXml, new WebWizardPageState(), false);
                  webPage.setReloadFunction(refreshParametersFunction);
                  webPage.setPageNumber(1);
                  webPage.init();
                  webPage.createPage();
                  return webPage;
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              }
            });
  }

  private void addParameter(
      IParameterDefnBase param,
      List<AbstractBirtType> paramControls,
      List<WizardControl> controls,
      int paramNum,
      PropBagEx docXml,
      Map<String, String[]> params,
      IGetParameterDefinitionTask paramTask,
      IParameterGroupDefn group) {
    if (param.getParameterType() == IParameterDefnBase.SCALAR_PARAMETER) {
      IScalarParameterDefn scalarDef = (IScalarParameterDefn) param;

      if (!scalarDef.isHidden()) {
        AbstractBirtType birtWrapper = BirtTypeUtils.createWrapper(scalarDef, paramNum, group);
        paramControls.add(birtWrapper);

        controls.add(birtWrapper.createWizardControl(paramTask));
        if (params != null) {
          birtWrapper.convertToXml(docXml, params);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private boolean containsVisibleParams(SectionInfo info) {
    IGetParameterDefinitionTask paramTask = getParametersTask(info);
    Collection<IParameterDefn> params = paramTask.getParameterDefns(false);
    paramTask.close();
    for (IParameterDefn parameterDefn : params) {
      if (parameterDefn.getParameterType() == IParameterDefnBase.SCALAR_PARAMETER) {
        IScalarParameterDefn scalarDef = (IScalarParameterDefn) parameterDefn;
        if (!scalarDef.isHidden()) {
          return true;
        }
      } else {
        throw new RuntimeException("Unsupported parameter type:" + parameterDefn.getTypeName());
      }
    }
    return false;
  }

  @EventHandlerMethod
  public void refreshParameters(SectionInfo info) {
    ReportingForm model = getModel(info);

    if (!model.isHasGroups()) {
      return;
    }
    WizardPage webPage = getWizardPage(info);
    PropBagEx docXml = webPage.getDocBag();

    Map<String, String[]> params = model.getParameters();
    List<AbstractBirtType> paramControls = model.getParamControls();
    for (AbstractBirtType control : paramControls) {
      control.update(params, paramControls, getParametersTask(info));
      control.convertToParams(docXml, params);
      try {
        webPage.ensureTreeAdded(info);
        webPage.loadFromDocument(info);
        webPage.saveToDocument(info);
        for (AbstractBirtType birtWrapper : paramControls) {
          birtWrapper.convertToParams(docXml, params);
        }
      } catch (Exception e) {
        // Deep troubs
      }
      // Seems odd but is necessary
    }
    model.setParametersValid(webPage.isValid());
  }

  @EventHandlerMethod
  public void execute(SectionInfo info) {
    ReportingForm model = getModel(info);
    if (model.isHasGroups()) {
      refreshParameters(info);
    }
    WizardPage wizardPage = getWizardPage(info);
    if (wizardPage.isValid()) {
      model.setForceParams(false);
    }
  }

  public static class ReportDesignHandle {
    private final Report report;
    private final String filename;

    public ReportDesignHandle(Report report, String filename) {
      this.report = report;
      this.filename = filename;
    }

    public Report getReport() {
      return report;
    }

    public String getFilename() {
      return filename;
    }
  }

  public class LinkHandler implements IHTMLActionHandler {
    private final Map<String, ReportDesignHandle> reportNameMap =
        new HashMap<String, ReportDesignHandle>();
    private final Map<String, Collection<IParameterDefn>> paramMap =
        new HashMap<String, Collection<IParameterDefn>>();
    private final Report currentReport;

    public LinkHandler(Report report) {
      this.currentReport = report;
    }

    @Override
    public String getURL(IAction arg0, Object arg1) {
      return null;
    }

    @Override
    public String getURL(IAction action, IReportContext context) {
      if (action == null) {
        return null;
      }
      String url = null;
      switch (action.getType()) {
        case IAction.ACTION_BOOKMARK:
          if (action.getActionString() != null) {
            url = "#" + action.getActionString();
          }
          break;
        case IAction.ACTION_HYPERLINK:
          url = action.getActionString();
          break;
        case IAction.ACTION_DRILLTHROUGH:
          url = buildDrillAction(action);
          break;
        default:
          assert false;
      }
      return url;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String buildDrillAction(IAction action) {
      ReportDesignHandle reportHandle = null;
      String reportName = action.getReportName();
      if (!reportNameMap.containsKey(reportName)) {
        String designFile = reportingService.findDesignFile(currentReport, reportName);
        if (designFile != null) {
          reportHandle = new ReportDesignHandle(currentReport, designFile);
        } else {
          Report report = reportingService.getReportForFilename(reportName);
          if (report != null) {
            reportHandle = new ReportDesignHandle(report, null);
          }
        }
        reportNameMap.put(reportName, reportHandle);
        if (reportHandle != null) {
          IGetParameterDefinitionTask paramTask =
              reportingService.createReportParametersTask(
                  reportHandle.getReport(), reportHandle.getFilename());
          paramMap.put(reportName, paramTask.getParameterDefns(false));
          paramTask.close();
        }
      } else {
        reportHandle = reportNameMap.get(reportName);
      }
      if (reportHandle == null) {
        String alertMsg = CurrentLocale.get(ALERT_UPLOAD, reportName);
        return "javascript:alert('" + Utils.jsescape(alertMsg) + "');";
      }
      Map params = action.getParameterBindings();
      Collection<IParameterDefn> parameterDefns = paramMap.get(reportName);
      HashMap<String, String> urlParams = new LinkedHashMap<String, String>();
      urlParams.put("reportUuid", reportHandle.getReport().getUuid());
      urlParams.put("df", reportHandle.getFilename());
      urlParams.put("format", action.getFormat());
      for (IParameterDefn def : parameterDefns) {
        String paramName = def.getName();
        Object reportParam = params.get(paramName);
        if (reportParam != null) {
          if (reportParam instanceof Collection) {
            reportParam = ((Collection<?>) reportParam).iterator().next();
          }
          String strValue =
              ParameterValidationUtil.getDisplayValue(
                  def.getTypeName(),
                  getParameterFormat((IScalarParameterDefn) def),
                  reportParam,
                  CurrentLocale.getLocale());
          urlParams.put(PFX_PARAM + paramName, strValue);
        }
      }
      return "../../access/reports.do?" + URLUtils.getParameterString(urlParams);
    }
  }

  protected String getParameterFormat(IScalarParameterDefn scalarDef) {
    switch (scalarDef.getDataType()) {
      case IParameterDefn.TYPE_DATE_TIME:
        return ParameterValidationUtil.DEFAULT_DATETIME_FORMAT;
      case IParameterDefn.TYPE_DATE:
        return ParameterValidationUtil.DEFAULT_DATE_FORMAT;
      case IParameterDefn.TYPE_TIME:
        return ParameterValidationUtil.DEFAULT_TIME_FORMAT;
      default:
        return null;
    }
  }

  public class ParametersHandler implements BookmarkEventListener, ParametersEventListener {

    public static final String WIZARD_SUBMITTED = "wizardSubmitted";

    @Override
    public void bookmark(SectionInfo info, BookmarkEvent event) {
      ReportingForm model = getModel(info);
      if (event.isRendering() && model.isShowWizard()) {
        event.setParam(WIZARD_SUBMITTED, "true");
      }
      Map<String, String[]> params = model.getParameters();
      if (params != null) {
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
          event.setParams(PFX_PARAM + entry.getKey(), Arrays.asList(entry.getValue()));
        }
      }

      Map<String, String[]> paramTexts = model.getParameterDisplayTexts();
      if (paramTexts != null) {
        for (Map.Entry<String, String[]> entry : paramTexts.entrySet()) {
          if (entry.getValue() == null) {
            continue;
          }
          event.setParams(PFX_PARAM_TEXT + entry.getKey(), Arrays.asList(entry.getValue()));
        }
      }
    }

    @Override
    public void handleParameters(SectionInfo info, ParametersEvent event) {
      ReportingForm model = getModel(info);
      if (model.getReportUuid() == null) {
        return;
      }

      WizardPage wizardPage = getWizardPage(info);
      if (event.getBooleanParameter(WIZARD_SUBMITTED, false)) {
        wizardPage.ensureTreeAdded(info, true);
        try {
          wizardPage.saveToDocument(info);
        } catch (Exception e) {
          e.printStackTrace();
        }
        wizardPage.setSubmitted(true);
        wizardPage.setShowMandatory(true);
        Map<String, String[]> params = new HashMap<String, String[]>();
        Map<String, String[]> paramDisplayNames = new HashMap<String, String[]>();
        PropBagEx docXml = wizardPage.getDocBag();
        List<AbstractBirtType> controls = model.getParamControls();
        for (AbstractBirtType birtWrapper : controls) {
          birtWrapper.convertToParams(docXml, params);
          String[] displayTexts = birtWrapper.getDisplayTexts(docXml);
          if (displayTexts != null) {
            paramDisplayNames.put(birtWrapper.getName(), displayTexts);
          }
        }
        model.setParameters(params);
        model.setParameterDisplayTexts(paramDisplayNames);
        model.setParametersValid(wizardPage.isValid());
      } else {
        wizardPage.ensureTreeAdded(info, false);

        Map<String, String[]> params = new HashMap<String, String[]>();
        Map<String, String[]> parameterDisplayTexts = new HashMap<String, String[]>();
        boolean allSupplied = true;
        for (AbstractBirtType parameterDefn : model.getParamControls()) {
          IScalarParameterDefn scalarDef = parameterDefn.getDefinition();
          String paramName = scalarDef.getName();
          String[] values = event.getParameterValues(paramName);
          if (values == null) {
            values = event.getParameterValues(PFX_PARAM + paramName);
          }
          if (values == null) {
            allSupplied &= !scalarDef.isRequired() || scalarDef.getDefaultValue() != null;
          } else {
            params.put(paramName, values);
          }
          model.setContainsParameters(true);

          if (event.getParameterValues(PFX_PARAM_TEXT + paramName) != null) {
            parameterDisplayTexts.put(
                paramName, event.getParameterValues(PFX_PARAM_TEXT + paramName));
          }
        }
        PropBagEx docXml = wizardPage.getDocBag();
        for (AbstractBirtType control : model.getParamControls()) {
          control.convertToXml(docXml, params);
        }
        wizardPage.loadFromDocument(info);
        model.setParameterDisplayTexts(parameterDisplayTexts);
        model.setParameters(params);
        model.setParametersValid(allSupplied);
      }
    }

    @Override
    public void document(SectionInfo info, DocumentParamsEvent event) {
      // nothing
    }
  }

  private Collection<Report> enumerateReports() {
    List<Report> reports = new ArrayList<Report>();
    for (Report report : reportingService.enumerateExecutable()) {
      if (!report.isHideReport()) {
        bundleCache.addBundle(report.getName());
        bundleCache.addBundle(report.getDescription());
        reports.add(report);
      }
    }
    return reports;
  }

  public Report getReport(SectionInfo info) {
    final ReportingForm model = getModel(info);
    if (model.getReportUuid() == null) {
      throw new RuntimeException("No report specified");
    }
    return model
        .getReportCached()
        .get(
            info,
            new CacheFiller<Report>() {
              @Override
              public Report get(SectionInfo info) {
                return reportingService.getByUuid(model.getReportUuid());
              }
            });
  }

  public IGetParameterDefinitionTask getParametersTask(SectionInfo info) {
    return reportingService.createReportParametersTask(
        getReport(info), getModel(info).getDesignFile());
  }

  @Override
  public Class<ReportingForm> getModelClass() {
    return ReportingForm.class;
  }

  public Table getReportTable() {
    return reportTable;
  }

  public NavBar getNavBar() {
    return navBar;
  }

  public Link getSubmitButton() {
    return submitButton;
  }

  public static class Format extends NameValue {
    private static final long serialVersionUID = 1L;

    private final boolean printable;
    private final boolean downloadOnly;

    public Format(String name, String value) {
      this(name, value, false, true);
    }

    public Format(String name, String value, boolean printable, boolean downloadOnly) {
      super(name, value);
      this.printable = printable;
      this.downloadOnly = downloadOnly;
    }

    public boolean isPrintable() {
      return printable;
    }

    public boolean isDownloadOnly() {
      return downloadOnly;
    }
  }
}
