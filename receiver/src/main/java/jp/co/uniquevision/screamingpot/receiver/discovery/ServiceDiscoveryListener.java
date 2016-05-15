package jp.co.uniquevision.screamingpot.receiver.discovery;

import java.util.Vector;

import javax.bluetooth.*;

/**
 * Bluetooth端末が提供しているサービスの探索中に発生するイベントを処理するリスナー
 *
 */
public class ServiceDiscoveryListener implements DiscoveryListener {

	private DiscoveryCompleteListener<ServiceParams> completeListener;
	private final Vector<ServiceParams> servicesFound = new Vector<>();
	
	/**
	 * コンストラクタ
	 * @param completeListener 探索終了リスナー
	 */
	public ServiceDiscoveryListener(DiscoveryCompleteListener<ServiceParams> completeListener) {
		this.completeListener = completeListener;
	}
	
	/**
	 * 端末発見
	 */
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 端末の探索終了
	 */
	@Override
	public void inquiryCompleted(int discType) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * サービスの探索終了
	 */
	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		System.out.println("service search completed! " + transID + ", " + respCode);
		if (null == this.completeListener) {
			return;
		}
		this.completeListener.onDiscoveryComplete(this.servicesFound);
	}

	/**
	 * サービス発見
	 */
	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i < servRecord.length; i++) {
			// サービスのURLを取得
			String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			if (null == url) {
				continue;
			}
			
			// サービス名を取得
			DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
			String nameValue = "";
			
			if (null != serviceName) {
				nameValue = serviceName.getValue().toString();
				System.out.println("service " + nameValue + " found " + url);
			}
			else {
				System.out.println("service found " + url);
			}
			
			ServiceParams service = new ServiceParams(url, nameValue);
            
			// サービスを記憶
			this.servicesFound.addElement(service);
		}
	}
}
