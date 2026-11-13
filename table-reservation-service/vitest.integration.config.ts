import { defineConfig } from "vitest/config";
import tsconfigPaths from "vite-tsconfig-paths";

// Integration tests: they need the external dependency they exercise (a MongoDB, named by
// MONGODB_TEST_URI) and are skipped without it. Kept out of the unit run (vitest.config.ts).
export default defineConfig({
  plugins: [tsconfigPaths()],

  test: {
    include: ["src/integrationTest/ts/**/*.test.ts"],
    env: { LOG_LEVEL: "silent" },
    // One database shared by the whole run.
    fileParallelism: false,
  },
});
