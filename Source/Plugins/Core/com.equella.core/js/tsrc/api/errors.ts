import v4 = require("uuid/v4");
import {AxiosError} from "axios";

export interface ErrorResponse
{
  id: string;
  code?: number|string;
  error: string; 
  description?: string;
}

export const generateNewErrorID = (error:string, code?:number, description?:string) => {
  return {
    id: v4(),
    code: code||500,
    description,
    error
  };
};

export const generateFromAxiosError = (error: AxiosError) =>{
  return {
    id: v4(),
    code:error.code,
    error:error.name,
    description:error.message
  }
};
