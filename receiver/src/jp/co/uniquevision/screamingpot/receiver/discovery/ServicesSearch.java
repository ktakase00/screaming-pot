package jp.co.uniquevision.screamingpot.receiver.discovery;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;

/**
 * Bluetooth端末が提供しているサービスを探索する
 *
 */
public class ServicesSearch {
	
	static final UUID OBEX_SERIAL_PORT = new UUID(0x1101);

	private RemoteDevice btDevice;
	private UUID serviceUUID;
	private Object serviceSearchCompletedEvent;
	private Vector<ServiceParams> servicesFound;
	
	/**
	 * コンストラクタ
	 * @param btDevice サービスを探索する対象のBluetooth端末
	 * @param serviceUUID 探索するサービスのUUID
	 */
	public ServicesSearch(RemoteDevice btDevice, UUID serviceUUID) {
		init(btDevice, serviceUUID);
	}
	
	/**
	 * コンストラクタ(シリアル通信サービス探索用)
	 * @param btDevice サービスを探索する対象のBluetooth端末
	 */
	public ServicesSearch(RemoteDevice btDevice) {
		init(btDevice, OBEX_SERIAL_PORT);
	}
	
	/**
	 * 初期化処理
	 * @param btDevice サービスを探索する対象のBluetooth端末
	 * @param serviceUUID 探索するサービスのUUID
	 */
	private void init(RemoteDevice btDevice, UUID serviceUUID) {
		this.btDevice = btDevice;
		this.serviceUUID = serviceUUID;
	}
	
	/**
	 * Bluetooth端末のサービスを探索
	 * @throws InterruptedException
	 */
	public void find() throws InterruptedException {
		// 探索が終了するまで待機するためのイベントオブジェクト
		this.serviceSearchCompletedEvent = new Object();
		
		// 探索の終了を受け取るリスナー
		DiscoveryCompleteListener<ServiceParams> completeListener =
				new DiscoveryCompleteListener<ServiceParams>() {
			
			@Override
			public void onDiscoveryComplete(Vector<ServiceParams> discovered) {
				ServicesSearch.this.onServiceDiscoveryComplete(discovered);
			}
		};

		// 探索中に発生する各イベントを処理するリスナー
		DiscoveryListener listener = new ServiceDiscoveryListener(completeListener);

		UUID[] searchUuidSet = new UUID[] {
				this.serviceUUID
		};
		int[] attrIDs =  new int[] {
				0x0100 // Service name
		};

		synchronized(this.serviceSearchCompletedEvent) {
			try {
				System.out.println("search services on " + btDevice.getBluetoothAddress() + " " + btDevice.getFriendlyName(false));
				
				// 探索の実行
				LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
				
				// 待機する
				this.serviceSearchCompletedEvent.wait();
			}
			catch (IOException e) {
//				e.printStackTrace();
				System.out.println(e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
				
				// 空の配列をセットする
				setServicesFound(new Vector<ServiceParams>());
			}
		}
	}
	
	/**
	 * 発見したサービスの一覧を返却する
	 * @return 発見したサービスの一覧
	 */
	public Vector<ServiceParams> getServicesFound() {
		return this.servicesFound;
	}
	
	/**
	 * 端末の探索終了時の処理
	 * @param discovered 見つかったサービスの一覧
	 */
	private void onServiceDiscoveryComplete(Vector<ServiceParams> discovered) {
		setServicesFound(discovered);
		
		// 待機を終了させる
		synchronized(this.serviceSearchCompletedEvent) {
			this.serviceSearchCompletedEvent.notifyAll();
		}
	}
	
	/**
	 * 探索結果を記憶する
	 * @param discovered サービスのVector配列
	 */
	private void setServicesFound(Vector<ServiceParams> discovered) {
		System.out.println(discovered.size() +  " service(s) found");
		this.servicesFound = discovered;
	}
}
