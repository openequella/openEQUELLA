package com.tle.webtests.framework;

import com.tle.webtests.pageobject.ExpectedConditions2;
import java.net.URL;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CommandPayload;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

/**
 * A {@link RemoteWebDriver} that normalises Chrome/ChromeDriver's "-32000" inspector error ("Node
 * with given id does not belong to the document") into a standard {@link
 * StaleElementReferenceException}.
 *
 * <p>Chrome sometimes surfaces a detached DOM node as a plain {@link WebDriverException} carrying
 * that message rather than a {@link StaleElementReferenceException} (see <a
 * href="https://github.com/SeleniumHQ/selenium/issues/15401">Selenium issue #15401</a>). The error
 * response is decoded into a value by the command executor and only <em>thrown</em> here, in {@link
 * RemoteWebDriver#execute(CommandPayload)} — the layer every element command, condition and wait
 * ultimately routes through. Translating it here means all downstream code that already tolerates
 * {@link StaleElementReferenceException} handles the Chrome variant too, without any per-call-site
 * special casing.
 */
public class StaleNodeTranslatingRemoteWebDriver extends RemoteWebDriver {

  public StaleNodeTranslatingRemoteWebDriver(URL remoteAddress, Capabilities capabilities) {
    super(remoteAddress, capabilities);
  }

  /**
   * No-arg constructor required so {@link org.openqa.selenium.remote.Augmenter} can build a proxy
   * subclass (it instantiates via the no-arg constructor and copies session state across),
   * mirroring {@link RemoteWebDriver#RemoteWebDriver()}.
   */
  protected StaleNodeTranslatingRemoteWebDriver() {
    super();
  }

  @Override
  protected Response execute(CommandPayload payload) {
    try {
      return super.execute(payload);
    } catch (WebDriverException e) {
      throw translate(e);
    }
  }

  /**
   * Returns a {@link StaleElementReferenceException} when {@code e} is Chrome's "-32000" stale-node
   * error, otherwise returns {@code e} unchanged. Exposed for unit testing.
   */
  public static WebDriverException translate(WebDriverException e) {
    if (ExpectedConditions2.isChromeStaleNodeException(e)) {
      return new StaleElementReferenceException(e.getMessage(), e);
    }
    return e;
  }
}
