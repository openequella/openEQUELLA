import * as React from 'react';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete'
import { withStyles, Theme, WithStyles, ListItem, ListItemText, ListItemSecondaryAction, IconButton } from '@material-ui/core';

const styles = withStyles((theme: Theme) => (
{
    searchResultContent: {
        display: "flex",
        marginTop: "8px"
    },
    itemThumb: {
        maxWidth: "88px",
        maxHeight: "66px",
        marginRight: "12px"
    },
    resultLink: {
        textDecoration: "none",
        fontSize: "1.3125rem",
        // fontWeight: 500,
        fontFamily: "Roboto, Helvetica, Arial, sans-serif",
        lineHeight: "1.16667em"
    }
}));

export interface SearchResultProps {
    href: string;
    onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    onDelete?: () => void;
    primaryText: string;
    secondaryText?: string;
}

type PropsWithStyles = SearchResultProps & WithStyles<"searchResultContent" | "itemThumb" | "resultLink">

class SearchResult extends React.Component<PropsWithStyles> {
    render() {
        const {classes, onDelete} = this.props
        const link: any = <a href={this.props.href} className={classes.resultLink} 
                onClick={this.props.onClick}>{this.props.primaryText}</a>
        //
        const content: any = <Typography variant="body1">{this.props.secondaryText}</Typography>;
        return <ListItem button onClick={this.props.onClick} divider>
                <ListItemText disableTypography primary={link} secondary={content} />
                <ListItemSecondaryAction>
                { onDelete && <IconButton onClick={onDelete}><DeleteIcon/></IconButton> }
                </ListItemSecondaryAction>
        </ListItem>;
    }
}

export default styles<SearchResultProps>(SearchResult);