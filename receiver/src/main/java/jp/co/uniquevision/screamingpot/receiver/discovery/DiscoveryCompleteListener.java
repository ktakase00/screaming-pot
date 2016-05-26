package jp.co.uniquevision.screamingpot.receiver.discovery;

import java.util.Vector;

/**
 * 探索終了時の処理を定義するインターフェイス
 *
 * @param <T> 発見したアイテムの一覧
 */
public interface DiscoveryCompleteListener<T> {
	
	/**
	 * 探索終了時の処理
	 * 
	 * @param discovered 発見した端末やサービスのリスト
	 */
	void onDiscoveryComplete(Vector<T> discovered);
}
