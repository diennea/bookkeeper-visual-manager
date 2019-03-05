const { task, src, dest, series, watch, parallel } = require('gulp');

const less = require('gulp-less');
const cleanCSS = require('gulp-clean-css');

const clean = require('gulp-clean');
const rename = require('gulp-rename');
const plumber = require('gulp-plumber');

const header = require('gulp-header');
const licenseheader = require('./licenseheader');
const browsersync = require('browser-sync').create();

const { path } = require('./configuration');
const source = path.src;
const dist = path.dist;

task('javascript', function () {
    return src(source.js + '/**/*.js')
        .pipe(dest(dist.js))
        .pipe(header(licenseheader))
        .pipe(rename({ suffix: '.min' }))
        .pipe(dest(dist.css))
        .pipe(browsersync.stream());
});

task('less', function () {
    return src(source.less + '/*.less')
        .pipe(plumber())
        .pipe(less())
        .pipe(header(licenseheader))
        .pipe(rename({ basename: 'main' }))
        .pipe(dest(dist.css))
        .pipe(browsersync.stream())
        .pipe(rename({ basename: 'main', suffix: '.min' }))
        .pipe(cleanCSS())
        .pipe(header(licenseheader))
        .pipe(dest(dist.css))
        .pipe(browsersync.stream());
});

task('html', function () {
    return src(source.folder + '/*.html')
        .pipe(dest(dist.folder));
});

task('clean', function () {
    return src(dist.folder + '/**/*', { read: false })
        .pipe(clean({ force: true }));
});

task('browsersync', function (callback) {
    browsersync.init({
      server: {
        baseDir: './',
        index: dist.folder + '/index.html'
      }
    });
    callback();
});

task('reload', function (callback) {
    browsersync.reload();
    callback();
});

task('watch', function () {
    watch(source.js + '/**/*.js', series('javascript'));
    watch(source.less + '/**/*.less', series('less'));
    watch(source.folder + '/*.html', series('html', 'reload'));
});

task('dist', series('clean',
    parallel('html', 'javascript', 'less')
));

task('dev', series('dist',
    parallel('browsersync', 'watch')
));

task('default', series('dist'));
