import { heroui } from "@heroui/react";

export default heroui({
  themes: {
    light: {
      colors: {
        primary: {
          DEFAULT: "oklch(0.5417 0.1790 288.0332)",
          foreground: "oklch(1.0000 0 0)",
        },
        secondary: {
          DEFAULT: "oklch(0.9174 0.0435 292.6901)",
          foreground: "oklch(0.4143 0.1039 288.1742)",
        },
        background: "oklch(0.9730 0.0133 286.1503)",
        foreground: "oklch(0.3015 0.0572 282.4176)",
      },
    },
    dark: {
      colors: {
        primary: {
          DEFAULT: "oklch(0.7162 0.1597 290.3962)",
          foreground: "oklch(0.1743 0.0227 283.7998)",
        },
        secondary: {
          DEFAULT: "oklch(0.3139 0.0736 283.4591)",
          foreground: "oklch(0.8367 0.0849 285.9111)",
        },
        background: "oklch(0.1743 0.0227 283.7998)",
        foreground: "oklch(0.9185 0.0257 285.8834)",
      },
    },
  },
});
