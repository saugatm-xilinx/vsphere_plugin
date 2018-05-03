'use strict';

module.exports =  {
   start(msg) {
      console.log('\n\x1b[1m\x1b[35m ** %s\x1b[1m\x1b[0m\x1b[0m', msg);
   },
   done(msg) {
      console.log('\x1b[33m || %s\x1b[0m\n', msg);
   },
   action(msg) {
      console.log('\x1b[36m   -> action: \x1b[33m%s\x1b[0m', msg);
   },
   info(msg) {
      console.log('\x1b[36m   %s\x1b[1m\x1b[0m\x1b[0m', msg);
   }
};