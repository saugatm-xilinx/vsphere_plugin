import { browser, element, by } from 'protractor';

const logger = require('../helpers/logger');

export class VsphereClient {
   navigateToShortcuts() {
      logger.action("Navigating to shortcuts page");
      return browser.get(browser.baseUrl + "?extensionId=vsphere.core.controlcenter.domainView#?" +
            "extensionId=vsphere.core.controlcenter.domainView");
   }
}
export class UiPluginPage {
   navigateTo() {
      return browser.get('/');
   }

   getHeaderTitle() {
      return element(by.css('my-app header .branding')).getText();
   }

   getTabFirstParagraph() {
      return element.all(by.css('clr-tab-content p')).first().getText();
   }

   getContentFirstParagraph() {
      return element.all(by.css('.content-area p')).first().getText();
   }

   getButtonByText(text) {
      return element(by.buttonText(text));
   }

   getLinkByText(text) {
      return element(by.linkText(text));
   }

   getOpenModalElement() {
      return element(by.css('.modal-dialog'));
   }

   getOpenModalTitle() {
      return element(by.css('.modal-dialog .modal-title')).getText();
   }

   getFirstBtnLink() {
      return element.all(by.css('clr-tab-content .btn-link')).first();
   }

}
