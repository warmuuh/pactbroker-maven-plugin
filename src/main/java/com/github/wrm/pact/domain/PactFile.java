package com.github.wrm.pact.domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * holding informations about a pactFile
 * @author pmucha
 *
 */
public class PactFile {

	final String path;
	final String consumer;
	final String provider;
	
	private PactFile (String path, String consumer, String provider){
		this.path = path;
		this.consumer = consumer;
		this.provider = provider;
	};
	
	/**
	 * reads the given file end extracts pact-details
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public static PactFile readPactFile(File file) throws FileNotFoundException{
		FileReader reader = new FileReader(file);
		JsonElement jelement = new JsonParser().parse(reader);
		
		String provider = jelement.getAsJsonObject().get("provider").getAsJsonObject().get("name").getAsString();
		String consumer = jelement.getAsJsonObject().get("consumer").getAsJsonObject().get("name").getAsString();
		return new PactFile(file.getAbsolutePath(), consumer, provider);
	}

	public String getPath() {
		return path;
	}

	public String getConsumer() {
		return consumer;
	}

	public String getProvider() {
		return provider;
	}
	
	
	
	
	
	
}
