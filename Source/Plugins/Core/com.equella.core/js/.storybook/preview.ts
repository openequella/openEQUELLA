import { addDecorator } from "@storybook/react";
import { withA11y } from "@storybook/addon-a11y";
import StoryRouter from "storybook-react-router";

addDecorator(withA11y);
addDecorator(StoryRouter());
