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
        articleRepository.save(new Article("Test", 23.1f, "Test", 5));
    }
}
