package com.github.wrm.pact.repository;

import java.io.File;
import java.util.List;

import com.github.wrm.pact.domain.PactFile;

public interface RepositoryProvider {

	/**
	 * the provider uploads all given pacts to the given url.
	 * @param pacts
	 * @throws Exception
	 */
	void uploadPacts(List<PactFile> pacts) throws Exception;

	/**
	 * the provider uploads all given pacts to the given url and tags the uploaded version.
	 * @param pacts
	 * @param snapshot
	 * @throws Exception
	 */
	public void uploadPacts(List<PactFile> pacts, String snapshot) throws Exception;
	
	
	/**
	 * the provider downloads all necessary pacts to the given repository
	 * @param providerId
	 * @param targetDirectory
	 * @throws Exception
	 */
	public void downloadPacts(String providerId, String tagName, File targetDirectory) throws Exception;

}
