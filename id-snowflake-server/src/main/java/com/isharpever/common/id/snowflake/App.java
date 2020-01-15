package com.isharpever.common.id.snowflake;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App {
    public static void main( String[] args ) {
        new SpringApplicationBuilder(App.class).run(args);
    }
}
