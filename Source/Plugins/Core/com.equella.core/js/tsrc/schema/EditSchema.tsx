import * as React from 'react';
import { Button, TextField, Grid } from '@material-ui/core';
import { Schema } from '../api';
import schemaService from './index';
import { StoreState } from '../store';
import { connect, Dispatch } from 'react-redux';
import { push } from 'react-router-redux'

//import List, { ListItem, ListItemText } from 'material-ui/List';
/*
import { withStyles } from 'material-ui/styles';
const styles = (theme: Theme) => ({
    root: {
      width: '100%',
      maxWidth: 360,
      backgroundColor: theme.palette.background.paper
    }
  });*/

interface EditSchemaProps {
    loadSchema: (uuid: string) => void;
    saveSchema: (schema: Schema) => void;
    onCancel: () => void;
    schema: Schema;
    //root: any;
}

interface EditSchemaState {
    uuid?: string;
    name?: string;
    description?: string;
    namePath?: string;
    descriptionPath?: string;
    definition?: any;
}

class EditSchema extends React.Component<EditSchemaProps, EditSchemaState> {

    constructor(props: EditSchemaProps){
        super(props);
        var uuids = window.location.href.match(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
        if (uuids){
            this.props.loadSchema(uuids[0]);
        }

        this.state = {
            name: '',
            description: '',
            namePath: '',
            descriptionPath: ''
        };
    }

    componentWillReceiveProps(nextProps: EditSchemaProps){
        const schema = nextProps.schema;
        if (schema){
            const { uuid, name, description, namePath, descriptionPath } = schema;
            this.setState({ uuid, name, description, namePath, descriptionPath });
        }
    }

    handleSave() {
        const { uuid, name, description, namePath, descriptionPath } = this.state;
        if (name){
            let schema = {
                uuid,
                name: name!,
                description,
                namePath: namePath!,
                descriptionPath: descriptionPath!,
                definition: {} //fixme
            };
            this.props.saveSchema(schema);
        }
    }

    handleCancel() {
        this.props.onCancel();
    }

    handleChange(stateFieldName: string): (event: React.ChangeEvent<any>) => void {
        return (event: React.ChangeEvent<any>) => {
            this.setState({ [stateFieldName]: event.target.value });
        };        
    }

    render() {
        const { name, description, namePath, descriptionPath } = this.state;
        /*
        <Stepper>
                <Step title="Basic Details" active>
                    
                </Step>
                <Step title="Permissions">
                </Step>
            </Stepper>*/
        return             <Grid>
                <div>
                    

                    <TextField id="name" 
                        label="Name" 
                        helperText="Schema name, e.g. Advanced EQUELLA studies"
                        value={name}
                        onChange={this.handleChange('name')}
                        fullWidth
                        margin="normal"
                        required
                        />

                    <TextField id="description" 
                        label="Description" 
                        helperText="A brief description"
                        value={description}
                        onChange={this.handleChange('description')}
                        fullWidth
                        multiline
                        rows={3}
                        margin="normal"
                        />

                    <TextField id="namePath" 
                        label="Name Path" 
                        helperText="Item name path, e.g. /xml/item/name"
                        value={namePath}
                        onChange={this.handleChange('namePath')}
                        fullWidth
                        margin="normal"
                        required
                            />

                    <TextField id="descriptionPath" 
                        label="Description Path" 
                        helperText="Item description path, e.g. /xml/item/description"
                        value={descriptionPath}
                        onChange={this.handleChange('descriptionPath')}
                        fullWidth
                        margin="normal"
                        required
                            />

                    <Button color="primary" onClick={this.handleSave.bind(this)} variant="raised">Save</Button>
                    <Button onClick={this.handleCancel.bind(this)} variant="raised">Cancel</Button>
                </div>
            </Grid>
    }
}

function mapStateToProps(state: StoreState) {
    const { schema } = state;
    return {
        schema: schema.editingEntity
    };
}

function mapDispatchToProps(dispatch: Dispatch<any>) {
    const { workers } = schemaService;
    return {
        loadSchema: (uuid: string) => workers.read(dispatch, {uuid}),
        saveSchema: (entity: Schema) => workers.update(dispatch, {entity}),
        onCancel: () => dispatch(push('/'))
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(EditSchema as any);