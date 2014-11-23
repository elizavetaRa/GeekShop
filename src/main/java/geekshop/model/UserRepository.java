package GeekShop.model;

/**
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.useraccount.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

//@Component("userRepository")
public interface UserRepository extends CrudRepository<User, Long> {

//    User save(User entry);
//    Optional<User> findOne(String userName);
//    Iterable<User> findAll();
//    int count();
    User findByUserAccount(UserAccount userAccount);
}
