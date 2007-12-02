package bio.pih.scheduler;

public interface Worker {
		
	int getPosition();
	String getIdentifier();
	void setIdentifier();
	float getLoad();
	int getRunning();
	
}
