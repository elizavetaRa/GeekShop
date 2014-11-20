package GeekShop;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by h4llow3En on 20/11/14.
 */
@Component("userRepository")
public interface UserRepository extends Repository<User, Long> {

    User save(User entry);
    Optional<User> findOne(String userName);
    Iterable<User> findAll();
    int count();
}
