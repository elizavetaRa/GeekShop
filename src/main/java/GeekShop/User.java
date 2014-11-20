package GeekShop;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by h4llow3En on 20/11/14.
 */
@Entity
public class User {

    private @Id @GeneratedValue Long id;
    private String userName;
    private String password;

    public User(String userName, String password){
        Assert.hasText(userName, "Username can not be Null.");
        Assert.hasText(password, "Password con not be Null.");

        this.userName = userName;
        this.password = password;
    }

    public User(){}

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }
}
