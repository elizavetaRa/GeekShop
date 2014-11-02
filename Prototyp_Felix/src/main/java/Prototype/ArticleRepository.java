package Prototype;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by h4llow3En on 30/10/14.
 */
@Component("articleRepository")
public interface ArticleRepository extends Repository<Article, Long> {
    void delete(Long Id);

    Article save(Article article);
    Optional<Article> findOne(Long id);
    Iterable<Article> findAll();
    int count();
}
