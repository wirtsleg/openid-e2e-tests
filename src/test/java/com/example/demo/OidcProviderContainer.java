package com.example.demo;

import org.apache.commons.lang3.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

public class OidcProviderContainer extends GenericContainer<OidcProviderContainer> {
    public static final int PORT = 9000;

    private String usersFile;
    private String cfgFile;

    public OidcProviderContainer() {
        this("qlik/simple-oidc-provider:0.2.5");
    }

    public OidcProviderContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));

        withExposedPorts(PORT);

        setWaitStrategy(Wait
            .forHttp("/.well-known/openid-configuration")
            .forPort(PORT)
            .withStartupTimeout(Duration.ofSeconds(30))
        );
    }

    public OidcProviderContainer withUserFile(String classpathName) {
        this.usersFile = classpathName;

        return this;
    }

    public OidcProviderContainer withConfigFile(String classpathName) {
        this.cfgFile = classpathName;

        return this;
    }

    @Override protected void configure() {
        if (StringUtils.isNotEmpty(usersFile)) {
            String usersFileInContainer = "/tmp/" + usersFile;
            withCopyFileToContainer(MountableFile.forClasspathResource(usersFile), usersFileInContainer);
            withEnv("USERS_FILE", usersFileInContainer);
        }

        if (StringUtils.isNotEmpty(cfgFile)) {
            String cfgFileInContainer = "/tmp/" + cfgFile;
            withCopyFileToContainer(MountableFile.forClasspathResource(cfgFile), cfgFileInContainer);
            withEnv("CONFIG_FILE", cfgFileInContainer);
        }
    }
}
