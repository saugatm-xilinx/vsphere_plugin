const logger = require('./logger');
const request = require('request');

// Base url for a local vSphere Client
const BASE_URL = "https://localhost:9443/ui/";

const DEFAULT_WAIT_TIME_MILLISECONDS = 4000;

module.exports = {
   addCookieToRequest: addCookieToRequest,
   addSessionToRequest: addSessionToRequest,
   getBaseUrl: getBaseUrl,
   initSession: initSession,
   waitForPresenceOf: waitForPresenceOf,
   waitForElementToBeClickable: waitForElementToBeClickable,
   waitForAndClick: waitForAndClick
};

function getBaseUrl() {
   return BASE_URL;
}

function addCookieToRequest() {
   return browser.driver.manage().getCookies().then(function(cookies) {
      var xsrfToken;
      var cookieJar = request.jar();

      for (var i in cookies) {
         var cookie = request.cookie(cookies[i].name + '=' + cookies[i].value);
         cookieJar.setCookie(cookie, browser.baseUrl);
         if (cookies[i].name === 'VSPHERE-UI-XSRF-TOKEN') {
            xsrfToken = cookies[i].value;
         }
      }

      return request.defaults({
         rejectUnauthorized: false,
         jar: cookieJar,
         headers: { 'X-VSPHERE-UI-XSRF-TOKEN': xsrfToken }
      });
   });
}

function addSessionToRequest(request) {
   var deferred = protractor.promise.defer();
   request.get(browser.baseUrl + 'usersession', function(error, response, body) {
      return deferred.fulfill(request.defaults({
         qs: { webClientSessionId: JSON.parse(body).clientId }
      }));
   });
   return deferred.promise;
}

function initSession() {
   return addCookieToRequest()
               .then(addSessionToRequest);
}

function waitForAndClick (element, element_name) {
   return waitForElementToBeClickable(element).then(function() {
      return browser.actions().click(element).perform().then(function() {
         retrieveElementName(element, element_name).then(function(element_name) {
            logger.action("Clicking '" + element_name + "'");
         });
      });
   });
}

function waitForElementToBeClickable (element) {
   var EC = protractor.ExpectedConditions;
   return browser.wait(EC.elementToBeClickable(element), DEFAULT_WAIT_TIME_MILLISECONDS);
}

function waitForPresenceOf (element) {
   var EC = protractor.ExpectedConditions;
   return browser.wait(EC.presenceOf(element), DEFAULT_WAIT_TIME_MILLISECONDS);
}

function retrieveElementName (element, element_name) {
   var defer = protractor.promise.defer();

   if (element_name === undefined) {
      defer.fulfill(element.getText());
   } else {
      defer.fulfill(element_name);
   }

   return defer;
}

