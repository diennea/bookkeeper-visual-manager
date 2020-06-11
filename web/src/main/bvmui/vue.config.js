const DEV_MODE = process.env.NODE_ENV !== "production";

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
  publicPath: DEV_MODE ? "/" : "",
  configureWebpack: {
    performance: {
      hints: false
    }
  },
  transpileDependencies: ["vuetify"]
};
