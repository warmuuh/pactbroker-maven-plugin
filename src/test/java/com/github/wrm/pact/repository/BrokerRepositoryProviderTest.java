package com.github.wrm.pact.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import au.com.dius.pact.consumer.ConsumerPactBuilder.PactDslWithProvider.PactDslWithState;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.model.PactFragment;

import com.github.wrm.pact.OpenPortProvider;
import com.github.wrm.pact.domain.PactFile;

public class BrokerRepositoryProviderTest {

    private static final String PROVIDER_NAME = "a_provider";
    private static final String CONSUMER_NAME = "a_consumer";
    private static final String CONSUMER_VERSION = "1.0.0";

    private int port = OpenPortProvider.getOpenPort();

    private String pactJson = "{ \"provider\": { \"name\": \"" + PROVIDER_NAME + "\" }, \"consumer\": { \"name\": \""
            + CONSUMER_NAME + "\" } }";
    private String pactPath = "/pacts/provider/" + PROVIDER_NAME + "/consumer/" + CONSUMER_NAME + "/version/"
            + CONSUMER_VERSION;
    private String pactLink = "http://localhost:" + port + pactPath;
    private String providerJson = "{ \"_links\": { \"pacts\": [ { \"href\": \"" + pactLink + "\" } ] }}";

    private PactFile pact;
    private BrokerRepositoryProvider brokerRepositoryProvider;

    @Before
    public void setup() throws Exception {
        File pactFile = temporaryFolder.newFile("some_pact.json");
        try (PrintWriter out = new PrintWriter(pactFile, StandardCharsets.UTF_8.displayName())) {
            out.write(pactJson);
        }
        pact = PactFile.readPactFile(pactFile);
        brokerRepositoryProvider = new BrokerRepositoryProvider("http://localhost:" + port, CONSUMER_VERSION,
                new SystemStreamLog());
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public PactRule rule = new PactRule("localhost", port, this);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @PactVerification("no-pacts-present")
    public void uploadPactToBroker() throws Exception {
        brokerRepositoryProvider.uploadPacts(Collections.singletonList(pact));
    }

    @Test
    @PactVerification("one-pact-present")
    public void downloadPactFromBroker_IsNotSupported() throws Exception {
        File pactFoder = temporaryFolder.newFolder();
        brokerRepositoryProvider.downloadPactsFromLinks(Collections.singletonList(pactLink), pactFoder);

        assertThat(
                new File(pactFoder.getAbsoluteFile() + "/" + CONSUMER_NAME + "-" + PROVIDER_NAME + ".json").exists(),
                is(true));
    }

    @Test
    @PactVerification("one-provider-pact-link-present")
    public void downloadProviderPactInformation() throws Exception {
        List<String> links = brokerRepositoryProvider.downloadPactLinks(PROVIDER_NAME);

        assertThat(links.size(), is(1));
        assertThat(links.get(0), is(pactLink));
    }

    @Pact(state = "no-pacts-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForUploading(PactDslWithState builder) {

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        return builder
                .uponReceiving("a pact file")
                .path("/pacts/provider/" + PROVIDER_NAME + "/consumer/" + CONSUMER_NAME + "/version/"
                        + CONSUMER_VERSION).body(pactJson).headers(requestHeaders).method("PUT").willRespondWith()
                .headers(responseHeaders).status(200).body(pactJson).toFragment();
    }

    @Pact(state = "one-provider-pact-link-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForDownloadingPactLinks(PactDslWithState builder) {

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        return builder.uponReceiving("a request for the latest provider pacts")
                .path("/pacts/provider/" + PROVIDER_NAME + "/latest").headers(requestHeaders).method("GET")
                .willRespondWith().headers(responseHeaders).status(200).body(providerJson).toFragment();
    }

    @Pact(state = "one-pact-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForDownloadingPact(PactDslWithState builder) {

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        return builder.uponReceiving("a request for the latest provider pacts").path(pactPath).headers(requestHeaders)
                .method("GET").willRespondWith().headers(responseHeaders).status(200).body(pactJson).toFragment();
    }

}
