import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["src/test/ts/**/*.test.ts"],
    coverage: {
      provider: "v8",
      include: ["src/main/ts/**/*.ts"],
      exclude: ["src/main/ts/index.ts"],
      all: true,
    },
  },
});
