package geekshop;

import geekshop.model.*;
import org.salespointframework.Salespoint;
import org.salespointframework.SalespointSecurityConfiguration;
import org.salespointframework.SalespointWebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import javax.annotation.PostConstruct;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = {Salespoint.class, GeekShop.class})
@EnableJpaRepositories(basePackageClasses = {Salespoint.class, GeekShop.class})
@ComponentScan
public class GeekShop {

    public static void main(String[] args) {
        SpringApplication.run(GeekShop.class, args);
    }


    @Autowired
    UserRepository userRepository;

    @PostConstruct
    void initialize(){
//        userRepository.save(new User(new UserAccount("efsr", "sdfs"), "T3st"));
//        userRepository.save(new User("test2", "T2st"));
//        userRepository.save(new User("test", "T1st"));
    }


    @Configuration
    static class GeekShopWebConfiguration extends SalespointWebConfiguration {

        /**
         * We configure {@code /login} to be directly routed to the {@code login} template without any controller
         * interaction.
         *
         * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addViewControllers(org.springframework.web.servlet.config.annotation.ViewControllerRegistry)
         */
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/login").setViewName("login");
        }
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class WebSecurityConfiguration extends SalespointSecurityConfiguration {

        /**
         * Disabling Spring Security's CSRF support as we do not implement pre-flight request handling for the sake of
         * simplicity. Setting up basic security and defining login and logout options.
         *
         * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.csrf().disable();

            http.authorizeRequests().antMatchers("/**").permitAll();
        }
    }
}
