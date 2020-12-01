module.exports = {
    moduleFileExtensions: ["js", "json", "vue"],
    transform: {
        ".*\\.(vue)$": "vue-jest",
        ".*\\.(js)$": "babel-jest"
    },
    transformIgnorePatterns: ["/node_modules/(?!(vuetify)/)"],
    moduleNameMapper: {
        "^@/(.*)$": "<rootDir>/src/$1"
    },
    testMatch: ["**/tests/**/*.(spec|test).(js)"],
    reporters: ["default", "jest-junit"]
}
