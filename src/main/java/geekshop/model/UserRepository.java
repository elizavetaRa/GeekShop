package geekshop.model;

/*
 * Created by h4llow3En on 20/11/14.
 */

import org.salespointframework.core.SalespointRepository;
import org.salespointframework.useraccount.UserAccount;

/**
 * Repository to store {@link User}s.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

public interface UserRepository extends SalespointRepository<User, Long> {

    User findByUserAccount(UserAccount userAccount);
}
