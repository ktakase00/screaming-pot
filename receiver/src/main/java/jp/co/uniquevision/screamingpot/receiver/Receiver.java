package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;

/**
 * Bluetoothと通信を行う
 *
 */
public class Receiver implements Runnable {
	
	public static final int RETRY_MAX = 1;
	public static final long RETRY_INTERVAL_MSEC = 5000;
	
	private String deviceAddress;
	private String friendlyName;
	private String url;
	private String serviceName;
	private DataStore dataStore;
	private int retryCount;
	private StreamConnection connection;
	
	/**
	 * コンストラクタ
	 * 
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
	public Thread start() {
		Thread serviceThread = new Thread(this);
		serviceThread.start();
		
		return serviceThread;
	}

	/**
	 * 通信スレッドの処理内容
	 */
	@Override
	public void run() {
		String threadName = this.deviceAddress + " " + this.friendlyName;
		System.out.println(getClass().getSimpleName() + ": thread start: " + threadName);
		
		this.retryCount = 0;
		this.connection = null;
		InputStream inStream = null;
		
		try {
			// 接続
			inStream = openStream();
			
			// 受信ループ
			receive(inStream);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			// 接続を閉じる
			closeStream(inStream);
			closeConnection(this.connection);
		}
		
		System.out.println(getClass().getSimpleName() + ": thread stopped: " + threadName);
	}
	
	/**
	 * 接続
	 * 
	 * @return Bluetoothからの入力ストリーム
	 * @throws InterruptedException
	 */
	private InputStream openStream() throws InterruptedException {
		InputStream inStream = null;
		boolean loop = true;
		
		System.out.println("Connecting to " + this.url);

		while (loop) {
			try {
				this.connection = null;
				inStream = null;
				
				// 接続
				this.connection = (StreamConnection)Connector.open(this.url);
				
				// ストリームを開く
				inStream = this.connection.openInputStream();
				
				// 成功したらループを終了
				loop = false;
			}
			catch (IOException e) {
				e.printStackTrace();
				
				// 接続を閉じる
				closeStream(inStream);
				closeConnection(this.connection);
				
				// 接続リトライ回数の確認
				this.retryCount++;
				loop = RETRY_MAX >= this.retryCount;
				
				// リトライまで少し待つ
				if (loop) {
					Thread.sleep(RETRY_INTERVAL_MSEC);
				}
			}
		}
		
		return inStream;
	}
	
	/**
	 * 受信ループ
	 * 
	 * @param inStream 入力ストリーム
	 */
	private void receive(InputStream inStream) {
		if (null == inStream) {
			return;
		}
		
		boolean loop = true;
		BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
		
		while (loop) {
			try {
				// read string from spp client
				String lineRead = bReader.readLine();
				
				System.out.println(getClass().getSimpleName() + ": received: " + lineRead);
				
				if (null == lineRead) {
					continue;
				}
				
				double degree = Double.valueOf(lineRead);
				Humidity humidity = Humidity.newInstance(this.friendlyName, degree);
				
				this.dataStore.append(humidity);
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
				
				// 通信エラーが発生したら接続を閉じる
				loop = false;
			}
		}
	}
	
	/**
	 * データストアを返す
	 * 
	 * @return データストア
	 */
	public DataStore getDataStore() {
		return this.dataStore;
	}
	
	/**
	 * ストリームを閉じる
	 * 
	 * @param stream 対象のストリーム
	 */
	private void closeStream(Closeable stream) {
		if (null == stream) {
			return;
		}
		
		try {
			stream.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接続を閉じる
	 * 
	 * @param connection 対象の接続
	 */
	private void closeConnection(Connection connection) {
		if (null == connection) {
			return;
		}
		
		try {
			connection.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
