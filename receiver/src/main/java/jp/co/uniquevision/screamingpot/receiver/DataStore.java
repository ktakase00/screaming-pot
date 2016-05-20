package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.RandomAccessFile;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;

public class DataStore {
	private String friendlyName;
	private Object dataStoreEventObject;
	
	public DataStore(String friendlyName) {
		init(friendlyName);
	}
	
	private void init(String friendlyName) {
		this.friendlyName = friendlyName;
		this.dataStoreEventObject = new Object();
		
	}
	
	public void append(Humidity humidity) {
		synchronized(this.dataStoreEventObject) {
			String lastLine = getLastLine();
			System.out.println(String.format("[%s]", lastLine));
			
			long sequence = getSequence(lastLine);
			Humidity newItem = Humidity.cloneWithSequence(humidity, sequence + 1);
			
			appendSync(newItem);
		}
	}
	
	private void appendSync(Humidity humidity) {
		File file = new File(this.friendlyName);
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
			
			String line = String.format("%08d,%s,%f",
					humidity.getSequence(),
					Util.dateToString(humidity.getTime()),
					humidity.getDegree());
			
			writer.write(String.format("%s\n", line));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (null != writer) {
				try {
					writer.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getLastLine() {
		File file = new File(this.friendlyName);
		RandomAccessFile access = null;
		String line = "";
		
		try {
			access = new RandomAccessFile(file, "r");
			long pos = access.length() - 2;
			System.out.println(pos);
			boolean found = false;
			
			try {
				while (pos > 0 && !found) {
					access.seek(pos);
					int character = access.read();
					
					if (0x0a == character) {
						found = true;
					}
					else {
						pos -= 1;
					}
				}
				
				if (found) {
					pos += 1;
				}
				access.seek(pos);
				line = access.readLine();
			}
			catch (EOFException ex) {
				ex.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				if (null != access) {
					access.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return line;
	}
	
	private long getSequence(String line) {
		long sequence = 0;
		
		if (line.length() > 0) {
			String[] values = line.split(",");
			
			try {
				sequence = Long.valueOf(values[0]);
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return sequence;
	}
}
