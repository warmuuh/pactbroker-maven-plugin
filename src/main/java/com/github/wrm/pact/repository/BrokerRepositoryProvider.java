package com.github.wrm.pact.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.apache.maven.plugin.logging.Log;

import com.github.wrm.pact.domain.PactFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString;

public class BrokerRepositoryProvider implements RepositoryProvider {

    private final String url;
    private final String consumerVersion;
    private final Log log;
    private final Optional<String> username;
    private final Optional<String> password;

    public BrokerRepositoryProvider(String url,
                                    String consumerVersion,
                                    Log log,
                                    Optional<String> username,
                                    Optional<String> password) {
        this.url = url;
        this.consumerVersion = consumerVersion;
        this.log = log;
        this.username = username;
        this.password = password;
    }

    @Override
    public void uploadPacts(final List<PactFile> pacts, final Optional<String> tagName) throws Exception {
        for (PactFile pact : pacts) {
            uploadPact(pact);
            if(tagName.isPresent()) {
                tagPactVersion(pact, tagName.get());
            }
        }
    }


    @Override
    public void downloadPacts(String providerId, String tagName, File targetDirectory) throws Exception {
        downloadPactsFromLinks(downloadPactLinks(providerId, tagName), targetDirectory);
    }

    public void downloadPactsFromLinks(List<String> links, File targetDirectory) throws IOException {
        targetDirectory.mkdirs();

        for (String link : links) {
            downloadPactFromLink(targetDirectory, link);
        }
    }

    public List<String> downloadPactLinks(String providerId, String tagName) throws IOException {
        String path = buildDownloadLinksPath(providerId, tagName);

        log.info("Downloading pact links from " + path);

        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        addBasicAuthTo(connection);

        List<String> links = new ArrayList<>();

        if (connection.getResponseCode() != 200) {
            log.error("Downloading pact links failed. Pact Broker answered with status: " + connection.getResponseCode()
                    + " and message: " + connection.getResponseMessage());
            return links;
        }

        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.displayName())) {
            JsonElement jelement = new JsonParser().parse(scanner.useDelimiter("\\A").next());

            JsonArray asJsonArray = jelement.getAsJsonObject().get("_links").getAsJsonObject().get("pacts")
                    .getAsJsonArray();

            asJsonArray.forEach(element -> {
                links.add(element.getAsJsonObject().get("href").getAsString());
            });
        }

        return links;
    }

    private void uploadPact(PactFile pact) throws IOException {
        String path = buildUploadPath(pact);

        log.info("Uploading pact to " + path);

        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.displayName());
        addBasicAuthTo(connection);

        byte[] content = Files.readAllBytes(Paths.get(pact.getFile().getAbsolutePath()));
        connection.getOutputStream().write(content);

        if (connection.getResponseCode() > 201) {
            try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.displayName())) {
                log.error("Uploading failed. Pact Broker answered with: " + scanner.useDelimiter("\\A").next());
            }
        }

        connection.disconnect();
    }

    private void tagPactVersion(PactFile pact, String tagName) throws IOException {
        String path = buildTaggingPath(pact, tagName);

        log.info("Tagging pact version with path: " + path);

        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.displayName());
        addBasicAuthTo(connection);

        if (connection.getResponseCode() > 201) {
            try (Scanner scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8.displayName())) {
                log.error("Tagging pact version failed. Pact Broker answered with: " + scanner.useDelimiter("\\A").next());
            }
        }

        connection.disconnect();
    }

    private void downloadPactFromLink(File targetDirectory, String link) throws MalformedURLException, IOException,
            FileNotFoundException {
        URL url = new URL(link);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        addBasicAuthTo(connection);

        if (connection.getResponseCode() != 200) {
            log.error("Downloading pact failed. Pact Broker answered with status: " + connection.getResponseCode()
                    + " and message: " + connection.getResponseMessage());
            return;
        }

        log.info("Downloading pact from " + link);

        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.displayName())) {
            String pact = scanner.useDelimiter("\\A").next();

            JsonElement jelement = new JsonParser().parse(pact);

            String provider = jelement.getAsJsonObject().get("provider").getAsJsonObject().get("name").getAsString();
            String consumer = jelement.getAsJsonObject().get("consumer").getAsJsonObject().get("name").getAsString();

            String pactFileName = targetDirectory.getAbsolutePath() + "/" + consumer + "-" + provider + ".json";

            try (PrintWriter printWriter = new PrintWriter(pactFileName)) {
                printWriter.write(pact);
                log.info("Writing pact file to " + pactFileName);
            }
        }
    }

    private void addBasicAuthTo(HttpURLConnection connection)
    {
        if (username.isPresent() && password.isPresent()) {
            String userpass = username.get() + ":" + password.get();
            String basicAuth = "Basic " + encodeBase64URLSafeString(userpass.getBytes());
            connection.setRequestProperty ("Authorization", basicAuth);
        }
    }

    private String buildUploadPath(PactFile pact) {
        return url + "/pacts/provider/" + pact.getProvider() + "/consumer/" + pact.getConsumer() + "/version/"
                + consumerVersion;
    }

    /*http://pact-broker/pacticipants/Zoo%20App/versions/1.0.0/tags/prod*/

    private String buildTaggingPath(PactFile pact, String tagName) {
        return url + "/pacticipants/" + pact.getConsumer() + "/versions/" + consumerVersion + "/tags/" + tagName;
    }

    private String buildDownloadLinksPath(String providerId, String tagName) {
        String downloadUrl = url + "/pacts/provider/" + providerId + "/latest";
        if(tagName != null && !tagName.isEmpty()) {
            return downloadUrl + "/" + tagName;
        }
        else
            return downloadUrl;
    }
}
