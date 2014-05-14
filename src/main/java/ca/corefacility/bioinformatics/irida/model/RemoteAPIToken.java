package ca.corefacility.bioinformatics.irida.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * OAuth2 token for communicating with a {@link RemoteAPI} for a given {@link User}
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
@Entity
@Table(name="remote_api_token", uniqueConstraints= @UniqueConstraint(columnNames = { "user_id","remote_api_id" }))
@Audited
public class RemoteAPIToken {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private String tokenString;
	
	@NotNull
	private Date expiryDate;
	
	@ManyToOne
	@JoinColumn(name="remote_api_id")
	@NotNull
	RemoteAPI remoteApi;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	@NotNull
	User user;
	
	public RemoteAPIToken(){
	}
	
	public RemoteAPIToken(String tokenString, RemoteAPI remoteApi,Date expiryDate) {
		super();
		this.tokenString = tokenString;
		this.remoteApi = remoteApi;
		this.expiryDate = expiryDate;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the tokenString
	 */
	public String getTokenString() {
		return tokenString;
	}

	/**
	 * @param tokenString the tokenString to set
	 */
	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}

	/**
	 * @return the remoteApi
	 */
	public RemoteAPI getRemoteApi() {
		return remoteApi;
	}

	/**
	 * @param remoteApi the remoteApi to set
	 */
	public void setRemoteApi(RemoteAPI remoteApi) {
		this.remoteApi = remoteApi;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "RemoteAPIToken [tokenString=" + tokenString + ", remoteApi=" + remoteApi + ", user=" + user + "]";
	}

	/**
	 * Get the date that this token expires
	 * @return
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}

	/**
	 * Set the date that this token expires
	 * @param expiryDate
	 */
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	/**
	 * Test if this token has expired
	 * @return true if this token has expired
	 */
	public boolean isExpired(){		
		return (new Date()).after(expiryDate);
	}
	
}
