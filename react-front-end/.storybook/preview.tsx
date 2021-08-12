import { addDecorator } from "@storybook/react";
import {MemoryRouter} from 'react-router-dom';

addDecorator((Story) =>
  <MemoryRouter><Story/></MemoryRouter>);
