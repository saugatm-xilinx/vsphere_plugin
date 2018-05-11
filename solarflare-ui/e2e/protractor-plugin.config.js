// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/docs/referenceConf.js

const login = require('./helpers/login');
const logger = require('./helpers/logger');
const util = require('./helpers/util');

/*global jasmine */
const { SpecReporter } = require('jasmine-spec-reporter');

exports.config = {
   framework: 'jasmine',
   allScriptsTimeout: 11000,
   specs: [
      './plugin/**/*.e2e-spec.ts'
   ],
   capabilities: {
      'browserName': 'chrome'
   },
   directConnect: true,

   baseUrl: util.getBaseUrl(),

   // The params object will be passed directly to the Protractor instance,
   // and can be accessed from your test as browser.params. It is an arbitrary
   // object and can contain anything you may need in your test.
   // This can be changed via the command line as:
   //   --params.login.user "Joe"
   params: {
      login: {
         username: 'administrator@vsphere.local',
         password: 'Admin!23'
      }
   },
   jasmineNodeOpts: {
      showColors: true,
      defaultTimeoutInterval: 30000,
      print: function() {}
   },
   beforeLaunch: function() {
      require('ts-node').register({
         project: 'e2e/tsconfig.e2e.json'
         // Use this other path when debugging Protractor in IntelliJ
         // project: 'tsconfig.e2e.json'
      });
   },
   onPrepare() {
      return login.start()
            .then(util.initSession())
            .then(() => {
               logger.start("vSphere Client logged in", arguments);
            });
   }
};
