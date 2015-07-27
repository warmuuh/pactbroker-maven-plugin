package com.github.wrm.pact.maven;

import org.apache.maven.plugin.AbstractMojo;

import com.github.wrm.pact.repository.BrokerRepositoryProvider;
import com.github.wrm.pact.repository.GitRepositoryProvider;
import com.github.wrm.pact.repository.RepositoryProvider;

public abstract class AbstractPactsMojo extends AbstractMojo {

    /**
     * returns an implementation of RepositorProvider based on given url
     *
     * @param url
     * @return
     */
    protected RepositoryProvider createRepositoryProvider(String url, String consumerVersion) {
        if (url.endsWith(".git"))
            return new GitRepositoryProvider(url, getLog());

        return new BrokerRepositoryProvider(url, consumerVersion, getLog());
    }

}
