import { ThemeProvider } from "@mui/material";
import { createTheme } from "@mui/material/styles";
import { addDecorator } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";

addDecorator((Story) => (
  <MemoryRouter>
    <ThemeProvider theme={createTheme()}>
      <Story />
    </ThemeProvider>
  </MemoryRouter>
));
