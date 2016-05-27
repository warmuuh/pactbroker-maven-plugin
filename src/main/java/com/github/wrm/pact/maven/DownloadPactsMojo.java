package com.github.wrm.pact.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.util.StringUtils;

import com.github.wrm.pact.git.auth.BasicGitCredentialsProvider;
import com.github.wrm.pact.git.auth.GitAuthenticationProvider;
import com.github.wrm.pact.repository.RepositoryProvider;

/**
 * Verifies all pacts that can be found for this provider
 */
@Mojo(name = "download-pacts")
@Execute(phase = LifecyclePhase.GENERATE_TEST_RESOURCES)
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
     * Location of pacts
     */
    @Parameter(defaultValue = "target/pacts-dependents")
    private String pacts;

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
        RepositoryProvider repoProvider;
        try {
            if(!StringUtils.isEmptyOrNull(password)&& !StringUtils.isEmptyOrNull(username)){
                GitAuthenticationProvider credentialsProvider = new BasicGitCredentialsProvider();
                repoProvider = createAuthenticatdeRepositoryProvider(brokerUrl, consumerVersion, credentialsProvider.getCredentialProvider(username, password));
            }else{
                repoProvider = createRepositoryProvider(brokerUrl, consumerVersion);
            }
            repoProvider.downloadPacts(provider, tagName, new File(pacts));
        }
        catch (Throwable e) {
            throw new MojoExecutionException("Failed to download pacts", e);
        }
    }

}
