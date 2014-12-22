package com.github.wrm.pact.maven;
import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.wrm.pact.repository.RepositoryProvider;

/**
 * Verifies all pacts that can be found for this provider
 *
 * @goal download-pacts
 * 
 * @phase generate-test-resources
 */
public class DownloadPactsMojo extends AbstractPactsMojo{

	/**
	 * url of pact broker
	 * 
	 * @parameter
	 */
	private String brokerUrl;

	/**
	 * Location of pacts
	 * 
	 * @parameter expression="target/pacts-dependents"
	 */
	private String pacts;


	/**
	 * Name of this provider
	 * 
	 * @parameter 
	 */
	private String provider;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			RepositoryProvider repoProvider = createRepositoryProvider(brokerUrl);
			repoProvider.downloadPacts(provider, new File(pacts));

		} catch (Throwable e) {
			throw new MojoExecutionException("Failed to download pacts", e);
		}
	}

	

	
}