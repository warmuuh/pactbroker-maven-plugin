package com.github.wrm.pact.repository;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
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

import com.github.wrm.pact.domain.PactFile;

public class BrokerRepositoryProviderTest {

    private static final String PROVIDER_NAME = "a_provider";
    private static final String CONSUMER_NAME = "a_consumer";
    private static final String CONSUMER_VERSION = "1.0.0";
    private static final String PACT_JSON = "{ \"provider\": { \"name\": \"" + PROVIDER_NAME
            + "\" }, \"consumer\": { \"name\": \"" + CONSUMER_NAME + "\" } }";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public PactRule rule = new PactRule("localhost", 8089, this);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PactFile pact;
    private BrokerRepositoryProvider brokerRepositoryProvider;

    @Before
    public void setup() throws Exception {
        File pactFile = temporaryFolder.newFile("some_pact.json");
        try (PrintWriter out = new PrintWriter(pactFile, StandardCharsets.UTF_8.displayName())) {
            out.write(PACT_JSON);
        }
        pact = PactFile.readPactFile(pactFile);
        brokerRepositoryProvider = new BrokerRepositoryProvider("http://localhost:8089", CONSUMER_VERSION,
                new SystemStreamLog());
    }

    @Test
    @PactVerification("no-pacts-present")
    public void uploadPactToBroker() throws Exception {
        brokerRepositoryProvider.uploadPacts(Collections.singletonList(pact));
    }

    @Test
    public void downloadPactFromBroker_IsNotSupported() throws Exception {
        thrown.expect(UnsupportedOperationException.class);
        brokerRepositoryProvider.downloadPacts(PROVIDER_NAME, temporaryFolder.newFolder());
    }

    @Pact(state = "no-pacts-present", provider = "broker-maven-plugin", consumer = "pact-broker")
    public PactFragment createFragment(PactDslWithState builder) {

        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", "application/json");

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        return builder
                .uponReceiving("an existing userId")
                .path("/pacts/provider/" + PROVIDER_NAME + "/consumer/" + CONSUMER_NAME + "/version/"
                        + CONSUMER_VERSION).body(PACT_JSON).headers(requestHeaders).method("PUT").willRespondWith()
                .headers(responseHeaders).status(200).body(PACT_JSON).toFragment();
    }

}
