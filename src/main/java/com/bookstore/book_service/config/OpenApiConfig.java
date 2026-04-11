package com.bookstore.book_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {



    public OpenAPI bookService(){

        return  new OpenAPI()
                .info( new Info()
                        .title("Book Service API")
                        .description("Rest API documentation for the Book Service application")
                        .version("V1.0")
                        .contact( new Contact()
                                .name("Bookstore API Support Team")
                                .email("bookservice@bookstore.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("\"https://opensource.org/licenses/MIT")));



    }

}
