package info.subvocal.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *  main method for app
 */
@Configuration
@ComponentScan("info.subvocal.web")
@EnableWebMvc
//@EnableTransactionManagement(proxyTargetClass = true)
@EnableAutoConfiguration(
        // stop the various stuff from auto firing after scanning our Controller classes
        exclude = {
                HibernateJpaAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                VelocityAutoConfiguration.class})
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
