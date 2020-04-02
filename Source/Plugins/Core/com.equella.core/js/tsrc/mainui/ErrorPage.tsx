import * as React from "react";
import { ErrorResponse } from "../api/errors";
import { CardContent, Card, makeStyles, Typography } from "@material-ui/core";

const useStyles = makeStyles(t => ({
  errorPage: {
    display: "flex",
    justifyContent: "center",
    marginTop: t.spacing(8)
  }
}));

interface ErrorPageProps {
  error: ErrorResponse;
}

export default React.memo(function ErrorPage({
  error: { code, error, error_description }
}: ErrorPageProps) {
  const classes = useStyles();
  return (
    <div id="errorPage" className={classes.errorPage}>
      <Card>
        <CardContent>
          <Typography variant="h3" color="error">
            {code} : {error}
          </Typography>
          {error_description && (
            <Typography variant="h5">{error_description}</Typography>
          )}
        </CardContent>
      </Card>
    </div>
  );
});
