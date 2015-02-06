package info.subvocal.web;

import info.subvocal.service.api.ApiProfileConfig;
import info.subvocal.service.master.MasterProfileConfig;
import info.subvocal.service.sentiment.SentimentServiceProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *  main method for app
 */
@Configuration
@EnableWebMvc
//@EnableTransactionManagement(proxyTargetClass = true)
@EnableAutoConfiguration(
        // stop the various stuff from auto firing after scanning our Controller classes
        exclude = {
                HibernateJpaAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                VelocityAutoConfiguration.class})

@Import({
        ApiProfileConfig.class,
        MasterProfileConfig.class,
        SentimentServiceProfileConfig.class })
public class Application extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }




}
