/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guestbook;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * The core class to bootstrap our application. It trigger the auto-configuration of the Spring Container (see
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration}) and activates component-scanning (see {@link org.springframework.context.annotation.ComponentScan}). At the same time the
 * class acts as configuration class to configure additional components (see {@link #characterEncodingFilter()}) that
 * the contain will take into account when bootstrapping.
 *
 * @author Paul Henke
 * @author Oliver Gierke
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

	/**
	 * The main application method, bootstraps the Spring container.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CharacterEncodingFilter characterEncodingFilter() {

		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);

		return characterEncodingFilter;
	}

	@Autowired Guestbook guestbook;

	/**
	 * Some initializing code to pre-fill our database with {@link GuestbookEntry}. The {@link javax.annotation.PostConstruct} annotation
	 * is causing the Spring container to trigger the execution on application startup.
	 */
	@PostConstruct
	void initialize() {

		guestbook.save(new GuestbookEntry("H4xx0r", "first!!!", "male"));
		guestbook.save(new GuestbookEntry("Arni", "Hasta la vista, baby", "male"));
		guestbook.save(new GuestbookEntry("Duke Nukem",
				"It's time to kick ass and chew bubble gum. And I'm all out of gum.", "male"));
		guestbook.save(new GuestbookEntry("Gump1337",
				"Mama always said life was like a box of chocolates. You never know what you're gonna get.", "female"));
	}
}
