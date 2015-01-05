package geekshop.model;

import org.salespointframework.core.SalespointRepository;
import org.salespointframework.useraccount.UserAccount;

/**
 * Repository to store {@link User}s.
 *
 * @author Felix Döring
 * @author Sebastian Döring
 */

public interface UserRepository extends SalespointRepository<User, Long> {

    User findByUserAccount(UserAccount userAccount);
}
