module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        "header-max-length": () => [0, "always", 72],
        "subject-case": () => [0, "never", ["sentence-case", "start-case", "pascal-case", "upper-case"]],
    }
};
