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
		
		// Bluetooth端末の探索
		DeviceDiscovery discovery = new DeviceDiscovery(receiverMap);
		Thread discoveryThread = new Thread(discovery);
		discoveryThread.start();
		
		Transporter transporter = new Transporter(receiverMap);
		Thread transporterThread = new Thread(transporter);
		transporterThread.start();
		
		try {
			discoveryThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
