package jp.co.uniquevision.screamingpot.receiver.models;

/**
 * ElasticeSearch Bulk APIのメタレコードのJSONパラメータ定義
 *
 */
public class EsMeta {
	private String _index;
	private String _type;
	private String _id;
	
	public EsMeta(String index, String type, String id) {
		this._index = index;
		this._type = type;
		this._id = id;
	}
}
