import { heroui } from "@heroui/react";

export default heroui({
  themes: {
    light: {
      colors: {
        primary: {
          DEFAULT: "#8B5CF6",
          foreground: "#FFFFFF",
        },
        secondary: {
          DEFAULT: "#F3E8FF",
          foreground: "#6B21A8",
        },
        background: "#FEFBFF",
        foreground: "#4C1D95",
      },
    },
    dark: {
      colors: {
        primary: {
          DEFAULT: "#A78BFA",
          foreground: "#1E1B4B",
        },
        secondary: {
          DEFAULT: "#5B21B6",
          foreground: "#DDD6FE",
        },
        background: "#1E1B4B",
        foreground: "#F3E8FF",
      },
    },
  },
});
