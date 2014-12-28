package geekshop;

import org.salespointframework.EnableSalespoint;
import org.salespointframework.SalespointSecurityConfiguration;
import org.salespointframework.SalespointWebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

/**
 * The central application class to configure the Spring container and run the application.
 *
 * @author Felix D&ouml;ring
 * @author Sebastian D&ouml;ring
 */

@EnableSalespoint
public class GeekShop {

    public static void main(String[] args) {
        SpringApplication.run(GeekShop.class, args);
    }


    @Configuration
    static class GeekShopWebConfiguration extends SalespointWebConfiguration {

        /**
         * We configure {@code /login} to be directly routed to the {@code login} template without any controller
         * interaction.
         */
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/login").setViewName("login");
        }
    }

    @Configuration
    static class WebSecurityConfiguration extends SalespointSecurityConfiguration {

        /**
         * Disabling Spring Security's CSRF support as we do not implement pre-flight request handling for the sake of
         * simplicity. Setting up basic security and defining login and logout options.
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.csrf().disable();

            http.authorizeRequests().antMatchers("/**").permitAll().and().
                    formLogin().loginPage("/login").loginProcessingUrl("/login").defaultSuccessUrl("/", true).and().
                    logout()/*.logoutUrl("/logout").logoutSuccessUrl("/")*/;
        }
    }

}
