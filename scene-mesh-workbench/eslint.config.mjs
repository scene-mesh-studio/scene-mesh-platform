import { dirname } from "path";
import { fileURLToPath } from "url";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
});

const eslintConfig = [
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  ...compat.extends("prettier"), // 必须放在最后，覆盖冲突的规则
  {
    files: ["**/*.{js,jsx,ts,tsx}"],
    rules: {
      // React 相关规则
      "react/react-in-jsx-scope": "off", // Next.js 自动导入 React
      "react/prop-types": "off", // TypeScript 已经提供类型检查
      "react/display-name": "warn",
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",

      // TypeScript 相关规则
      "@typescript-eslint/no-unused-vars": ["error", { 
        "argsIgnorePattern": "^_",
        "varsIgnorePattern": "^_" 
      }],
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-var-requires": "error",

      // 通用 JavaScript/TypeScript 规则
      "no-console": "warn",
      "no-debugger": "error",
      "no-duplicate-imports": "error",
      "no-unused-expressions": "error",
      "prefer-const": "error",
      "no-var": "error",
      
      // Next.js 特定规则
      "@next/next/no-img-element": "error",
      "@next/next/no-html-link-for-pages": "error",
    },
  },
  {
    files: ["**/*.{js,jsx}"],
    rules: {
      // JavaScript 文件的特定规则
      "@typescript-eslint/no-var-requires": "off",
    },
  },
  {
    files: ["**/*.config.{js,mjs,ts}"],
    rules: {
      // 配置文件的特定规则
      "no-console": "off",
      "@typescript-eslint/no-var-requires": "off",
    },
  },
  {
    ignores: [
      ".next/**",
      "out/**",
      "build/**",
      "dist/**",
      "node_modules/**",
      ".git/**",
      "*.min.js",
      "public/**",
      "eslint-show-config.json",
      "src/lib/prisma/client/**",
      "prisma/generated/**",
    ],
  },
];

export default eslintConfig;