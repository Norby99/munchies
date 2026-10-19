import { defineConfig } from "vitest/config";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [tsconfigPaths()],

  test: {
    include: ["src/test/ts/**/*.test.ts"],
    // Keeps the application logger quiet during test runs.
    env: { LOG_LEVEL: "silent" },
    coverage: {
      provider: "v8",
      include: ["src/**/*.{ts,tsx}"],
      all: true,
      thresholds: { lines: 70, functions: 70, branches: 70, statements: 70},
    },
  },
});
