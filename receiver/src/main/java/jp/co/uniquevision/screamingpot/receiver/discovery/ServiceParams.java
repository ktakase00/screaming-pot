package jp.co.uniquevision.screamingpot.receiver.discovery;

/**
 * Bluetooth端末が提供しているサービスの情報
 *
 */
public class ServiceParams {
	private String url;
	private String serviceName;
	
	/**
	 * コンストラクタ
	 * @param url サービスのURL
	 * @param serviceName サービス名(SPPなど)
	 */
	public ServiceParams(String url, String serviceName) {
		this.url = url;
		this.serviceName = serviceName;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public String getServiceName() {
		return this.serviceName;
	}
}
