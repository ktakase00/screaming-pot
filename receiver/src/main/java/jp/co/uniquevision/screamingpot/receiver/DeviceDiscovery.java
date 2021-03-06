package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.bluetooth.*;

import jp.co.uniquevision.screamingpot.receiver.discovery.DeviceDiscoveryListener;
import jp.co.uniquevision.screamingpot.receiver.discovery.DiscoveryCompleteListener;
import jp.co.uniquevision.screamingpot.receiver.discovery.ServiceParams;
import jp.co.uniquevision.screamingpot.receiver.discovery.ServicesSearch;

/**
 * Bluetooth端末を探索する
 *
 */
public class DeviceDiscovery implements Runnable {
	
	public static final long DISCOVERY_PERIOD_MSEC = 10000L;

	private Map<String, Receiver> receiverMap;
	private Map<String, Thread> threadMap;
	private Vector<RemoteDevice> devicesDiscovered;
	private Object inquiryCompletedEvent;
	
	/**
	 * コンストラクタ
	 * 
	 * @param receiverMap レシーバーのマップ
	 */
	public DeviceDiscovery(Map<String, Receiver> receiverMap) {
		this.receiverMap = receiverMap;
		this.threadMap = new HashMap<String, Thread>();
	}
    
	/**
	 * スレッドを開始する
	 * 
	 * @param receiverMap レシーバーのマップ
	 * @return スレッドオブジェクト
	 */
	public static Thread start(Map<String, Receiver> receiverMap) {
		DeviceDiscovery discovery = new DeviceDiscovery(receiverMap);
		Thread discoveryThread = new Thread(discovery);
		discoveryThread.start();
		
		return discoveryThread;
	}
	
	/**
	 * スレッドの処理内容
	 */
	@Override
	public void run() {
		boolean loop = true;
				
		try {
			while (loop) {
				// Bluetoothデバイスが存在するかどうか探索する
				discoverDevices();
				
				// 停止しているレシーバーをマップから削除する
				cleanupReceiverMap();
				
				// サービスが存在するかどうか探索する
				discoverServices();
				
				Thread.sleep(DISCOVERY_PERIOD_MSEC);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			loop = false;
		}
	}
	
	/**
	 * Bluetoothデバイスが存在するかどうか探索する
	 * 
	 * @throws InterruptedException
	 */
	private void discoverDevices() throws InterruptedException {
		// 前回の探索結果を破棄する
		if (null != this.devicesDiscovered) {
			this.devicesDiscovered.clear();
			this.devicesDiscovered = null;
		}
		
		// 探索が終了するまで待機するためのイベントオブジェクト
		this.inquiryCompletedEvent = new Object();
		
		// 探索の終了を受け取るリスナー
		final DiscoveryCompleteListener<RemoteDevice> completeListener = new DiscoveryCompleteListener<RemoteDevice>() {
			@Override
			public void onDiscoveryComplete(Vector<RemoteDevice> discovered) {
				DeviceDiscovery.this.onDeviceDiscoveryComplete(discovered);
			}
		};
		// 探索中に発生する各イベントを処理するリスナー
		final DeviceDiscoveryListener listener = new DeviceDiscoveryListener(completeListener);
		
		synchronized(this.inquiryCompletedEvent) {
			try {
				// 探索の実行
				boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
				
				// 待機する
				if (started) {
					System.out.println("wait for device inquiry to complete...");
					this.inquiryCompletedEvent.wait();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * サービスが存在するかどうか探索する
	 * 
	 * @throws InterruptedException
	 */
	private void discoverServices() throws InterruptedException {
		if (null == this.devicesDiscovered || null == this.receiverMap) {
			return;
		}
		
		// 見つかったすべての端末について繰り返し
		for (RemoteDevice btDevice : this.devicesDiscovered) {
			// サービスの探索を実行
			ServicesSearch servicesSearch = new ServicesSearch(btDevice);
			servicesSearch.find();
			
			// 送信元サービスをリストに追加する
			for (ServiceParams params : servicesSearch.getServicesFound()) {
				addReceiver(btDevice, params);
			}
		}
	}
	
	/**
	 * レシーバーをマップに追加する
	 * 
	 * @param btDevice Bluetooth端末
	 * @param params サービスの情報
	 */
	private void addReceiver(RemoteDevice btDevice, ServiceParams params) {
		try {
			String address = btDevice.getBluetoothAddress();
			String friendlyName = btDevice.getFriendlyName(false);
			if (friendlyName.isEmpty()) {
				friendlyName = address;
			}
			
			// すでに追加されていれば何もしない
			if (this.receiverMap.containsKey(friendlyName)) {
				System.out.println(getClass().getSimpleName() + ": receiver already exists: " + friendlyName);
				return;
			}
			
			// サービスのURL
			String url = params.getUrl();
			
			// 送信元サービスを生成
			Receiver service = new Receiver(address,
					friendlyName,
					url,
					params.getServiceName());
			
			this.receiverMap.put(friendlyName, service);
			
			// 通信開始
			Thread receiverThread = service.start();
			
			// スレッドをマップで記憶
			this.threadMap.put(friendlyName, receiverThread);
			
			System.out.println(getClass().getSimpleName() + ": receiver added: " + friendlyName);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 端末の探索終了時の処理
	 * 
	 * @param devicesDiscovered 見つかった端末の一覧
	 */
	private void onDeviceDiscoveryComplete(Vector<RemoteDevice> discovered) {
		System.out.println(discovered.size() +  " device(s) found");
		this.devicesDiscovered = discovered;
		
		// 待機を終了させる
		synchronized(this.inquiryCompletedEvent) {
			this.inquiryCompletedEvent.notifyAll();
		}
	}
	
	/**
	 * 停止しているレシーバーをマップから削除する
	 */
	private void cleanupReceiverMap() {
		List<String> stoppedList = new ArrayList<String>();
		
		// レシーバーの数だけ繰り返し
		for (Map.Entry<String, Thread> entry : this.threadMap.entrySet()) {
			String friendlyName = entry.getKey();
			Thread thread = entry.getValue();
			
			// レシーバーのスレッドが存在するかどうかを確認
			if (!thread.isAlive()) {
				// 削除対象とする
				stoppedList.add(friendlyName);
			}
		}
		
		// 削除対象のレシーバーの数だけ繰り返し
		for (String friendlyName : stoppedList) {
			// レシーバーのマップとスレッドのマップから削除する
			this.receiverMap.remove(friendlyName);
			this.threadMap.remove(friendlyName);
			
			System.out.println(getClass().getSimpleName() + ": removed receiver: " + friendlyName);
		}
	}
}
