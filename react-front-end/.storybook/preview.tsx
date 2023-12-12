import { ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import { MemoryRouter } from "react-router-dom";

import type { Preview } from '@storybook/react';

const preview: Preview = {
  decorators: [
    (Story) => (
      <MemoryRouter>
        <ThemeProvider theme={createTheme()}>
          <Story />
        </ThemeProvider>
      </MemoryRouter>
    ),
  ],
};

export default preview;