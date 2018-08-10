import { CircularProgress, Paper, Theme, Typography } from '@material-ui/core';
import List from '@material-ui/core/List';
import withStyles, { StyleRules, WithStyles } from '@material-ui/core/styles/withStyles';
import * as React from 'react';
import { connect, Dispatch } from 'react-redux';
import { sprintf } from 'sprintf-js';
import { TargetList } from '../api/acleditor';
import AppBarQuery from '../components/AppBarQuery';
import TargetListEntry from '../components/TargetListEntry';
import aclService from '../service/acl';
import { EditObjectDispatchProps, EditObjectProps, EditObjectStateProps } from '../service/generic';
import { StoreState } from '../store';
import { IDictionary } from '../util/dictionary';
import { prepLangStrings, sizedString } from '../util/langstrings';
import VisibilitySensor = require('react-visibility-sensor');

const styles = (theme: Theme) => ({
    overall: {
      padding: theme.spacing.unit * 2, 
      height: "100%"
    }, 
    results: {
      padding: theme.spacing.unit * 2, 
      position: "relative"
    }, 
    resultList: {
    },
    fab: {
      zIndex: 1000,
      position: 'fixed',
      bottom: theme.spacing.unit * 2,
      right: theme.spacing.unit * 5,
    }, 
    resultHeader: {
      display: "flex",
      justifyContent: "flex-end"
    }, 
    resultText: {
        flexGrow: 1
    }, 
    progress: {
        display: "flex", 
        justifyContent: "center"
    }
} as StyleRules)

interface SearchPrivilegeStateProps extends EditObjectStateProps<TargetList> {
    
}

interface SearchPrivilegeDispatchProps extends EditObjectDispatchProps<TargetList> {
    //listPrivileges: (node: string) => Promise<{node: string, result: string[]}>;
}

interface SearchPrivilegeProps extends EditObjectProps, 
                        SearchPrivilegeStateProps, 
                        SearchPrivilegeDispatchProps {
	id: string;
}

/*
Available values : INSTITUTION, ALL_SCHEMAS, SCHEMA, ALL_COLLECTIONS, COLLECTION, 
GLOBAL_ITEM_STATUS, ITEM_STATUS, ITEM_METADATA, DYNAMIC_ITEM_METADATA, 
ALL_WORKFLOWS, WORKFLOW, WORKFLOW_TASK, ALL_POWER_SEARCHES, POWER_SEARCH,
 ALL_FEDERATED_SEARCHES, FEDERATED_SEARCH, ALL_FILTER_GROUPS, FILTER_GROUP, 
 ITEM, ALL_REPORTS, REPORT, ALL_SYSTEM_SETTINGS, SYSTEM_SETTING, HIERARCHY_TOPIC, 
 ALL_COURSE_INFO, COURSE_INFO, ALL_DYNA_COLLECTIONS, DYNA_COLLECTION, ALL_TAXONOMIES,
  TAXONOMY, ALL_PORTLETS, PORTLET, PORTLET_HTML, PORTLET_RSS, PORTLET_IFRAME, 
  PORTLET_FREEMARKER, PORTLET_TASKS, PORTLET_SEARCH, PORTLET_MYRESOURCES, 
  PORTLET_FAVOURITES, PORTLET_RECENT, PORTLET_BROWSE, PORTLET_TASKSTATISTICS,
   ALL_CONNECTORS, CONNECTOR, ALL_KALTURAS, KALTURA, ALL_ECHOS, ECHO, 
   ALL_OAUTH_CLIENTS, OAUTH_CLIENT, ALL_USER_SCRIPTS, USER_SCRIPTS, 
   ALL_EXTERNAL_TOOLS, EXTERNAL_TOOL, ALL_LTI_CONSUMERS, LTI_CONSUMER, 
   ALL_HTMLEDITOR_PLUGINS, HTMLEDITOR_PLUGIN, ALL_HARVESTER_PROFILES, 
   HARVESTER_PROFILE, ALL_CUSTOM_LINKS, CUSTOM_LINK, ALL_MANAGING, MANAGING

 */
type InstitutionKey = 'INSTITUTION';
const InstitutionKey = 'INSTITUTION';
type SchemaKey = 'SCHEMA';
const SchemaKey = 'SCHEMA';
type CollectionKey = 'COLLECTION';
const CollectionKey = 'COLLECTION';
type CourseKey = 'COURSE_INFO';
const CourseKey = 'COURSE_INFO';
type WorkflowKey = 'WORKFLOW';
const WorkflowKey = 'WORKFLOW';
type ConnectorKey = 'CONNECTOR';
const ConnectorKey = 'CONNECTOR';
type TaxonomyKey = 'TAXONOMY';
const TaxonomyKey = 'TAXONOMY';
type ReportKey = 'REPORT';
const ReportKey = 'REPORT';
type OAuthClientKey = 'OAUTH_CLIENT';
const OAuthClientKey = 'OAUTH_CLIENT';
//type HierarchyKey = 'HIERARCHY';
//const HierarchyKey = 'HIERARCHY';


type AclKey = InstitutionKey |
    SchemaKey |
    CollectionKey |
    CourseKey |
    WorkflowKey |
    ConnectorKey |
    TaxonomyKey |
    ReportKey |
    OAuthClientKey;

type Props = SearchPrivilegeProps & WithStyles<'results' | 'overall' | 'fab' 
    | 'resultHeader' | 'resultText' | 'resultList' | 'progress'>;

interface AclsMap extends IDictionary<TargetList> {
    [InstitutionKey]: TargetList;
    [SchemaKey]: TargetList;
    [CollectionKey]: TargetList;
    [CourseKey]: TargetList;
    [WorkflowKey]: TargetList;
    [ConnectorKey]: TargetList;
    [TaxonomyKey]: TargetList;
    [ReportKey]: TargetList;
    [OAuthClientKey]: TargetList;
}

interface SearchPrivilegeState {
    query: string;
    confirmOpen: boolean;
    canCreate: boolean;
    searching: boolean;
    totalAvailable?: number;
    resumptionToken?: string;
    bottomVisible: boolean;
    acls: AclsMap;
    deleteDetails?: {
        uuid: string;
        name: string;
    }
}

const MaxPrivileges = 200;

export const strings = prepLangStrings("security", {
    title: "Privileges",
    sure: "Are you sure you want to delete - '%s'?", 
    confirmDelete: "It will be permanently deleted.", 
    rulesAvailable: {
        zero: "No rules available",
        one: "%d rule",
        more: "%d rules"
    }
});


/// Available at /page/security
class SearchPrivilege extends React.Component<Props, SearchPrivilegeState> {

    constructor(props: Props){
        super(props);

        var emptyTargetList = (node: AclKey): TargetList => { return { entries:[], node: node } };
        this.state = {
            query: '',
            confirmOpen: false,
            canCreate: false,
            acls: { 
                [InstitutionKey]: emptyTargetList(InstitutionKey),
                [SchemaKey]: emptyTargetList(SchemaKey),
                [CollectionKey]: emptyTargetList(CollectionKey),
                [CourseKey]: emptyTargetList(CourseKey),
                [WorkflowKey]: emptyTargetList(WorkflowKey),
                [ConnectorKey]: emptyTargetList(ConnectorKey),
                [TaxonomyKey]: emptyTargetList(TaxonomyKey),
                [ReportKey]: emptyTargetList(ReportKey),
                [OAuthClientKey]: emptyTargetList(OAuthClientKey)
            },
            searching: false, 
            bottomVisible: true
        }
    }

    maybeKeepSearching = (node: AclKey) => {
        if (this.state.bottomVisible) {
            this.fetchMore(node);
        }
    }

    fetchMore = (node: AclKey) => {
        const {resumptionToken,searching, query, acls} = this.state;
        if (resumptionToken && !searching && acls[node]!.entries.length < MaxPrivileges) {
            this.doSearch(query, false);
        }
    }

    nextSearch : NodeJS.Timer | null = null;

    doSearch = (q: string, reset: boolean) => {
        //const resumptionToken = reset ? undefined : this.state.resumptionToken;
        //const doReset = resumptionToken == undefined;
        //const { node, listPrivileges } = this.props;
        //const { bottomVisible } = this.state;
        this.setState({searching:true});
        const { loadObject } = this.props;
        const callback = (res: { result: TargetList }) => {
            const acls = this.state.acls;
            const newAcls = Object.assign({}, acls, {[res.result.node]: res.result});

            this.setState((prevState) => ({...prevState, 
                //courses: doReset ? sr.results : prevState.courses.concat(sr.results), 
                acls: newAcls,
                //totalAvailable: sr.available, 
                //resumptionToken: sr.resumptionToken, 
                searching: false
            }));
        };
        loadObject(InstitutionKey).then(callback);
        loadObject(SchemaKey).then(callback);
        loadObject(CollectionKey).then(callback);
        loadObject(CourseKey).then(callback);
        loadObject(WorkflowKey).then(callback);
        loadObject(ConnectorKey).then(callback);
        loadObject(TaxonomyKey).then(callback);
        loadObject(ReportKey).then(callback);
        loadObject(OAuthClientKey).then(callback);
        /*
        listPrivileges(node).then(sr => {
            //if (sr.resumptionToken && bottomVisible) setTimeout(this.maybeKeepSearching, 250);
            this.setState((prevState) => ({...prevState, 
                //courses: doReset ? sr.results : prevState.courses.concat(sr.results), 
                acls: {node, entries: sr.result.map((e) => {}) },
                //totalAvailable: sr.available, 
                //resumptionToken: sr.resumptionToken, 
                searching: false
            }));
        });*/
    }

    searchFromState = () => {
        const {query} = this.state;
        this.doSearch(query, true);
	}
	
    handleQuery = (q: string) => {
        this.setState({query:q});
        if (this.nextSearch)
        {
            clearTimeout(this.nextSearch);
        }
        this.nextSearch = setTimeout(this.searchFromState, 250);
    }

    visiblityCheck = (bottomVisible: boolean) => this.setState((prevState) => 
        ({...prevState, bottomVisible: prevState.bottomVisible && bottomVisible}))

    componentWillUnmount() {
        window.removeEventListener('scroll', this.onScroll, false);        
    }
  
    onScroll = () => {
        /*
        if ((window.innerHeight + window.scrollY) >= (document.body.offsetHeight - 400)) {
            this.fetchMore();
        }*/
    }

    componentDidMount() {
        window.addEventListener('scroll', this.onScroll, false);
        this.doSearch('', true);
        //this.props.checkCreate().then(canCreate => this.setState({canCreate}));
    }

    handleClose = () => {
        this.setState({confirmOpen:false});
    }
/*
    handleDelete = () => {
        if (this.state.deleteDetails) {
            const { uuid } = this.state.deleteDetails;
            this.handleClose();
            const {query} = this.state;
            this.props.deletePrivilege(uuid).then(
                _ => this.doSearch(query, true)
            );
        }
    }*/

    render() {
        const {Template, router, routes} = this.props.bridge;
        const {classes} = this.props;
        const {query, acls, searching} = this.state;
        
        //const totalAvailable = acls.entries.length;
        //const {onClick:clickNew, href:hrefNew} = router(routes.NewPrivilege)
/*
{this.state.deleteDetails && 
                    <ConfirmDialog open={confirmOpen} 
                        title={sprintf(strings.sure, this.state.deleteDetails.name)} 
                        onConfirm={this.handleDelete} onCancel={this.handleClose}>
                        {strings.confirmDelete}
                    </ConfirmDialog>}
*/
        const nodes: string[] = [];
        for (const key in acls) {
            if (acls.hasOwnProperty(key)) {
                nodes.push(key);
            }
        }
        return <Template title={strings.title} titleExtra={<AppBarQuery query={query} onChange={this.handleQuery}/>}>
            <div className={classes.overall}>
                
                <Paper className={classes.results}>

                    { nodes.map((node: string) => {
                        const entries = (acls[node] as TargetList).entries;
                        const totalAvailable = entries.length;
                        const { /*onClick: clickEdit,*/ href: hrefEdit} = router(routes.PrivilegeEdit(node));

                        return <div key={node}>
                                    <a href={hrefEdit}><h2>{node}</h2></a>
                                    
                                        <div className={classes.resultHeader}>
                                    <Typography className={classes.resultText} variant="subtitle1">{
                                        entries.length == 0 ? strings.rulesAvailable.zero : 
                                        sprintf(sizedString(totalAvailable||0, strings.rulesAvailable), totalAvailable||0)
                                    }</Typography>
                                </div>
                            <List className={classes.resultList}>
                            {
                            entries.map((entry) => {
                                    // href='#' onClick={()=>{}}
                                    return <TargetListEntry entry={entry} />
                                })
                            }
                            <VisibilitySensor onChange={this.visiblityCheck}/>
                            </List>
                            {searching && <div className={classes.progress}><CircularProgress/></div>}

                        </div>
                    }) }

                </Paper>
            </div>
        </Template>
    }
}

function mapStateToProps(state: StoreState): SearchPrivilegeStateProps {
    return { object: {entries: [], node: ''}};
}

function mapDispatchToProps(dispatch: Dispatch<any>): SearchPrivilegeDispatchProps {
    const { workers, actions } = aclService;
    return {
        //deletePrivilege: (id: string) => workers.delete(dispatch, {id}), 
        //listPrivileges: (node: string) => workers.listPrivileges(dispatch, {node})
        //,
        //checkCreate: () => workers.checkPrivs(dispatch, {privilege:["CREATE_COURSE_INFO"]}).then(p => p.indexOf("CREATE_COURSE_INFO") != -1)
        loadObject: (id: string) => workers.read(dispatch, {id}),
        saveObject: (object: TargetList) => workers.update(dispatch, { object }),
        modifyObject: (object: TargetList) => dispatch(actions.modify({ object })),
        validateObject: (object: TargetList) => workers.validate(dispatch, { object })
    };
}

export default withStyles(styles)(connect(mapStateToProps, mapDispatchToProps)(SearchPrivilege));