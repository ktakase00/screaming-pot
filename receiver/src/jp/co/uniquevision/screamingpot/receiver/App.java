package jp.co.uniquevision.screamingpot.receiver;

public class App {
	public static void main(String[] args) {
		RemoteDeviceDiscovery discovery = new RemoteDeviceDiscovery();
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
