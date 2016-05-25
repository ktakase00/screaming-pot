package jp.co.uniquevision.screamingpot.receiver;

import java.util.Map;
import java.util.HashMap;

/**
 * 湿度センサーから送信された検出値をBluetoothのシリアル通信で受信するアプリケーション
 *
 */
public class App 
{
	/**
	 * メイン関数
	 * @param args コマンドライン引数
	 */
    public static void main(String[] args)
    {
		// 送信元サービス管理用マップ
		Map<String, Receiver> receiverMap = new HashMap<>();
		
		// Bluetooth端末の探索スレッド開始
		Thread discoveryThread = DeviceDiscovery.start(receiverMap);
		
		// データの転送スレッド開始
		Thread transporterThread = Transporter.start(receiverMap);
		
		try {
			// 終了するのを待つ
			transporterThread.join();
			discoveryThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
