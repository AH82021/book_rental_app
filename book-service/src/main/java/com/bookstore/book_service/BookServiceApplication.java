package com.bookstore.book_service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication
@EnableCaching
public class BookServiceApplication {



	public static void main(String[] args) {



		SpringApplication.run(BookServiceApplication.class, args);


		log.info("Book Service Application has started successfully.");


	}




}
