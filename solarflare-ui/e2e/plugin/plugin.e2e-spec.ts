import { browser, element, by } from "protractor";
import { VsphereClient } from "./plugin.po";
const logger = require("../helpers/logger");

// End to end tests in plugin mode.
// - Your local vSphere client must be already running with the plugin deployed
// - Start the tests with this command:
//    ng e2e --config e2e/protractor-plugin.config.js --serve false

describe('Tests in plugin mode', () => {
   let client: VsphereClient;

   beforeEach(() => {
      client = new VsphereClient();
   });

   it('finds solarflare shortcut and opens its main view', () => {
      client.navigateToShortcuts().then(() => {
         // Find the div for "solarflare" shortcut
         const pluginShortcut = element(by.xpath('//div[text()="solarflare"]'));
         pluginShortcut.getText().then(name => {
            logger.info("Found shortcut: " + name);
            expect(name).toBe("solarflare");
         });

         browser.actions().click(pluginShortcut).perform().then(() => {
            logger.action("Click solarflare shortcut");
            browser.sleep(4000);

            // Check the view title
            const viewTitle = element(by.css(".titlebar"));
            viewTitle.getText().then(title => {
               logger.info("Found view title: " + title);
               expect(title).toContain("solarflare Main View");
            });

            // Check the iframe content
            const iframe = element(by.tagName("iframe")).getWebElement();
            browser.switchTo().frame(iframe);

            const mainView = element(by.tagName("main-view"));
            mainView.getText().then(viewText => {
               logger.info("Found mainView: " + viewText);
               expect(viewText).toContain("Home");
               expect(viewText).toContain("Echo Service");
               expect(viewText).toContain("Chassis List");
            });
         });
      });
   });
});
