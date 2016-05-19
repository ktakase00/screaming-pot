package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;

/**
 * Bluetoothと通信を行う
 *
 */
public class Receiver implements Runnable {
	
	private String deviceAddress;
	private String friendlyName;
	private String url;
	private String serviceName;
	private DataStore dataStore;
	
	/**
	 * コンストラクタ
	 * @param deviceAddress Bluetooth端末のアドレス
	 * @param friendlyName Bluetooth端末の名前
	 * @param url サービスのURL
	 * @param serviceName サービス名
	 */
	public Receiver(String deviceAddress,
			String friendlyName,
			String url,
			String serviceName) {
		
		this.deviceAddress = deviceAddress;
		this.friendlyName = friendlyName;
		this.url = url;
		this.serviceName = serviceName;
		this.dataStore = new DataStore(friendlyName);
	}
	
	/**
	 * 通信スレッドを開始する
	 */
	public void start() {
		Thread serviceThread = new Thread(this);
		serviceThread.start();
	}

	/**
	 * 通信スレッドの処理内容
	 */
	@Override
	public void run() {
		String threadName = this.deviceAddress + " " + this.friendlyName;
		System.out.println("service thread start: " + threadName);
		
		try {
			// 通信実行
			communicate();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("service thread stopped: " + threadName);
	}
	
	/**
	 * 通信実行
	 * @throws InterruptedException
	 */
	private void communicate() throws InterruptedException {
		StreamConnection connection = null;
		InputStream inStream = null;
		
		try {
			System.out.println("Connecting to " + this.url);

			// 接続
			connection = (StreamConnection)Connector.open(this.url);
			
			// ストリームを開く
			inStream = connection.openInputStream();
			
			// 受信ループ
			receive(inStream);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// 接続を閉じる
			if (null != inStream) {
				try {
					inStream.close();
				}
				catch (IOException e) {
					// 何もしない
				}
			}
			
			if (null != connection) {
				try {
					connection.close();
				}
				catch (IOException e) {
					// 何もしない
				}
			}
		}
	}
	
	/**
	 * 受信ループ
	 * @param inStream 入力ストリーム
	 */
	private void receive(InputStream inStream) {
		boolean activeFlag = true;
		BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
		
		while (activeFlag) {
			try {
				// read string from spp client
				String lineRead = bReader.readLine();
				
				System.out.println("received: " + lineRead);
				
				double degree = Double.valueOf(lineRead);
				Humidity humidity = Humidity.newInstance(degree);
				
				this.dataStore.append(humidity);
			}
			catch (IOException e) {
				e.printStackTrace();
				
				// 通信エラーが発生したら接続を閉じる
				activeFlag = false;
			}
		}
	}
}
