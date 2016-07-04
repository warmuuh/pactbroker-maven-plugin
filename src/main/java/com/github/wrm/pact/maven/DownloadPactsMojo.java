package com.github.wrm.pact.maven;

import com.github.wrm.pact.repository.RepositoryProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Optional;

/**
 * Verifies all pats that can be found for this provider
 */
@Mojo(name = "download-pacts")
@Execute(phase = LifecyclePhase.NONE)
public class DownloadPactsMojo extends AbstractPactsMojo {

    /**
     * url of pact broker
     */
    @Parameter
    private String brokerUrl;

    /**
     * Consumer version
     */
    @Parameter(defaultValue = "1.0.0")
    private String consumerVersion;

    @Parameter
    private String tagName;
    /**
     * Location of providerPacts
     */
    @Parameter(defaultValue = "${providerPacts.rootDir}")
    private String providerPacts;

    /**
     * Name of this provider
     */
    @Parameter
    private String provider;

    /**
     * username of git repository
     */
    @Parameter
    private String username;

    /**
     * password of git repository
     */
    @Parameter
    private String password;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (providerPacts != null && !providerPacts.equals("${providerPacts.rootDir}")) {

            try {
                RepositoryProvider repoProvider = createRepositoryProvider(brokerUrl, consumerVersion, Optional.ofNullable(username), Optional.ofNullable(password));
                repoProvider.downloadPacts(provider, tagName, new File(providerPacts));
            } catch (Throwable e) {
                throw new MojoExecutionException("Failed to download providerPacts", e);
            }
        } else {
            getLog().info("<<<<<<<<<<< Skip download! providerPacts configuration is not provided...");
        }
    }

}
