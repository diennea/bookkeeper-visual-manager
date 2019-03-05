module.exports = {
    path: {
        src: {
            folder: './src',
            less: './src/less',
            js: './src/js'
        },
        dist: {
            folder: './dist',
            css: './dist/css',
            js: './dist/js'
        }
    },
    production: function () {
        return process.env.NODE_ENV === 'production';
    },
    development: function () {
        return process.env.NODE_ENV === 'development';
    }
} 
