import { Entity } from "./Entity";
import { IDictionary } from "../util/dictionary";

export interface Schema extends Entity {
  namePath: string;
  descriptionPath: string;
  definition: SchemaDefinition;
}

export interface SchemaDefinition {
  [key: string]: SchemaNode;
}

interface SchemaNodeBaseProps {
  _indexed?: boolean;
  _field?: boolean;
  _nested?: boolean;
  _type?: string;
}

interface SchemaNodeDictProps extends IDictionary<SchemaNode> {}

export type SchemaNode = SchemaNodeBaseProps & SchemaNodeDictProps;
