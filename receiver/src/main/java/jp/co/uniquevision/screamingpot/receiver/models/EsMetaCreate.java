package jp.co.uniquevision.screamingpot.receiver.models;

/**
 * ElasticeSearch Bulk APIのCREATEレコードのJSONパラメータ定義
 *
 */
public class EsMetaCreate {
	private EsMeta create;
	
	public EsMetaCreate(String index, String type, String id) {
		this.create = new EsMeta(index, type, id);
	}
}
