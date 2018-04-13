import { Schema } from '../api';
import { entityService } from '../entity/index';

const schemaService = entityService<Schema>('SCHEMA');
export default schemaService;