package vo;

public class UserMeta2 {
	private int id;
	private int rep_id;
	private String nickname;
	private int admin_level;
	
	public UserMeta2() {}

	public UserMeta2(int id, int rep_id, String nickname, int admin_level) {
		super();
		this.id = id;
		this.rep_id = rep_id;
		this.nickname = nickname;
		this.admin_level = admin_level;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRep_id() {
		return rep_id;
	}

	public void setRep_id(int rep_id) {
		this.rep_id = rep_id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getAdmin_level() {
		return admin_level;
	}

	public void setAdmin_level(int admin_level) {
		this.admin_level = admin_level;
	}

	@Override
	public String toString() {
		return "UserMeta2 [id=" + id + ", rep_id=" + rep_id + ", nickname=" + nickname + ", admin_level=" + admin_level
				+ "]";
	}

	
	
	

}
