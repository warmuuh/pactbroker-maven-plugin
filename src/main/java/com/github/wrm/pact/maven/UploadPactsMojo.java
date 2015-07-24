package com.github.wrm.pact.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.wrm.pact.domain.PactFile;
import com.github.wrm.pact.repository.RepositoryProvider;

/**
 * Verifies all pacts that can be found for this provider
 */
@Mojo(name = "upload-pacts")
@Execute(phase = LifecyclePhase.TEST)
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
     * Location of pacts
     */
    @Parameter(defaultValue = "target/pacts")
    private String pacts;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File folder = new File(pacts);
        getLog().info("loading pacts from " + pacts);
        try {
            List<PactFile> pactList = readPacts(folder);
            RepositoryProvider provider = createRepositoryProvider(brokerUrl, consumerVersion);
            provider.uploadPacts(pactList);
        }
        catch (Exception e) {
            throw new MojoExecutionException("Failed to read pacts", e);
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