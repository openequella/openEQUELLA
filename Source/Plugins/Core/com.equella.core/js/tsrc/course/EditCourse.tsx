import {
  Button,
  FormControl,
  FormControlLabel,
  FormGroup,
  FormHelperText,
  Grid,
  Input,
  InputLabel,
  MenuItem,
  Paper,
  Switch,
  Tab,
  Tabs,
  TextField,
  Theme
} from "@material-ui/core";
import Select from "@material-ui/core/Select";
import { StyleRules, WithStyles, withStyles } from "@material-ui/core/styles";
import { LocationDescriptor } from "history";
import { DateTime } from "luxon";
import { DatePicker } from "material-ui-pickers";
import * as React from "react";
import { connect, Dispatch } from "react-redux";
import { Link } from "react-router-dom";
import { sprintf } from "sprintf-js";
import courseService from ".";
import aclService from "../acl/index";
import { Course } from "../api";
import { AclEditorChangeEvent, TargetListEntry } from "../api/acleditor";
import { Bridge } from "../api/bridge";
import { Error, Loader } from "../components/index";
import MessageInfo from "../components/MessageInfo";
import {
  EditEntityDispatchProps,
  EditEntityProps,
  EditEntityStateProps,
  entityStrings
} from "../entity";
import { routes } from "../mainui/routes";
import { templateDefaults, TemplateProps } from "../mainui/Template";
import schemaService from "../schema/index";
import { StoreState } from "../store";
import { commonString } from "../util/commonstrings";
import { properties } from "../util/dictionary";
import { languageStrings } from "../util/langstrings";

declare const bridge: Bridge;

const styles = (theme: Theme) => {
  //TODO: get drawerWidth passed in somehow
  const footerHeight = 48;
  return {
    form: {
      display: "flex",
      flexFlow: "row wrap"
    },
    formControl: {
      margin: theme.spacing(1),
      flex: "1 1 40%",
      marginBottom: theme.spacing(2)
    },
    formControl2: {
      margin: theme.spacing(1),
      flex: "2 1 100%",
      marginBottom: theme.spacing(2)
    },
    body: {
      padding: `${theme.spacing(2)}px`,
      paddingBottom: footerHeight,
      height: "100%"
    },
    footer: {
      minHeight: footerHeight
    },
    footerActions: {
      padding: "4px",
      paddingRight: "20px",
      display: "flex",
      justifyContent: "flex-end"
    },
    hiddenTab: {
      display: "none"
    }
  } as StyleRules;
};

interface EditCourseStateProps extends EditEntityStateProps<Course> {
  citations?: string[];
  availablePrivileges?: string[];
}

interface EditCourseDispatchProps extends EditEntityDispatchProps<Course> {
  loadCitations: () => Promise<string[]>;
  listPrivileges: (node: string) => Promise<{ node: string; result: string[] }>;
}

interface EditCourseProps
  extends EditEntityProps<Course>,
    EditCourseStateProps,
    EditCourseDispatchProps {
  updateTemplate: (update: (template: TemplateProps) => TemplateProps) => void;
  setPreventNavigation(b: boolean): void;
  redirect(to: LocationDescriptor): void;
}

type Props = EditCourseProps &
  WithStyles<
    | "hiddenTab"
    | "body"
    | "formControl"
    | "formControl2"
    | "form"
    | "footerActions"
    | "footer"
  >;

interface EditCourseState {
  activeTab?: number;
  canSave: boolean;
  changed: boolean;
  justSaved: boolean;
  editing: boolean;
  errored: boolean;
  editSecurity?: () => TargetListEntry[];
}
export const strings = languageStrings.courseedit;

class EditCourse extends React.Component<Props, EditCourseState> {
  constructor(props: Props) {
    super(props);

    this.state = {
      activeTab: 0,
      canSave: true,
      changed: false,
      justSaved: false,
      errored: false,
      editing: this.props.uuid ? true : false
    };
    if (this.props.uuid) {
      this.props.loadEntity(this.props.uuid);
    } else {
      this.props.modifyEntity({
        code: "",
        name: "",
        description: "",
        type: "Internal",
        versionSelection: "INSTITUTION_DEFAULT"
      });
    }
    this.props.loadCitations();
    this.props.listPrivileges("COURSE_INFO");
  }

  title = () => {
    const { editing } = this.state;
    const { entity } = this.props;
    return sprintf(
      editing ? strings.title : strings.newtitle,
      entity ? entity.name : ""
    );
  };

  componentDidMount() {
    this.props.updateTemplate(tp => ({
      ...templateDefaults(this.title())(tp),
      backRoute: routes.Courses.path,
      fixedViewPort: true,
      tabs: this.tabs(),
      footer: this.footer()
    }));
  }

  componentDidUpdate(prevProps: Props) {
    const newUuid = this.props.uuid;
    if (newUuid && prevProps.uuid !== newUuid) {
      this.props.loadEntity(newUuid);
    }
    this.updateTitle();
  }

  updateChanged = (changed: boolean) => {
    this.setState({ changed }, this.updateFooter);
    this.props.setPreventNavigation(changed);
  };

  modifyEntity = (c: Partial<Course>) => {
    if (this.props.entity) {
      this.props.modifyEntity({ ...this.props.entity, ...c });
      if (!this.state.changed) {
        this.updateChanged(true);
      }
    }
  };

  handleSave = () => {
    if (this.props.entity) {
      const { versionSelection, students } = this.props.entity;
      const vs = versionSelection === "DEFAULT" ? undefined : versionSelection;

      const course = {
        ...this.props.entity,
        versionSelection: vs,
        students: students || undefined,
        security: this.state.editSecurity
          ? { rules: this.state.editSecurity() }
          : this.props.entity.security
      };

      const { saveEntity } = this.props;
      const thiss = this;
      this.props.validateEntity(course).then(valErrors => {
        if (properties(valErrors).length === 0) {
          saveEntity(course)
            .then(editedCourse => {
              // change the URL, but only if it's new
              thiss.updateChanged(false);
              thiss.setState({ justSaved: true });
              if (!thiss.props.uuid) {
                thiss.props.loadEntity(editedCourse.result.uuid!);
              }
            })
            .catch(r => thiss.setState({ errored: true }));
        } else {
          thiss.setState({ activeTab: 0 }, thiss.updateTabs);
        }
      });
    }
  };

  handleChange(stateFieldName: string) {
    return (
      event: React.ChangeEvent<HTMLTextAreaElement | HTMLSelectElement>
    ) => {
      this.modifyEntity({ [stateFieldName]: event.target.value });
    };
  }

  handleIntChange(stateFieldName: string) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const val = event.target.value;
      let intVal: number | undefined = parseInt(val);
      if (!Number.isInteger(intVal)) {
        intVal = undefined;
      }
      this.modifyEntity({ [stateFieldName]: intVal });
    };
  }

  handleCheckboxChange(stateFieldName: string) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      this.modifyEntity({ [stateFieldName]: event.target.checked });
    };
  }

  handleDateChange(stateFieldName: string): (date?: DateTime) => void {
    return (date?: DateTime) => {
      this.modifyEntity({ [stateFieldName]: date ? date.toISO() : null });
    };
  }

  tabs() {
    return (
      <Tabs
        value={this.state.activeTab}
        onChange={this.handleTabChange}
        variant="fullWidth"
      >
        <Tab label={strings.tab} />
        <Tab label={entityStrings.edit.tab.permissions} />
      </Tabs>
    );
  }

  updateTabs() {
    this.props.updateTemplate(tp => ({ ...tp, tabs: this.tabs() }));
  }

  updateFooter() {
    this.props.updateTemplate(tp => ({ ...tp, footer: this.footer() }));
  }

  handleTabChange = (event: React.ChangeEvent<{}>, value: number) => {
    this.setState({ activeTab: value }, this.updateTabs);
  };

  handleAclChange = (e: AclEditorChangeEvent) => {
    this.updateChanged(true);
    this.setState(
      {
        canSave: e.canSave,
        changed: true,
        editSecurity: e.getAcls
      },
      this.updateFooter
    );
  };

  footer() {
    const { classes } = this.props;
    const { changed, canSave } = this.state;
    return (
      <Paper className={classes.footerActions}>
        <Button
          component={p => <Link {...p} to={routes.Courses.path} />}
          color="secondary"
        >
          {commonString.action.cancel}
        </Button>
        <Button
          onClick={this.handleSave}
          color="primary"
          disabled={!canSave || !changed}
        >
          {commonString.action.save}
        </Button>
      </Paper>
    );
  }

  updateTitle() {
    return this.props.updateTemplate(template => ({
      ...template,
      title: this.title()
    }));
  }

  render() {
    const {
      loading,
      entity,
      citations,
      availablePrivileges,
      classes
    } = this.props;
    const { AclEditor } = bridge;
    const typeval = strings.type;
    const versionval = strings.version;

    if (loading || !citations || !availablePrivileges) {
      return <Loader />;
    }

    if (!entity) {
      return <Error>Error loading entity</Error>;
    }

    const {
      code,
      name,
      description,
      type,
      departmentName,
      citation,
      students,
      from,
      until,
      versionSelection,
      archived,
      security,
      validationErrors
    } = entity;
    const { justSaved, errored } = this.state;
    const vs = versionSelection ? versionSelection : "DEFAULT";
    const fromDate = from ? DateTime.fromISO(from) : null;
    const untilDate = until ? DateTime.fromISO(until) : null;
    const val = validationErrors || {};

    let rules: TargetListEntry[] = [];
    if (security) {
      rules = security.rules;
    }

    return (
      <React.Fragment>
        <MessageInfo
          title={strings.saved}
          open={justSaved}
          onClose={() => this.setState({ justSaved: false })}
          variant="success"
        />
        <MessageInfo
          title={strings.errored}
          open={errored}
          onClose={() => this.setState({ errored: false })}
          variant="error"
        />
        <div className={classes.body}>
          <div className={this.state.activeTab === 0 ? "" : classes.hiddenTab}>
            <Grid>
              <div className={classes.form}>
                <TextField
                  id="name"
                  label={strings.name.label}
                  helperText={strings.name.help}
                  value={name || ""}
                  onChange={this.handleChange("name")}
                  margin="normal"
                  className={classes.formControl2}
                  required
                  error={val["name"] ? true : false}
                />

                <TextField
                  id="description"
                  label={strings.description.label}
                  helperText={strings.description.help}
                  value={description || ""}
                  onChange={this.handleChange("description")}
                  multiline
                  rows={2}
                  margin="normal"
                  className={classes.formControl2}
                />

                <TextField
                  id="code"
                  label={strings.code.label}
                  helperText={strings.code.help}
                  value={code}
                  onChange={this.handleChange("code")}
                  margin="normal"
                  className={classes.formControl}
                  required
                  error={val["code"] ? true : false}
                />

                <FormControl margin="normal" className={classes.formControl}>
                  <InputLabel htmlFor="type">{strings.type.label}</InputLabel>
                  <Select
                    id="type"
                    value={type}
                    input={<Input id="type-inp" />}
                    onChange={this.handleChange("type")}
                  >
                    <MenuItem key={"i"} value={"Internal"}>
                      {typeval.i}
                    </MenuItem>
                    <MenuItem key={"e"} value={"External"}>
                      {typeval.e}
                    </MenuItem>
                    <MenuItem key={"s"} value={"Staff"}>
                      {typeval.s}
                    </MenuItem>
                  </Select>
                </FormControl>

                <TextField
                  id="departmentName"
                  label={strings.department.label}
                  //helperText=""
                  value={departmentName || ""}
                  onChange={this.handleChange("departmentName")}
                  margin="normal"
                  className={classes.formControl}
                />

                <FormControl margin="normal" className={classes.formControl}>
                  <InputLabel htmlFor="citation">
                    {strings.citation.label}
                  </InputLabel>
                  <Select
                    id="citation"
                    value={citation || ""}
                    input={<Input id="citation-inp" />}
                    onChange={this.handleChange("citation")}
                  >
                    {citations.map(citation => (
                      <MenuItem key={citation} value={citation}>
                        {citation}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <DatePicker
                  id="from"
                  label={strings.startdate.label}
                  // format="MMMM Do YYYY"
                  value={fromDate}
                  onChange={this.handleDateChange("from")}
                  clearable
                  margin="normal"
                  className={classes.formControl}
                />

                <DatePicker
                  id="until"
                  label={strings.enddate.label}
                  // format="MMMM Do YYYY"
                  value={untilDate}
                  onChange={this.handleDateChange("until")}
                  clearable
                  margin="normal"
                  className={classes.formControl}
                />

                <FormControl margin="normal" className={classes.formControl}>
                  <InputLabel htmlFor="versionSelection">
                    {strings.version.label}
                  </InputLabel>
                  <Select
                    id="versionSelection"
                    value={vs}
                    input={<Input id="versionSelection-inp" />}
                    onChange={this.handleChange("versionSelection")}
                  >
                    <MenuItem key={"DEFAULT"} value={"INSTITUTION_DEFAULT"}>
                      {versionval.default}
                    </MenuItem>
                    <MenuItem key={"FORCE_LATEST"} value={"FORCE_LATEST"}>
                      {versionval.forcelatest}
                    </MenuItem>
                    <MenuItem key={"FORCE_CURRENT"} value={"FORCE_CURRENT"}>
                      {versionval.forcecurrent}
                    </MenuItem>
                    <MenuItem
                      key={"DEFAULT_TO_LATEST"}
                      value={"DEFAULT_TO_LATEST"}
                    >
                      {versionval.defaultlatest}
                    </MenuItem>
                    <MenuItem
                      key={"DEFAULT_TO_CURRENT"}
                      value={"DEFAULT_TO_CURRENT"}
                    >
                      {versionval.defaultcurrent}
                    </MenuItem>
                  </Select>
                  <FormHelperText>{strings.version.help}</FormHelperText>
                </FormControl>

                <TextField
                  id="students"
                  label={strings.students.label}
                  //helperText=""
                  value={students || " "}
                  onInput={this.handleIntChange("students")}
                  margin="normal"
                  className={classes.formControl}
                  type="number"
                  error={val["students"] ? true : false}
                />

                <FormGroup className={classes.formControl}>
                  <FormControlLabel
                    label={strings.archived.label}
                    control={
                      <Switch
                        checked={archived || false}
                        onChange={this.handleCheckboxChange("archived")}
                        value="archived"
                      />
                    }
                  />
                </FormGroup>
              </div>
            </Grid>
          </div>

          <div
            className={this.state.activeTab === 1 ? "" : classes.hiddenTab}
            style={{ height: "100%" }}
          >
            {/* TODO: priv list from API */}
            <AclEditor
              onChange={this.handleAclChange}
              acls={rules}
              allowedPrivs={availablePrivileges}
            />
          </div>
          <div className={classes.footer} />
        </div>
      </React.Fragment>
    );
  }
}

function mapStateToProps(state: StoreState): EditCourseStateProps {
  const { course, schema, acl } = state;
  return {
    loading: course.loading,
    entity: course.editingEntity,
    citations: schema.citations,
    availablePrivileges: acl.nodes["COURSE_INFO"]
  };
}

function mapDispatchToProps(dispatch: Dispatch<any>): EditCourseDispatchProps {
  const { workers, actions } = courseService;

  return {
    loadEntity: (uuid: string) => workers.read(dispatch, { uuid }),
    saveEntity: (entity: Course) => workers.update(dispatch, { entity }),
    modifyEntity: (entity: Course) =>
      dispatch(actions.modify({ entity: entity })),
    loadCitations: () => schemaService.workers.citations(dispatch, {}),
    listPrivileges: (node: string) =>
      aclService.workers.listPrivileges(dispatch, { node }),
    validateEntity: (entity: Course) => workers.validate(dispatch, { entity })
  };
}

export default withStyles(styles)(
  connect(mapStateToProps, mapDispatchToProps)(EditCourse)
);
