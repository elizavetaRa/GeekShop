package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link Joke}s.
 *
 * @author Sebastian Döring
 * @author Felix Döring
 */

public interface JokeRepository extends SalespointRepository<Joke, Long> {

    Joke findJokeById(Long id);

}
