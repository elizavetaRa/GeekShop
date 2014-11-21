package geekshop.model;

/**
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private UserAccount userAccount;

    private String userName;

    public User(UserAccount userAccount, String userName){
        Assert.notNull(userAccount, "UserAccount must not be null.");
        Assert.hasText(userName, "Username must not be null.");

        this.userAccount = userAccount;
        this.userName = userName;
    }

    public User(){}

    public String getUserName() { return userName; }

    public UserAccount getUserAccount(){
        return userAccount;
    }
}
