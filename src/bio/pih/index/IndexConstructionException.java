package bio.pih.index;

public class IndexConstructionException extends RuntimeException {

	private static final long serialVersionUID = 3385806705266172005L;
	String cause;
	Throwable root;

	public IndexConstructionException(String cause) {
		this.cause = cause;
	}
	
	public IndexConstructionException(Throwable root) {
		this.root = root;
	}
	
	public IndexConstructionException(String cause, Throwable root) {
		this.root = root;
		this.cause = cause;
	}	
}
