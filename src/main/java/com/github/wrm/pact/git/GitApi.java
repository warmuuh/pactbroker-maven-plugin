package com.github.wrm.pact.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;

public class GitApi {

	Git repository;
	
	public GitApi() {
		
	}
	
	public void init(File repoDir, String url) throws Exception {
		try{
			repository = Git.open(repoDir);
			repository.pull().call();
		} catch (IOException ex) {
			//failed to open, so we clone it anew
			repository = Git.cloneRepository().setDirectory(repoDir).setURI(url).call();
		}
	}
	
	
	/**
	 * adds, commits and pushes changes only, if there are actually changes
	 * @param repository
	 * @return false, if there were no changes to be pushed
	 * @throws Exception
	 */
	public boolean pushChanges(String message) throws Exception {
		
		if (repository.diff().call().isEmpty()){
			return false;
		}
		
		repository.add().addFilepattern(".").call();
		repository.commit().setMessage(message).call();
		repository.push().call();
		return true;
	}
	
}
