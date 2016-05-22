package jp.co.uniquevision.screamingpot.receiver.models;

public class EsMetaCreate {
	private EsMeta create;
	
	public EsMetaCreate(String index, String type, String id) {
		this.create = new EsMeta(index, type, id);
	}
}
