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

/**
 * Bluetoothから受信した湿度の値をローカルファイルに保存する
 *
 */
public class DataStore {
	private String device;
	private Object dataStoreEventObject;
	
	/**
	 * コンストラクタ
	 * 
	 * @param device デバイス名
	 */
	public DataStore(String device) {
		init(device);
	}
	
	/**
	 * 初期化処理
	 * 
	 * @param device デバイス名
	 */
	private void init(String device) {
		this.device = device;
		this.dataStoreEventObject = new Object();
	}
	
	/**
	 * ファイルにレコードを追加する
	 * 
	 * @param humidity 湿度データ
	 */
	public void append(Humidity humidity) {
		synchronized(this.dataStoreEventObject) {
			appendSync(humidity);
		}
	}
	
	/**
	 * ファイルからすべてのレコードを読み出す
	 * 
	 * @return 湿度データのリスト
	 */
	public List<Humidity> readDataStore() {
		synchronized(this.dataStoreEventObject) {
			return readDataStoreSync();
		}
	}
	
	/**
	 * ファイルから転送済みのレコードを削除する
	 * 
	 * @param lastSequence 転送に成功した最後のシーケンス番号
	 */
	public void cleanup(long lastSequence) {
		synchronized(this.dataStoreEventObject) {
			cleanupSync(lastSequence);
		}
	}
	
	/**
	 * デバイス名を返す
	 * 
	 * @return デバイス名
	 */
	public String getDevice() {
		return this.device;
	}
	
	/**
	 * ローカルファイルにレコードを追加する
	 * 
	 * @param humidity 湿度データ
	 */
	private void appendSync(Humidity humidity) {
		// ファイルの最後のレコードを読みだす
		String lastLine = getLastLine();
//		System.out.println(String.format("[%s]", lastLine));
		
		// 最後のレコードのシーケンス番号を取り出す
		long sequence = getLastSequence(lastLine);
		// 最後のシーケンス番号に1を加えた値を新しい湿度データにセットする
		Humidity newItem = Humidity.cloneWithSequence(humidity, sequence + 1);
		
		// 湿度データからレコードを生成
		String line = String.format("%08d,%s,%s,%f\n",
				newItem.getSequence(),
				newItem.getDevice(),
				Util.dateToString(newItem.getTime()),
				newItem.getDegree());
		
		// ファイルを準備
		File file = getFile();
		BufferedWriter writer = null;
		
		try {
			// ファイルに新しいレコードを書き込む
			writer = new BufferedWriter(new FileWriter(file, true));
			writer.write(line);
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(writer);
		}
	}
	
	/**
	 * ファイルの最後のレコードを読み出す
	 * 
	 * @return ファイルの最後のレコード
	 */
	private String getLastLine() {
		String line = "";
		File file = getFile();
		
		// ファイルサイズが2より小さい場合は空ファイルであると判断して空文字列を返す
		if (2 > file.length()) {
			return line;
		}
		RandomAccessFile access = null;
		
		try {
			// ランダムアクセスでファイルを開く
			access = new RandomAccessFile(file, "r");
			
			// ファイルの末尾を指す
			long pos = access.length() - 2;
//			System.out.println(pos);
			boolean found = false;
			
			try {
				// ファイルの先頭にたどり着くか、または改行文字が見つかるまで繰り返し
				while (pos > 0 && !found) {
					// ファイルから文字を読みだす
					access.seek(pos);
					int character = access.read();
					
					// 改行文字かどうかを判定
					if (0x0a == character) {
						found = true;
					}
					else {
						// 改行文字でなければひとつ前に移動する
						pos -= 1;
					}
				}
				
				// 改行文字が見つかったら一文字移動してレコードの先頭を指すようにする
				if (found) {
					pos += 1;
				}
				// ファイルの最後のレコードを読み出す
				access.seek(pos);
				line = access.readLine();
			}
			catch (EOFException ex) {
				// EOFなら空文字列を返す
				ex.printStackTrace();
			}
		}
		catch (FileNotFoundException e) {
			// ファイルが見つからなければ空文字列を返す
			e.printStackTrace();
		}
		catch (IOException e) {
			// ファイルの読み取りに失敗したら空文字列を返す
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(access);
		}
		
		return line;
	}
	
	/**
	 * ファイルのレコードから湿度データのインスタンスを生成する
	 * 
	 * @param line ファイルから読み出したレコード
	 * @return 湿度データ
	 */
	private Humidity retreiveHumidity(String line) {
		// 空文字列は無視する
		if (0 >= line.length()) {
			return null;
		}
		// 区切り文字で分解する
		String[] values = line.split(",");
		Humidity humidity = null;
		
		try {
			// 各項目の値の型を変換する
			long sequence = Long.valueOf(values[0]);
			String friendlyName = values[1];
			Date time = Util.stringToDate(values[2]);
			double degree = Double.valueOf(values[3]);
			
			// 湿度データのインスタンスを生成
			humidity = new Humidity(sequence, friendlyName, time, degree);
		}
		catch (ParseException e) {
			// 日付の書式が不正
			e.printStackTrace();
		}
		catch (NumberFormatException e) {
			// 数値の書式が不正
			e.printStackTrace();
		}
		return humidity;
	}
	
	/**
	 * ファイルレコードからシーケンス番号を取り出す
	 * 
	 * @param line ファイルから読み出したレコード
	 * @return シーケンス番号
	 */
	private long getLastSequence(String line) {
		long sequence = 0;
		
		// ファイルのレコードから湿度データのインスタンスを生成する
		Humidity humidity = retreiveHumidity(line);
		
		// シーケンス番号を取り出す
		if (null != humidity) {
			sequence = humidity.getSequence();
		}
		return sequence;
	}
	
	/**
	 * ファイルからすべてのレコードを読み出す
	 * 
	 * @return 湿度データのリスト
	 */
	private List<Humidity> readDataStoreSync() {
		File file = getFile();
		BufferedReader reader = null;
		List<Humidity> list = new ArrayList<Humidity>();
		Map<Date, Integer> timeMap = new HashMap<Date, Integer>();
		
		try {
			// ファイルを開く
			reader = new BufferedReader(new FileReader(file));
			String line = null;

			// ファイルを1行ずつ読み込む
			while (null != (line = reader.readLine())) {
				// ファイルのレコードから湿度データのインスタンスを生成する
				Humidity humidity = retreiveHumidity(line);
				
				if (null == humidity) {
					continue;
				}
				
				Date time = humidity.getTime();
				
				// すでに同じ時刻のレコードが存在するかどうかを判定する
				// プログラムが起動したときにすでにBluetoothから送信されていた
				// 湿度の値がファイルに書き込まれていることがある。
				// 最少分解精度を1秒として、同時刻のレコードがある場合は
				// 最後のレコードだけを有効とする
				if (!timeMap.containsKey(time)) {
					// リストに湿度データを追加
					list.add(humidity);
					// レコードの日時がリストの何番目に追加されたかを記憶しておく
					timeMap.put(time, list.size() - 1);
				}
				else {
					// リストの同時刻の湿度データを差し替える
					list.set(timeMap.get(time), humidity);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(reader);
		}
		
		return list;
	}
	
	/**
	 * ファイルから転送済みのレコードを削除する
	 * 
	 * @param lastSequence 転送に成功した最後のシーケンス番号
	 */
	private void cleanupSync(long lastSequence) {
		// 未転送のレコードをファイルから読み出す
		String[] newLineAry = readNewLines(lastSequence);
		
		// 未転送のレコードがなければ
		if (0 >= newLineAry.length) {
			// ファイルを空にする
			truncateFile();
			return;
		}
		
		// 未転送のレコードだけをもう一度ファイルに書き込む
		writeNewLines(newLineAry);
	}
	
	/**
	 * 未転送のレコードをファイルから読み出す
	 * 
	 * @param lastSequence 転送に成功した最後のシーケンス番号
	 * @return 未転送のレコードの配列
	 */
	private String[] readNewLines(long lastSequence) {
		File file = getFile();
		BufferedReader reader = null;
		List<String> list = new ArrayList<String>();
		String[] lineAry = new String[] {};
		
		try {
			// ファイルを開く
			reader = new BufferedReader(new FileReader(file));
			String line = null;

			// ファイルを1行ずつ読み込む
			while (null != (line = reader.readLine())) {
				// ファイルのレコードから湿度データのインスタンスを生成する
				Humidity humidity = retreiveHumidity(line);
				
				// シーケンス番号を比較
				if (null != humidity &&
						lastSequence < humidity.getSequence()) {
					
					// 未転送ならリストに追加する
					list.add(line);
				}
			}
			
			// リストを配列に変換
			lineAry = new String[list.size()];
			list.toArray(lineAry);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(reader);
		}
		return lineAry;
	}
	
	/**
	 * 未転送のレコードだけをもう一度ファイルに書き込む
	 * 
	 * @param newLineAry　未転送のレコードの配列
	 */
	private void writeNewLines(String[] newLineAry) {
		File file = getFile();
		BufferedWriter writer = null;
		
		try {
			// ファイルを開く
			writer = new BufferedWriter(new FileWriter(file, false));
			
			// すべてのレコードについて繰り返し
			for (String line : newLineAry) {
				// ファイルに書き込む
				writer.write(String.format("%s\n", line));
			}
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(writer);
		}
	}
	
	/**
	 * ファイルを空にする
	 */
	private void truncateFile() {
		File file = getFile();
		RandomAccessFile access = null;
		
		try {
			// ファイルを開く
			access = new RandomAccessFile(file, "rw");
			// ファイルを空にする
			access.setLength(0);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			// ファイルを閉じる
			closeReaderWriter(access);
		}
	}
	
	/**
	 * ファイルのインスタンスを生成する
	 * @return
	 */
	private File getFile() {
		return new File(this.device);
	}
	
	/**
	 * ファイルを閉じる
	 * 
	 * @param readerWriter ファイルの入出力を行うオブジェクト
	 */
	private void closeReaderWriter(Closeable readerWriter) {
		try {
			if (null != readerWriter) {
				// ファイル閉じる
				readerWriter.close();
			}
		}
		catch (IOException e) {
			// 例外は無視する
		}
	}
}
