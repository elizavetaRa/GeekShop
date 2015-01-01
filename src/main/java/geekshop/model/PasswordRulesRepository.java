package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link PasswordRules}. Actually, only one instance thereof (with ID "{@code passwordRules}") should be stored.
 *
 * @author Sebastian Döring
 */

public interface PasswordRulesRepository extends SalespointRepository<PasswordRules, String> {

}
