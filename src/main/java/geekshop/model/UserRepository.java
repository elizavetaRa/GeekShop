package geekshop.model;

/**
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.core.SalespointRepository;
import org.springframework.stereotype.Component;

@Component("userRepository")
public interface UserRepository extends SalespointRepository<User, Long> {

//    User save(User entry);
//    Optional<User> findOne(String userName);
//    Iterable<User> findAll();
//    int count();
}
