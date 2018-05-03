'use strict';

const logger = require('./logger');

const selectors = {
   username() {
      return element(by.id('username'));
   },
   password() {
      return element(by.id('password'));
   },
   loginButton() {
      return element(by.id('submit'));
   }
};

const start = (username, password) => {
   browser.ignoreSynchronization = true;
   var EC = protractor.ExpectedConditions;

   return browser.get(browser.baseUrl)
      .then(() => {
         logger.info('Waiting for login input to be clickable');
         return browser.wait(EC.elementToBeClickable(element(by.id('username')), 5000));
      })
      .then(() => {
         logger.action('Clearing username field');
         return selectors.username().clear();
      })
      .then(() => {
         logger.action('Clearing password field');
         return selectors.password().clear();
      })
      .then(() => {
         logger.action('Setting username field');
         return selectors.username().sendKeys(username || browser.params.login.username);
      })
      .then(() => {
         logger.action('Setting password field');
         return selectors.password().sendKeys(password || browser.params.login.password);
      })
      .then(() => {
         logger.info('Waiting for login button to be clickable');
         return browser.wait(EC.elementToBeClickable(selectors.loginButton(), 5000));
      })
      .then(() => {
         logger.action('Clicking login button');
         return selectors.loginButton().click();
      })
      .then(() => {
         logger.info('Waiting for page to load');
         return browser.wait(EC.presenceOf(element(by.id('homeBurgerMenu')), 5000));
      })
      .then(() => {
         logger.done("Logged in");
      })
      .catch(err => {
         console.error('Error encountered during login', err);
      });
}

module.exports = {
   selectors,
   start
};