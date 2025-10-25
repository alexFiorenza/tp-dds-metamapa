'use client';

import React from 'react';
import { useTheme } from 'next-themes';
import { motion } from 'motion/react';

interface LogoProps {
  /**
   * Width of the logo
   * @default "100%"
   */
  width?: string | number;
  /**
   * Height of the logo
   * @default "100%"
   */
  height?: string | number;
  /**
   * Custom fill color. If not provided, it will use the theme-based color
   */
  fill?: string;
  /**
   * Additional CSS classes
   */
  className?: string;
}

export const Logo: React.FC<LogoProps> = ({
  width = "100%",
  height = "100%",
  fill,
  className
}) => {
  const { theme, resolvedTheme } = useTheme();
  const [mounted, setMounted] = React.useState(false);

  React.useEffect(() => {
    setMounted(true);
  }, []);

  const getFillColor = () => {
    if (fill) return fill;

    if (!mounted) return 'currentColor';

    const currentTheme = resolvedTheme || theme;
    return currentTheme === 'dark' ? '#ffffff' : '#000000';
  };

  return (
    <motion.svg
      width={width}
      height={height}
      viewBox="0 0 200 200"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      whileHover={{ rotate: [0, -5, 5, 0], transition: { duration: 0.5 } }}
    >
      <title>MetaMapa Isotype</title>
      <motion.path
        d="M25 175C25 75 50 25 100 100C150 25 175 75 175 175H150C150 100 135 75 100 125C65 75 50 100 50 175H25Z"
        fill={getFillColor()}
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={{ duration: 1, ease: "easeInOut" }}
      />
    </motion.svg>
  );
};

export const LogoBlack: React.FC<Omit<LogoProps, 'fill'>> = (props) => {
  return <Logo {...props} fill="#000000" />;
};

export const LogoWhite: React.FC<Omit<LogoProps, 'fill'>> = (props) => {
  return <Logo {...props} fill="#ffffff" />;
};
