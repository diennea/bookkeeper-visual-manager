module.exports = {
    root: true,
    env: {
      browser: true,
      node: true,
      jest: true
    },
    parserOptions: {
      parser: "babel-eslint",
      sourceType: "module"
    },
    extends: [
      "eslint:recommended",
      "plugin:vue/recommended"
    ],
    rules: {
      "vue/script-indent": ["error", 4, {"switchCase": 1}],
      "vue/multiline-html-element-content-newline": 'off',
      "vue/singleline-html-element-content-newline": 'off',
      "vue/mustache-interpolation-spacing": ["error", "always"],
      "vue/require-default-prop": 'off',
      "vue/max-attributes-per-line": 'off',
      "vue/attributes-order": 'off',
      "vue/html-closing-bracket-newline": 'off',
      "vue/html-indent": 'off',
      'vue/valid-v-slot': ['error', { allowModifiers: true }],
      // disallow use of Object.prototypes builtins directly
      "no-prototype-builtins": 'off',
      // specify curly brace conventions for all control statements
      "curly": ['error', 'multi-line'],
      // require or disallow space before function opening parenthesis
      'space-before-function-paren': ['error', { anonymous: 'always', named: 'never', asyncArrow: 'always' }],
      // require or disallow spaces inside parentheses
      'space-in-parens': ['error', 'never'],
      // require spaces around operators
      'space-infix-ops': 'error',
      // disallow whitespace before properties
      'no-whitespace-before-property': 'error',
      // disallow multiple empty lines, only one newline at the end, and no new lines at the beginning
      'no-multiple-empty-lines': ['error', { max: 2, maxBOF: 1, maxEOF: 0 }],
      // disallow tab characters entirely
      'no-tabs': 'error',
      // disallow use of chained assignment expressions
      'no-multi-assign': ['error'],
      // enforces spacing between keys and values in object literal properties
      'key-spacing': ['error', { beforeColon: false, afterColon: true }],
      // enforce spacing before and after comma
      'comma-spacing': ['error', { before: false, after: true }],
      // enforce spacing before and after semicolons
      'semi-spacing': ['error', { before: false, after: true }],
      // enforce spacing inside array brackets
      'array-bracket-spacing': ['error', 'never'],
      // require a newline around variable declaration
      'one-var-declaration-per-line': ['error', 'always'],
      // enforce newline at the end of file, with no multiple empty lines
      'eol-last': ['error', 'always'],
      // treat var statements as if they were block scoped
      'block-scoped-var': 'error',
      // disallow empty statements
      "no-empty": ["error"],
      // disallow use of eval()
      'no-eval': 'error',
      // disallow fallthrough of case statements
      'no-fallthrough': 'error',
      // disallow the use of alert, confirm, and prompt
      'no-alert': 'warn',
      // disallow comparisons with the value NaN
      'use-isnan': 'error',
      // disallow trailing whitespace at the end of lines
      "no-trailing-spaces": ["error"],
      // disallow usage of configurable warning terms in comments: e.g. todo
      'no-warning-comments': ['off', { terms: ['todo', 'fixme', 'xxx'], location: 'start' }],
      // disallow use of console (only on build)
      "no-console": process.env.NODE_ENV === "production" ? "error" : "off",
      // disallow use of debugger (only on build)
      "no-debugger": process.env.NODE_ENV === "production" ? "error" : "off",
      // disallow declaration of variables that are not used in the code
      'no-unused-vars': ['error', { vars: 'all', args: 'after-used', ignoreRestSiblings: true }],
      // disallow use of variables before they are defined
      'no-use-before-define': ['error', { functions: false, classes: true, variables: true }],
    }
  };
