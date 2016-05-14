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
	 * @param completeListener 探索終了リスナー
	 */
	public DeviceDiscoveryListener(DiscoveryCompleteListener<RemoteDevice> completeListener) {
		this.completeListener = completeListener;
	}
	
	/**
	 * 端末発見
	 */
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
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
	 */
	@Override
	public void inquiryCompleted(int discType) {
		System.out.println("Device Inquiry completed!");
		
		if (null == this.completeListener) {
			return;
		}
		this.completeListener.onDiscoveryComplete(this.devicesDiscovered);
	}
	
	/**
	 * サービスの探索終了
	 */
	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * サービス発見
	 */
	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// TODO Auto-generated method stub
		
	}
}
