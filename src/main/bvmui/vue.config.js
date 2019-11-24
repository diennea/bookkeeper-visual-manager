const argv = require("yargs").argv;
const devTarget = argv.s || "http://localhost:8086";

module.exports = {
  devServer: {
    proxy: {
      "/api": {
        target: devTarget,
        secure: false
      }
    }
  },
  transpileDependencies: ["vuetify"]
};
