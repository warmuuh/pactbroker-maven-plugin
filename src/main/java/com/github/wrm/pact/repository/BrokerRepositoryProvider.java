package com.github.wrm.pact.repository;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.github.wrm.pact.domain.PactFile;

public class BrokerRepositoryProvider implements RepositoryProvider {

    private final String url;
    private final String consumerVersion;
    private final Log log;

    public BrokerRepositoryProvider(String url, String consumerVersion, Log log) {
        this.url = url;
        this.consumerVersion = consumerVersion;
        this.log = log;
    }

    @Override
    public void uploadPacts(List<PactFile> pacts) throws Exception {
        for (PactFile pact : pacts) {
            uploadPact(pact);
        }
    }

    @Override
    public void downloadPacts(String providerId, File targetDirectory) throws Exception {
        throw new UnsupportedOperationException();
    }

    private void uploadPact(PactFile pact) throws MalformedURLException, IOException, ProtocolException {
        String path = buildUploadPath(pact);

        log.info("Uploading pact to " + path);

        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.displayName());

        byte[] content = Files.readAllBytes(Paths.get(pact.getPath()));
        connection.getOutputStream().write(content);

        if (connection.getResponseCode() != 200) {
            log.error("Uploading failed. Pact Broker answered with: " + connection.getContent());
        }

        connection.disconnect();
    }

    private String buildUploadPath(PactFile pact) {
        return url + "/pacts/provider/" + pact.getProvider() + "/consumer/" + pact.getConsumer() + "/version/"
                + consumerVersion;
    }
}
