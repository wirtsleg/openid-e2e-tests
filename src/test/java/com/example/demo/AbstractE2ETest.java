package com.example.demo;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Testcontainers
@SpringBootTest
public abstract class AbstractE2ETest {
	protected static final Network NETWORK = Network.newNetwork();

	@Container
	protected OidcProviderContainer provider = new OidcProviderContainer()
		.withUserFile("oidc-users.json")
		.withConfigFile("oidc-config.json")
		.withNetwork(NETWORK)
		.withNetworkAliases("provider");

	@Container
	protected BrowserWebDriverContainer<?> selenium = new BrowserWebDriverContainer<>(
			DockerImageName.parse("selenium/standalone-chrome:93.0-chromedriver-93.0-grid-4.0.0-rc-2-prerelease-20210916")
					.asCompatibleSubstituteFor(DockerImageName.parse("selenium/standalone-chrome"))
	)
			.withSharedMemorySize((long)2e+9)
		.withNetwork(NETWORK)
		.withCapabilities(
			new ChromeOptions()
				.setHeadless(true)
				.setPageLoadStrategy(PageLoadStrategy.EAGER)
				.addArguments("--whitelisted-ips")
				.addArguments("--no-sandbox")
				.addArguments("--disable-extensions")
				.addArguments("--disable-dev-shm-usage")
				.addArguments("--disable-gpu")
				.addArguments("--verbose")
		)
		.withStartupTimeout(Duration.of(60, SECONDS));

	@BeforeEach
	public void setup() {
		Configuration.driverManagerEnabled = true;
		Configuration.startMaximized = true;
		Configuration.baseUrl = "http://host.testcontainers.internal:8080";
		Configuration.reportsFolder = "./target/e2e/";
		Configuration.remote = selenium.getSeleniumAddress().toString();
		WebDriverRunner.setWebDriver(selenium.getWebDriver());
	}
}
