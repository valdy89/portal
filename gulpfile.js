const gulp = require('gulp');
const nodemon = require('gulp-nodemon');

gulp.task('start', function () {
  var stream = nodemon({
    exec: 'ts-node',
    script: 'server.ts'
  });

  stream.on('restart', function () {
    console.log('Node restarted!')
  }).on('crash', function() {
    console.error('Application has crashed!\n')
    stream.emit('restart', 10)
  })
})
