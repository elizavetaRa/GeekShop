package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link geekshop.model.Message}s.
 *
 * @author Felix Döring
 */
public interface MessageRepository extends SalespointRepository<Message, Long> {
    
    Iterable<Message> findByMessageKind(MessageKind kind);
}
