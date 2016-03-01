package ca.corefacility.bioinformatics.irida.web.controller.test.listeners;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Global settings for all integration tests.
 * 
 */
public class IntegrationTestListener extends RunListener {
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTestListener.class);
	public static final int DRIVER_TIMEOUT_IN_SECONDS = 3;

	private static WebDriver driver;

	/**
	 * {@inheritDoc}
	 */
	public void testRunStarted(Description description) throws Exception {

		if (isRunningUITests()) {
			driver = new ChromeDriver();
			driver.manage().window().setSize(new Dimension(1400, 900));
			driver.manage().timeouts().implicitlyWait(DRIVER_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
		}

		logger.debug("Setting up RestAssured.");
		RestAssured.requestContentType(ContentType.JSON);
		RestAssured.port = Integer.valueOf(System.getProperty("jetty.port"));
	}

	/**
	 * For starting chrome only when running individual tests,
	 * running the ui_testing profile, or running all_testing
	 *
	 * Essentially preventing ChromeDriver from running when it
	 * isn't required for the tests.
	 *
	 * i.e. Black list profiles for when we don't want chrome to run
	 *	
	 */
	public boolean isRunningUITests() {
		try {
			File file = new File("src/test/resources/active-profile.txt");

			final Scanner scanner = new Scanner(file);

			ArrayList<String> blacklist = new ArrayList<>();
			//if adding any new profiles to black list, add them here!
			blacklist.add("service_testing");
			blacklist.add("rest_testing");
			blacklist.add("galaxy_testing");

			while (scanner.hasNext()) {
				String[] line = scanner.nextLine().split("\\s+");
				for (String str : line) {
					if (blacklist.contains(str)) {
						logger.debug("Profile blacklisted: not running ChromeDriver.");
						return false;
					}
				}
			}
		}
		catch (Exception e) {
			//If the file doesn't exist or we have problems reading the file, we don't want to stop
			// execution just for this, since we're just checking whether or not to run ChromeDriver.
			// So, by default, if there's an error we'll just continue with ChromeDriver set to run.
			logger.debug("ERROR: " + e.toString());
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void testRunFinished(Result result) throws Exception {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * Get a reference to the {@link WebDriver} used in the tests.
	 * @return the instance of {@link WebDriver} used in the tests.
	 */
	public static WebDriver driver() {
		return driver;
	}
}