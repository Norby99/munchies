import { defineConfig } from "vitest/config";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [tsconfigPaths()],

  test: {
    include: ["src/test/ts/**/*.test.ts"],
    coverage: {
      provider: "v8",
      include: ["src/**/*.{ts,tsx}"],
      exclude: ["src/main/ts/domain/external-modules.{ts,tsx}"],
      all: true,
      reporter: ["text", "lcov", "json"],
      // thresholds: { lines: 80, functions: 80, branches: 75, statements: 80},
    },
  },
});
