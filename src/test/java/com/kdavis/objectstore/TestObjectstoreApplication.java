package com.kdavis.objectstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestObjectstoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(ObjectstoreApplication::main).with(TestObjectstoreApplication.class).run(args);
	}

}
