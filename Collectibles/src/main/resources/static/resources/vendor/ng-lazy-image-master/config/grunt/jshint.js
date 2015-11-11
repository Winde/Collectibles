module.exports = {
    files : ['src/*.js'],
    options : {
        jshintrc : '.jshintrc',
        jshintignore : '.jshintignore',
        reporter : require('jshint-junit-reporter'),
        reporterOutput : 'target/reports/jshint-result.xml'
    }
};