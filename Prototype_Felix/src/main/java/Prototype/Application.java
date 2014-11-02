package Prototype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Created by h4llow3En on 30/10/14.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String args[]){
        SpringApplication.run(Application.class, args);}

//    @Bean
//    public CharacterEncodingFilter characterEncodingFilter(){
//        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
//        characterEncodingFilter.setEncoding("UTF-8");
//        characterEncodingFilter.setForceEncoding(true);
//
//        return characterEncodingFilter;
//    }

    @Autowired ArticleRepository articleRepository;
    @PostConstruct
    void initialize(){
        articleRepository.save(new Article("ARTIKEL-Name", 23.1f, "Dies ist eine Beschreibung fuer den Artiken", 5));
        articleRepository.save(new Article("ARTIKEL-2", 1.2f, "Dies ist eine Beschreibung fuer den Artiken", 5));
        articleRepository.save(new Article("ARTIKEL-test", 87654f, "Dies ist eine Beschreibung fuer den Artiken", 5));
        articleRepository.save(new Article("Schluesselband", 1234.65f, "Dies ist eine Beschreibung fuer den Artiken", 5));
        articleRepository.save(new Article("ARTIKEL-Name", 0f, "Dies ist eine Beschreibung fuer den Artiken", 5));
        articleRepository.save(new Article("ARTIKEL-Name", 9876.54f, "Dies ist eine Beschreibung fuer den Artiken", 5));
    }
}
