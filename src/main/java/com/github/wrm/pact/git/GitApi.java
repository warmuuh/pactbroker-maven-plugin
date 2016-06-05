package com.github.wrm.pact.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;

public class GitApi {

	Git repository;
	CredentialsProvider credentialsProvider;
	
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
	
        public void initWithCredentials(File repoDir, String url, CredentialsProvider credentialsProvider) throws Exception {
                this.credentialsProvider = credentialsProvider;
                try{
                        repository = Git.open(repoDir);
                        repository.pull().setCredentialsProvider(this.credentialsProvider).call();
                } catch (IOException ex) {
                        //failed to open, so we clone it anew
                        repository = Git.cloneRepository().setCredentialsProvider(this.credentialsProvider).setDirectory(repoDir).setURI(url).call();
                }
        }
	
	
	/**
	 * adds, commits and pushes changes only, if there are actually changes
	 * @param message
	 * @return false, if there were no changes to be pushed
	 * @throws Exception
	 */
	public boolean pushChanges(String message) throws Exception {
		
		if (repository.diff().call().isEmpty()){
			return false;
		}
		
		repository.add().addFilepattern(".").call();
		repository.commit().setMessage(message).call();
		
		if(this.credentialsProvider!=null){
		    repository.push().setCredentialsProvider(this.credentialsProvider).call();
		}else{
		    repository.push().call();
		}
		return true;
	}
	
}
