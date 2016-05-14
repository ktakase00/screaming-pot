package jp.co.uniquevision.screamingpot.receiver;

import java.util.Map;
import java.util.HashMap;

import jp.co.uniquevision.screamingpot.receiver.discovery.RemoteDeviceDiscovery;

/**
 * 湿度センサーから送信された検出値をBluetoothのシリアル通信で受信するアプリケーション
 *
 */
public class App {
	
	/**
	 * メイン関数
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		// 送信元サービス管理用マップ
		Map<String, SenderService> senderServiceMap = new HashMap<>();
		
		// Bluetooth端末の探索
		RemoteDeviceDiscovery discovery = new RemoteDeviceDiscovery(senderServiceMap);
		Thread discoveryThread = new Thread(discovery);
		discoveryThread.start();
		
		try {
			discoveryThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
