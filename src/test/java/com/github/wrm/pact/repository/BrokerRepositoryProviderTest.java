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
    @PactVerification("pact-already-uploaded")
    public void uploadExistingPactToBroker() throws Exception {
        brokerRepositoryProvider.uploadPacts(Collections.singletonList(pact));
    }

    @Test
    @PactVerification("one-pact-present")
    public void downloadPactFromBroker() throws Exception {
        File pactFoder = new File(temporaryFolder.newFolder() + "/target/pacts-dependents");

        brokerRepositoryProvider.downloadPactsFromLinks(Collections.singletonList(pactLink), pactFoder);

        assertThat(
                new File(pactFoder.getAbsoluteFile() + "/" + CONSUMER_NAME + "-" + PROVIDER_NAME + ".json").exists(),
                is(true));
    }

    @Test
    @PactVerification("one-provider-pact-link-present")
    public void downloadProviderPactInformation() throws Exception {
        List<String> links = brokerRepositoryProvider.downloadPactLinks(PROVIDER_NAME, null);

        assertThat(links.size(), is(1));
        assertThat(links.get(0), is(pactLink));
    }

    @Test
    @PactVerification("one-prod-provider-pact-link-present")
    public void downloadProviderPactInformationForProdTag() throws Exception {
        List<String> links = brokerRepositoryProvider.downloadPactLinks(PROVIDER_NAME, "prod");

        assertThat(links.size(), is(1));
        assertThat(links.get(0), is(pactLink));
    }

    @Pact(state = "no-pacts-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForUploading(PactDslWithState builder) {

        return builder
                .uponReceiving("a pact file")
                .path("/pacts/provider/" + PROVIDER_NAME + "/consumer/" + CONSUMER_NAME + "/version/"
                        + CONSUMER_VERSION).body(pactJson).headers(getHeaders()).method("PUT").willRespondWith()
                .headers(getHeaders()).status(201).body(pactJson).toFragment();
    }

    @Pact(state = "pact-already-uploaded", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForUploadingPact(PactDslWithState builder) {

        return builder.uponReceiving("an already existing pact file").path(pactPath).headers(getHeaders())
                .method("PUT").willRespondWith().headers(getHeaders()).status(200).body(pactJson).toFragment();
    }

    @Pact(state = "one-provider-pact-link-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForDownloadingPactLinks(PactDslWithState builder) {

        return builder.uponReceiving("a request for the latest provider pacts")
                .path("/pacts/provider/" + PROVIDER_NAME + "/latest").headers(getHeaders()).method("GET")
                .willRespondWith().headers(getHeaders()).status(200).body(providerJson).toFragment();
    }

    @Pact(state = "one-prod-provider-pact-link-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForDownloadingPactLinksForProdTag(PactDslWithState builder) {

        return builder.uponReceiving("a request for the latest provider pacts for the prod tag")
                .path("/pacts/provider/" + PROVIDER_NAME + "/latest/prod").headers(getHeaders()).method("GET")
                .willRespondWith().headers(getHeaders()).status(200).body(providerJson).toFragment();
    }

    @Pact(state = "one-pact-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragmentForDownloadingPact(PactDslWithState builder) {

        return builder.uponReceiving("a request for the latest provider pacts").path(pactPath).headers(getHeaders())
                .method("GET").willRespondWith().headers(getHeaders()).status(200).body(pactJson).toFragment();
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
