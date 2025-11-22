import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from '@typescript-eslint/eslint-plugin'
import tsparser from '@typescript-eslint/parser'
import { defineConfig, globalIgnores } from 'eslint/config'

// Pull the recommended rules from the plugin to avoid using string 'extends'
// which can cause plugin-resolution ordering issues with the flat config.
const tsRecommendedRules = tseslint?.configs?.recommended?.rules ?? {}

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    plugins: {
      '@typescript-eslint': tseslint,
    },
    // Merge the recommended TypeScript rules directly to avoid 'plugin not found'
    // issues when using string-form `extends` with the flat config.
    rules: {
      ...tsRecommendedRules,
      'no-undef': 'off',
    },
    languageOptions: {
      parser: tsparser,
      ecmaVersion: 2020,
      globals: globals.browser,
    },
  },
])
