package jp.co.uniquevision.screamingpot.receiver.discovery;

import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.*;

/**
 * Bluetooth端末の探索中に発生するイベントを処理するリスナー
 *
 */
public class DeviceDiscoveryListener implements DiscoveryListener {
	
	private final Vector<RemoteDevice> devicesDiscovered = new Vector<>();
	private DiscoveryCompleteListener<RemoteDevice> completeListener;
	
	/**
	 * コンストラクタ
	 * 
	 * @param completeListener 探索終了リスナー
	 */
	public DeviceDiscoveryListener(DiscoveryCompleteListener<RemoteDevice> completeListener) {
		this.completeListener = completeListener;
	}
	
	/**
	 * 端末発見
	 * 
	 * @param btDevice 発見した端末
	 * @param cod わからん
	 */
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
		
		// リストに追加
		this.devicesDiscovered.addElement(btDevice);
        
		try {
			System.out.println("     name " + btDevice.getFriendlyName(false));
		}
		catch (IOException cantGetDeviceName) {
//			cantGetDeviceName.printStackTrace();
			System.out.println("     " + cantGetDeviceName.getLocalizedMessage());
		}
	}
	
	/**
	 * 端末の探索終了
	 * 
	 * @param discType わからん
	 */
	@Override
	public void inquiryCompleted(int discType) {
		System.out.println("Device Inquiry completed!");
		
		if (null == this.completeListener) {
			return;
		}
		
		// 探索終了を通知
		this.completeListener.onDiscoveryComplete(this.devicesDiscovered);
	}
	
	/**
	 * サービスの探索終了
	 * 
	 * @param transID わからん
	 * @param respCode わからん
	 */
	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * サービス発見
	 * 
	 * @param transID わからん
	 * @param servRecord わからん
	 */
	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// TODO Auto-generated method stub
		
	}
}
