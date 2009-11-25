package bio.pih.genoogle.index;

public class IndexConstructionException extends Exception {

	private static final long serialVersionUID = 3385806705266172005L;
	

	public IndexConstructionException(String message) {
		super(message);
	}
	
	public IndexConstructionException(Throwable cause) {
		super(cause);
	}
	
	public IndexConstructionException(String message, Throwable cause) {
		super(message, cause);
	}	
}
