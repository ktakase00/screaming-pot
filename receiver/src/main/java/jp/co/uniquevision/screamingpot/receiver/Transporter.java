package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.Gson;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;

public class Transporter implements Runnable {

	public static final MediaType JSON
		= MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient client;

	public Transporter() {
		this.client = new OkHttpClient();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			gather();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void gather() throws InterruptedException {
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
