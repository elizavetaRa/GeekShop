package geekshop.model;

import org.salespointframework.core.SalespointRepository;

/**
 * Repository to store {@link Joke}s.
 *
 * @author Sebastian D&ouml;ring
 * @author Felix D&ouml;ring
 */

public interface JokeRepository extends SalespointRepository<Joke, Long> {

    Joke findJokeById(Long id);

}
