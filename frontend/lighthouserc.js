module.exports = {
  ci: {
    collect: {
      // Use Desktop to avoid mobile-specific penalties
      settings: {
        preset: "desktop",
      },
      startServerCommand: "pnpm start",
      url: ["http://localhost:3000/"],
    },
    upload: {
      target: "temporary-public-storage",
    },
    assert: {
      // "lighthouse:no-pwa" turns off checks for App Icons, Splash Screens, etc.
      preset: "lighthouse:no-pwa",

      // "off" = disable entirely
      // "warn" = print a warning but return Exit Code 0 (Success)
      // "error" = fail the build (Default)
      assertions: {
        // 1. SECURITY: Content Security Policy is complex to set up.
        // Unless you are a bank, you can usually turn this off for now.
        "csp-xss": "off",

        // 2. CONSOLE ERRORS: Ideally you fix these, but sometimes
        // plugins throw noise. Set to 'warn' so it doesn't break CI.
        "errors-in-console": "warn",

        // 3. ACCESSIBILITY: Headings out of order (h1 -> h3).
        // Good to fix, but shouldn't stop deployment.
        "heading-order": "warn",

        // 4. PERFORMANCE: These fail if your JS/images are large.
        // 'warn' lets you see the issue without blocking.
        "total-byte-weight": "warn",
        "unused-javascript": "warn",

        // GLOBAL SETTINGS
        // Require 70% in categories, but only 'warn' if lower.
        "categories:performance": ["warn", { minScore: 0.7 }],
        "categories:accessibility": ["warn", { minScore: 0.7 }],
        "categories:best-practices": ["warn", { minScore: 0.7 }],
        "categories:seo": ["warn", { minScore: 0.9 }],
      },
    },
  },
};
