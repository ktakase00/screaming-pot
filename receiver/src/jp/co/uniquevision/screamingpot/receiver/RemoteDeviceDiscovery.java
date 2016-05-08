package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.*;

public class RemoteDeviceDiscovery implements Runnable {
	
	private final Vector<RemoteDevice> devicesDiscovered = new Vector<>();

	@Override
	public void run() {
		try {
			while (true) {
				search();
				Thread.sleep(1000);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void search() throws InterruptedException {

        final Object inquiryCompletedEvent = new Object();

        this.devicesDiscovered.clear();
        DiscoveryListener listener = new SpDiscoveryListener(this, inquiryCompletedEvent);

        synchronized(inquiryCompletedEvent) {
    		try {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                
                if (started) {
                    System.out.println("wait for device inquiry to complete...");
                    inquiryCompletedEvent.wait();
                    System.out.println(devicesDiscovered.size() +  " device(s) found");
                }
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
        }
	}
	
	public void addElement(RemoteDevice btDevice) {
		this.devicesDiscovered.addElement(btDevice);
	}

}
