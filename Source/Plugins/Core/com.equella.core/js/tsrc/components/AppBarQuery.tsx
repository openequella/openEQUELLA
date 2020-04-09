import * as React from "react";
import { useState, FunctionComponent, ChangeEvent } from "react";
import { Icon, Theme } from "@material-ui/core";
import { fade } from "@material-ui/core/styles/colorManipulator";
import { makeStyles } from "@material-ui/core/styles";
import { commonString } from "../util/commonstrings";
import { debounce } from "lodash";

interface AppBarQueryProps {
  onChange: (query: string) => void;
  query: string;
}

const useStyles = makeStyles((theme: Theme) => ({
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
}));

const AppBarQuery: FunctionComponent<AppBarQueryProps> = ({
  onChange,
  query
}: AppBarQueryProps) => {
  const classes = useStyles();
  const [searchText, setSearchText] = useState(query);
  const [debounceHandler] = useState(() =>
    debounce((value: string) => onChange(value), 500)
  );

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
        onChange={({ target: { value } }: ChangeEvent<HTMLInputElement>) => {
          setSearchText(value);
          debounceHandler(value);
        }}
      />
    </div>
  );
};

export default AppBarQuery;
