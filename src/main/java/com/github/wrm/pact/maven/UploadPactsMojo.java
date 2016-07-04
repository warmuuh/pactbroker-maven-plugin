package com.github.wrm.pact.maven;

import com.github.wrm.pact.domain.PactFile;
import com.github.wrm.pact.repository.RepositoryProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Verifies all consumerPacts that can be found for this provider
 */
@Mojo(name = "upload-pacts")
@Execute(phase = LifecyclePhase.NONE)
public class UploadPactsMojo extends AbstractPactsMojo {

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

    /**
     * Location of consumerPacts
     */
    @Parameter(defaultValue = "${consumerPacts.rootDir}")
    private String consumerPacts;

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

    /**
     * Tag name to tag the consumer pact version with
     */
    @Parameter
    private String tagName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (consumerPacts != null && !consumerPacts.equals("${consumerPacts.rootDir}")) {

            File folder = new File(consumerPacts);

            if (!folder.exists()) {
                getLog().warn(String.format("consumer pact folder '%s' does not exist", consumerPacts));
                return;
            }

            getLog().info("loading consumerPacts from " + consumerPacts);

            try {
                List<PactFile> pactList = readPacts(folder);
                RepositoryProvider provider = createRepositoryProvider(brokerUrl, consumerVersion, Optional.ofNullable(username), Optional.ofNullable(password));
                provider.uploadPacts(pactList, tagName);
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to read consumerPacts", e);
            }
        } else {
            getLog().info("<<<<<<<<<<< Skip upload pacts! consumerPacts configuration is not provided...");
        }

    }

    private List<PactFile> readPacts(File folder) throws FileNotFoundException {
        List<PactFile> pacts = new LinkedList<PactFile>();
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            String fileName = file.getName();
            if (fileName.endsWith("json")) {
                PactFile pactFile = PactFile.readPactFile(file);
                pacts.add(pactFile);
                getLog().info("found pact file: " + fileName);
            }
        }
        return pacts;
    }

}