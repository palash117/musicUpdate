package dto;

public class MusicUpdateDto {

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Long getLastChecked() {
		return lastChecked;
	}
	public void setLastChecked(Long lastChecked) {
		this.lastChecked = lastChecked;
	}
	private String user;
	private Long lastChecked;
}
