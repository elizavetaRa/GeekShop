package GeekShop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    @Autowired UserRepository userRepository;

    @PostConstruct
    void initialize(){
        userRepository.save(new User("test3", "T3st"));
        userRepository.save(new User("test2", "T2st"));
        userRepository.save(new User("test", "T1st"));
    }
}
