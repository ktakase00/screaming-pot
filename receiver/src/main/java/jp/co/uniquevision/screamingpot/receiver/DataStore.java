package jp.co.uniquevision.screamingpot.receiver;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Closeable;
import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.co.uniquevision.screamingpot.receiver.models.Humidity;

public class DataStore {
	private String device;
	private Object dataStoreEventObject;
	
	public DataStore(String device) {
		init(device);
	}
	
	private void init(String device) {
		this.device = device;
		this.dataStoreEventObject = new Object();
	}
	
	public void append(Humidity humidity) {
		synchronized(this.dataStoreEventObject) {
			appendSync(humidity);
		}
	}
	
	public List<Humidity> readDataStore() {
		synchronized(this.dataStoreEventObject) {
			return readDataStoreSync();
		}
	}
	
	public void cleanup(long lastSequence) {
		synchronized(this.dataStoreEventObject) {
			cleanupSync(lastSequence);
		}
	}
	
	public String getDevice() {
		return this.device;
	}
	
	private void appendSync(Humidity humidity) {
		String lastLine = getLastLine();
//		System.out.println(String.format("[%s]", lastLine));
		
		long sequence = getLastSequence(lastLine);
		Humidity newItem = Humidity.cloneWithSequence(humidity, sequence + 1);
		
		String line = String.format("%08d,%s,%s,%f\n",
				newItem.getSequence(),
				newItem.getDevice(),
				Util.dateToString(newItem.getTime()),
				newItem.getDegree());
		
		File file = getFile();
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(line);
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(writer);
		}
	}
	
	private String getLastLine() {
		String line = "";
		File file = getFile();
		if (2 > file.length()) {
			return line;
		}
		RandomAccessFile access = null;
		
		try {
			access = new RandomAccessFile(file, "r");
			long pos = access.length() - 2;
//			System.out.println(pos);
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
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(access);
		}
		
		return line;
	}
	
	private Humidity retreiveHumidity(String line) {
		if (0 >= line.length()) {
			return null;
		}
		String[] values = line.split(",");
		Humidity humidity = null;
		
		try {
			long sequence = Long.valueOf(values[0]);
			String friendlyName = values[1];
			Date time = Util.stringToDate(values[2]);
			double degree = Double.valueOf(values[3]);
			
			humidity = new Humidity(sequence, friendlyName, time, degree);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return humidity;
	}
	
	private long getLastSequence(String line) {
		long sequence = 0;
		Humidity humidity = retreiveHumidity(line);
		
		if (null != humidity) {
			sequence = humidity.getSequence();
		}
		return sequence;
	}
	
	private List<Humidity> readDataStoreSync() {
		File file = getFile();
		BufferedReader reader = null;
		List<Humidity> list = new ArrayList<Humidity>();
		Map<Date, Integer> timeMap = new HashMap<Date, Integer>();
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;

			// ファイルを1行ずつ読み込む
			while (null != (line = reader.readLine())) {
				Humidity humidity = retreiveHumidity(line);
				
				if (null == humidity) {
					continue;
				}
				
				Date time = humidity.getTime();
				
				if (!timeMap.containsKey(time)) {
					list.add(humidity);
					timeMap.put(time, list.size() - 1);
				}
				else {
					list.set(timeMap.get(time), humidity);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(reader);
		}
		
		return list;
	}
	
	private void cleanupSync(long lastSequence) {
		String[] newLineAry = readNewLines(lastSequence);
		
		if (0 >= newLineAry.length) {
			truncateFile();
			return;
		}
		writeNewLines(newLineAry);
	}
	
	private String[] readNewLines(long lastSequence) {
		File file = getFile();
		BufferedReader reader = null;
		List<String> list = new ArrayList<String>();
		String[] lineAry = new String[] {};
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;

			// ファイルを1行ずつ読み込む
			while (null != (line = reader.readLine())) {
				Humidity humidity = retreiveHumidity(line);
				
				if (null != humidity &&
						lastSequence < humidity.getSequence()) {
					
					list.add(line);
				}
			}
			
			lineAry = new String[list.size()];
			list.toArray(lineAry);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(reader);
		}
		return lineAry;
	}
	
	private void writeNewLines(String[] newLineAry) {
		File file = getFile();
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter(file, false));
			
			for (String line : newLineAry) {
				writer.write(String.format("%s\n", line));
			}
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(writer);
		}
	}
	
	private void truncateFile() {
		File file = getFile();
		RandomAccessFile access = null;
		
		try {
			access = new RandomAccessFile(file, "rw");
			access.setLength(0);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			closeReaderWriter(access);
		}
	}
	
	private File getFile() {
		return new File(this.device);
	}
	
	private void closeReaderWriter(Closeable readerWriter) {
		try {
			if (null != readerWriter) {
				readerWriter.close();
			}
		}
		catch (IOException e) {
			
		}
	}
}
