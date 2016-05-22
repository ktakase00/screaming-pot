package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;
import jp.co.uniquevision.screamingpot.receiver.models.EsMetaCreate;
import jp.co.uniquevision.screamingpot.receiver.models.EsSourceCreate;

public class Transporter implements Runnable {

	public static final MediaType JSON
		= MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient client;
	private Map<String, Receiver> receiverMap;

	public Transporter(Map<String, Receiver> receiverMap) {
		this.receiverMap = receiverMap;
		this.client = new OkHttpClient();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean loop = true;
		
		while (loop) {
			try {
				gather();
				Thread.sleep(5000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				loop = false;
			}
		}
	}

	private void gather() throws InterruptedException {
		/*
		Humidity humidity = Humidity.newInstance(100.0);
		Gson gson = new Gson();
		String json = gson.toJson(humidity);
		System.out.println(json);
		String url = "http://127.0.0.1:8080/elasticsearch/screamingpot/humidity";
		
		try {
			post(url, json);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		*/
		System.out.println(String.format("transporter: %d", this.receiverMap.size()));
		List<Humidity> listAll = new ArrayList<Humidity>();
		Map<String, Long> sequenceMap = new HashMap<String, Long>();
		
		for (Map.Entry<String, Receiver> ent : this.receiverMap.entrySet()) {
			Receiver receiver = ent.getValue();
			DataStore dataStore = receiver.getDataStore();
			List<Humidity> list = dataStore.readDataStore();
			listAll.addAll(list);
			
			if (0 < list.size()) {
				sequenceMap.put(dataStore.getDevice(),
						list.get(list.size()-1).getSequence());
			}
		}
		
		String bulk = makeBulk(listAll);
		System.out.println(bulk);
		
		for (Map.Entry<String, Receiver> ent : this.receiverMap.entrySet()) {
			Receiver receiver = ent.getValue();
			DataStore dataStore = receiver.getDataStore();
			String deviceName = dataStore.getDevice();
			
			if (sequenceMap.containsKey(deviceName)) {
				dataStore.cleanup(sequenceMap.get(deviceName));
			}
		}
	}
	
	private String makeBulk(List<Humidity> list) {
		StringBuilder builder = new StringBuilder();
		Gson gson = new Gson();
		
		for (Humidity humidity : list) {
			EsMetaCreate meta = new EsMetaCreate("screamongpot", "humidity", null);
			EsSourceCreate source = new EsSourceCreate(humidity.getDevice(),
					humidity.getTime(),
					humidity.getDegree());
			String metaJson = gson.toJson(meta);
			String sourceJson = gson.toJson(source);
			builder.append((String.format("%s\n%s\n", metaJson, sourceJson)));
		}
		return builder.toString();
	}
	
	String post(String url, String json) throws IOException {
		RequestBody body = RequestBody.create(JSON, json);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		Response response = client.newCall(request).execute();
		return response.body().string();
	}
}
