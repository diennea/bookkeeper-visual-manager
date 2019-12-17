const DEV_MODE = process.env.NODE_ENV !== "production";

const devServer = "http://localhost:8086";

module.exports = {
    devServer: {
        disableHostCheck: true,
        proxy: {
            "/api": {
                target: devServer,
                secure: false
            }
        }
    }
}