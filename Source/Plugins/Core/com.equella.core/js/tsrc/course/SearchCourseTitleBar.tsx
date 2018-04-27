import * as React from 'react';
import { Icon, Theme } from 'material-ui';
import courseService from './index';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import { Bridge } from '../api/bridge';

import withStyles, { WithStyles, StyleRulesCallback } from 'material-ui/styles/withStyles';
import {fade} from 'material-ui/styles/colorManipulator';

const styles: StyleRulesCallback<'root' | 'queryWrapper' | 'queryIcon' | 'queryField'> = (theme: Theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper
    },
    queryWrapper: {
        fontFamily: theme.typography.fontFamily,
        position: "relative",
        marginRight: theme.spacing.unit * 2,
        marginLeft: theme.spacing.unit * 2,
        borderRadius: 2,
        background: fade(theme.palette.common.white, 0.15),
        width: "400px"
      },
      queryIcon: {
        width: theme.spacing.unit * 9,
        height: "100%",
        position: "absolute",
        pointerEvents: "none",
        display: "flex",
        alignItems: "center",
        justifyContent: "center"
      },
      queryField: {
        font: "inherit",
        padding: `${theme.spacing.unit}px ${theme.spacing.unit}px ${theme.spacing.unit}px ${theme.spacing.unit * 9}px`,
        border: 0,
        display: "block",
        verticalAlign: "middle",
        whiteSpace: "normal",
        background: "none",
        margin: 0,
        color: "rgb(255, 255, 255)",
        width: "100%"
      }
  });

  

interface SearchCourseTitleBarProps {
    onSearch: (query?: string) => void;
    bridge: Bridge;
    query?: string;
    root: any;
}

interface SearchCourseTitleBarState {
    query?: string;
}

class SearchCourseTitleBar extends React.Component<SearchCourseTitleBarProps & WithStyles<'root' | 'queryWrapper' | 'queryIcon' | 'queryField'>, SearchCourseTitleBarState> {

    constructor(props: SearchCourseTitleBarProps & WithStyles<'root' | 'queryWrapper' | 'queryIcon' | 'queryField'>){
        super(props);
    }

    componentDidMount() {
        this.props.onSearch('');
    }

    onChange(): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.setState({ query: event.target.value });
            this.props.onSearch(event.target.value);
        };
    }

    render() {
        const { classes } = this.props;
        return <div className={classes.queryWrapper}>
                <div className={classes.queryIcon}>
                    <Icon>search</Icon>
                </div>
                <input type="text" className={classes.queryField} onChange={this.onChange()} />
            </div>
    }
}

function mapStateToProps(state: StoreState) {
    const { course } = state;
    return {
        query: course.query
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = courseService;
    return {
        onSearch: (query?: string) => workers.search(dispatch, {query})
    }
}

export default withStyles(styles)<{}>(connect(mapStateToProps, mapDispatchToProps)(SearchCourseTitleBar as any) as any);
//export default connect(mapStateToProps, mapDispatchToProps)(SearchCourseTitleBar as any);