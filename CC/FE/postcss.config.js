import { resolve } from "path";

export const plugins = {
  // Ensures the default variables are available
  "postcss-custom-properties-fallback": {
    importFrom: resolve("react-spring-bottom-sheet/defaults.json"),
  },
};
