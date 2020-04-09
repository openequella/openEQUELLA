import * as React from "react";
import { WithStyles, Icon, withStyles, Theme } from "@material-ui/core";
import { fade } from "@material-ui/core/styles/colorManipulator";
import { createStyles } from "@material-ui/core/styles";
import { commonString } from "../util/commonstrings";
import { debounce } from "lodash";

interface AppBarQueryProps {
  onSearch?: (query?: string) => void;
  onChange: (query: string) => void;
  query: string;
}
interface AppBarQueryState {
  searchText: string;
}
const styles = (theme: Theme) =>
  createStyles({
    queryWrapper: {
      position: "relative",
      fontFamily: theme.typography.fontFamily,
      marginRight: theme.spacing(2),
      marginLeft: theme.spacing(2),
      borderRadius: 2,
      background: fade(theme.palette.common.white, 0.15),
      width: "400px"
    },
    queryIcon: {
      width: theme.spacing(9),
      height: "100%",
      position: "absolute",
      pointerEvents: "none",
      display: "flex",
      alignItems: "center",
      justifyContent: "center"
    },
    queryField: {
      font: "inherit",
      padding: theme.spacing(1),
      paddingLeft: theme.spacing(9),
      border: 0,
      display: "block",
      verticalAlign: "middle",
      whiteSpace: "normal",
      background: "none",
      margin: 0,
      color: "inherit",
      width: "100%"
    }
  });

class AppBarQuery extends React.Component<
  AppBarQueryProps & WithStyles<"queryWrapper" | "queryIcon" | "queryField">,
  AppBarQueryState
> {
  constructor(
    props: AppBarQueryProps &
      WithStyles<"queryWrapper" | "queryIcon" | "queryField">
  ) {
    super(props);
    this.state = {
      searchText: ""
    };
  }

  // Do a search after user stops typing for 500 milliseconds.
  // Purescript passes a concrete search function to onChange.
  debouncedSearch = debounce(() => {
    this.props.onChange(this.state.searchText);
  }, 500);

  handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.setState(
      {
        searchText: e.target.value
      },
      () => {
        this.debouncedSearch();
      }
    );
  };

  render() {
    const { classes } = this.props;
    const { searchText } = this.state;
    return (
      <div className={classes.queryWrapper}>
        <div className={classes.queryIcon}>
          <Icon>search</Icon>
        </div>
        <input
          type="text"
          aria-label={commonString.action.search}
          className={classes.queryField}
          value={searchText}
          onChange={this.handleTextChange}
        />
      </div>
    );
  }
}

export default withStyles(styles)(AppBarQuery);
