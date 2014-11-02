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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.util.Assert;

/**
 * A guestbook entry. An entity as in the Domain Driven Design context. Mapped onto the database using JPA annotations.
 * 
 * @author Paul Henke
 * @author Oliver Gierke
 * @see http://en.wikipedia.org/wiki/Domain-driven_design#Building_blocks_of_DDD
 */
@Entity
public class GuestbookEntry {

	private @Id @GeneratedValue Long id;
    private String name, text, colour="#000000";
	private Date date;

	/**
	 * Creates a new {@link GuestbookEntry} for the given name and text.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param text must not be {@literal null} or empty;
	 */
	public GuestbookEntry(String name, String text, String colour) {

		Assert.hasText(name, "Name must not be null or empty!");
		Assert.hasText(text, "Text must not be null or empty!");

		this.name = name;
		this.text = text;
		this.date = new Date();
        this.colour = colour;
	}

	GuestbookEntry() {}

	public String getName() {
		return name;
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getText() {
		return text;
	}

    public String getColour() { return colour; }

}
