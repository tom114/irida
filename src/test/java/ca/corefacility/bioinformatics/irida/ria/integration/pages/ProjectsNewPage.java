package ca.corefacility.bioinformatics.irida.ria.integration.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Strings;

/**
 * <p> Page Object to represent the projects-new page used to create a new project. </p>
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
public class ProjectsNewPage extends AbstractPage {
	public static final String PROJECT_NEW_URL = BASE_URL + "projects/new";

	public ProjectsNewPage(WebDriver driver) {
		super(driver);
	}

	public void goToPage() {
		driver.get(PROJECT_NEW_URL);
	}

	public void submitForm(String name, String organism, String wiki, String description) {
		driver.findElement(By.name("name")).sendKeys(name);
		if (!Strings.isNullOrEmpty(organism)) {
			WebElement organismField = driver.findElement(By.cssSelector("a.select2-choice"));
			organismField.click();
			WebElement sdf = driver.findElement(By.name("organism"));
			sdf.sendKeys(organism);
			sdf.sendKeys(Keys.RETURN);
		}
		waitForTime(700);
		driver.findElement(By.name("remoteURL")).sendKeys(wiki);
		driver.findElement(By.name("projectDescription")).sendKeys(description);
	}

	public void clickSubmit() {
		driver.findElement(By.id("submitBtn")).click();
	}

	public void setName(String name) {
		WebElement nameField = driver.findElement(By.name("name"));
		nameField.sendKeys(name);
		nameField.sendKeys(Keys.TAB);
		waitForTime(700);
	}

	public boolean isErrorNameRequiredDisplayed() {
		return driver.findElements(By.id("error-name-required")).size() == 1;
	}

	public boolean isErrorNameServerDisplayed() {
		return driver.findElements(By.id("error-name-server")).size() == 1;
	}

	public boolean isErrorUrlDisplayed() {
		return driver.findElements(By.id("error-url")).size() == 1;
	}

	public boolean formHasErrors() {
		WebDriverWait wait = new WebDriverWait(driver, 500);
		if (driver.findElements(By.className("errors-default")).size() > 0) {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("errors-default")));
		}
		try {
			driver.findElements(By.className("errors-default"));
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	public void setRemoteURL(String url) {
		WebElement urlField = driver.findElement(By.name("remoteURL"));
		urlField.clear();
		urlField.sendKeys(url);
		urlField.sendKeys(Keys.TAB);
		waitForTime(700);
	}
}
