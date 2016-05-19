package jp.co.uniquevision.screamingpot.receiver;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;
import junit.framework.TestCase;

public class DataStoreTest extends TestCase {
	public void testSave() {
		Humidity humidity = Humidity.newInstance(100.0);
		DataStore store = new DataStore("sample_data_store");
		store.append(humidity);
		
		assertTrue(true);
	}
}
