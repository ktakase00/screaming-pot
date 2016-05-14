package jp.co.uniquevision.screamingpot.receiver.discovery;

import java.util.Vector;

/**
 * 探索終了時の処理を定義するインターフェイス
 *
 * @param <T> 発見したアイテムの一覧
 */
interface DiscoveryCompleteListener<T> {
	void onDiscoveryComplete(Vector<T> discovered);
}
