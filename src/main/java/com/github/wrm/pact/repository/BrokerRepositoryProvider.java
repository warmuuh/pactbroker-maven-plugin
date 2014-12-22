package com.github.wrm.pact.repository;

import java.io.File;
import java.util.List;

import com.github.wrm.pact.domain.PactFile;

public class BrokerRepositoryProvider implements RepositoryProvider {

	@Override
	public void uploadPacts(List<PactFile> pacts) throws Exception {
		//TODO
	}

	@Override
	public void downloadPacts(String providerId, File targetDirectory) throws Exception {
		//TODO		
	}
	
	
//	
//	private void uploadPact(String consumer, String provider, File file) throws Throwable {
//		String request = pactBroker + "/pact/provider/" + provider + "/consumer/" + consumer;
//		getLog().info("uploading to " + request);
//		URL url = new URL(request); 
//		HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
//		connection.setDoOutput(true);
//		connection.setDoInput(true);
////		connection.setInstanceFollowRedirects(false); 
//		connection.setRequestMethod("POST"); 
//		connection.setRequestProperty("Content-Type", "application/json"); 
//		connection.setRequestProperty("Accept", "application/json");
//		connection.setRequestProperty("charset", "utf-8");
////		connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
////		connection.setUseCaches (false);
//
////		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//		OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
//		String content = readFile(file, Charset.defaultCharset());
//		System.out.println(content);
//		wr.write(content);
//		wr.flush();
//		System.out.println(connection.getResponseMessage());
//		wr.close();
//		connection.disconnect();
//	}
//
//	static String readFile(File file, Charset encoding) throws IOException {
//		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
//		return new String(encoded, encoding);
//	}

	

//	private void downloadPacts() throws Throwable {
//		String request = pactBroker + "/pact/provider/" + provider;
//
//		FileUtils.deleteDirectory(new File(pacts + "/"));
//		getLog().info("downloading from " + request);
//		URL url = new URL(request); 
//		String content = readStream(url.openStream());
//		JsonElement jelement = new JsonParser().parse(content);
//		JsonArray arr = jelement.getAsJsonArray();
//		for(int i = 0; i < arr.size(); ++i){
//			String provider = arr.get(i).getAsJsonObject().get("provider").getAsJsonObject().get("name").getAsString();
//			String consumer = arr.get(i).getAsJsonObject().get("consumer").getAsJsonObject().get("name").getAsString();
//			
//			
//			new File(pacts + "/").mkdirs();
//			File targetFile = new File(pacts + "/" + consumer + "-" + provider + ".json");
//			getLog().info("writing to " + targetFile);
////			targetFile..mkdirs();
//			FileWriter writer = new FileWriter(targetFile);
//			writer.write(arr.get(i).toString());
//			writer.close();
//		}
//		
//	}
//
//	static String readStream(InputStream stream) throws IOException {
//		return IOUtils.toString(stream);
//	}
}
