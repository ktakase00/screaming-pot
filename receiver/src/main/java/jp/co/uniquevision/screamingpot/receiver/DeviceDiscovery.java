package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
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

	private Map<String, Receiver> senderServiceMap;
	private Vector<RemoteDevice> devicesDiscovered;
	private Object inquiryCompletedEvent;
	
	public DeviceDiscovery(Map<String, Receiver> senderServiceMap) {
		this.senderServiceMap = senderServiceMap;
	}
    
	@Override
	public void run() {
		try {
			while (true) {
				discoverDevices();
				discoverServices();
				
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
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
	 * @throws InterruptedException
	 */
	private void discoverServices() throws InterruptedException {
		if (null == this.devicesDiscovered || null == this.senderServiceMap) {
			return;
		}
		
		// 見つかったすべての端末について繰り返し
		for (RemoteDevice btDevice : this.devicesDiscovered) {
			// サービスの探索を実行
			ServicesSearch servicesSearch = new ServicesSearch(btDevice);
			servicesSearch.find();
			
			// 送信元サービスをリストに追加する
			for (ServiceParams params : servicesSearch.getServicesFound()) {
				addSenderService(btDevice, params);
			}
		}
	}
	
	/**
	 * 送信元サービスをリストに追加する
	 * @param btDevice Bluetooth端末
	 * @param params サービスの情報
	 */
	private void addSenderService(RemoteDevice btDevice, ServiceParams params) {
		// サービスのURL
		String url = params.getUrl();
		
		// すでに追加されていれば何もしない
		if (this.senderServiceMap.containsKey(url)) {
			return;
		}
		
		try {
			// 送信元サービスを生成
			Receiver service = new Receiver(btDevice.getBluetoothAddress(),
					btDevice.getFriendlyName(false),
					url,
					params.getServiceName());
			
			this.senderServiceMap.put(url, service);
			
			// 通信開始
			service.start();
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
}