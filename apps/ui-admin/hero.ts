import { heroui } from "@heroui/react";

export default heroui({
  themes: {
    light: {
      colors: {
        background: "#F8F7FC",
        foreground: "#3D2B6B",
        primary: {
          DEFAULT: "#8B5CF6",
          foreground: "#FFFFFF",
        },
        secondary: {
          DEFAULT: "#E9DDFA",
          foreground: "#6328C7",
        },
        content1: "#FFFFFF",
        content2: "#F3F0F8",
        content3: "#E8DDF7",
        content4: "#E6E2ED",
        default: {
          100: "#F3F0F8",
          200: "#E9DDFA",
          300: "#E6E2ED",
          400: "#A393C1",
          500: "#8976A8",
          600: "#6F5C8F",
          foreground: "#3D2B6B",
        },
        divider: "#E6E2ED",
      },
    },
    dark: {
      colors: {
        background: "#1A1829",
        foreground: "#EEEBF4",
        primary: {
          DEFAULT: "#A78BFA",
          foreground: "#1A1829",
        },
        secondary: {
          DEFAULT: "#4A3B6B",
          foreground: "#D5C6E9",
        },
        content1: "#252138",
        content2: "#342F4A",
        content3: "#3F3A5C",
        content4: "#3E3B51",
        default: {
          100: "#342F4A",
          200: "#3F3A5C",
          300: "#4A3B6B",
          400: "#6F5C8F",
          500: "#B7A9CB",
          600: "#D5C6E9",
          foreground: "#EEEBF4",
        },
        divider: "#3E3B51",
      },
    },
  },
});
