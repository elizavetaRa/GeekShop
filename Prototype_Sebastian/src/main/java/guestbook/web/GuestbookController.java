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
package guestbook.web;

import guestbook.Guestbook;
import guestbook.GuestbookEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * A controller to handle web requests to manage {@link GuestbookEntry}s
 *
 * @author Paul Henke
 * @author Oliver Gierke
 */
@Controller
public class GuestbookController {

    // A special header sent with each AJAX request
    private static final String IS_AJAX_HEADER = "X-Requested-With=XMLHttpRequest";

    private final Guestbook guestbook;

    /**
     * Creates a new {@link GuestbookController} using the given {@link Guestbook}. The {@link Autowired} causes the
     * Spring container to try to find a Spring bean of type {@link Guestbook} and use it to create an instance of
     * {@link GuestbookController}.
     *
     * @param guestbook must not be {@literal null}.
     */
    @Autowired
    public GuestbookController(Guestbook guestbook) {

        Assert.notNull(guestbook, "Guestbook must not be null!");
        this.guestbook = guestbook;
    }

    /**
     * Handles requests to the application root URI. Note, that you can use {@code redirect:} as prefix to trigger a
     * browser redirect instead of simply rendering a view.
     *
     * @return
     */
    @RequestMapping("/")
    public String index() {
        return "redirect:/guestbook";
    }

    /**
     * Handles requests to access the guestbook. Obtains all currently available {@link GuestbookEntry}s and puts them
     * into the {@link Model} that's used to render the view.
     *
     * @return
     */
    @RequestMapping(value = "/guestbook", method = RequestMethod.GET)
    public String guestBook(Model model) {

        model.addAttribute("entries", guestbook.findAll());
        return "guestbook";
    }

    /**
     * Handles requests to create a new {@link GuestbookEntry}. Uses the fields {@code name} and {@code text} from the
     * HTML form via the {@link RequestParam} annotations. The mapping also supports other types than {@link String}, see
     * {@link #removeEntry(Long)}.
     * <p/>
     * For the sake of simplicity we don't do any validation here. Spring has support for that kind of stuff but we leave
     * that for the VideoShop example to cover.
     *
     * @param name the name of the person that made the entry
     * @param text the actual text of the entry
     * @return
     */
    @RequestMapping(value = "/guestbook", method = RequestMethod.POST)
    public String addEntry(@RequestParam("name") String name, @RequestParam("text") String text, @RequestParam("color") String color) {
        color = modifyColor(color);

        guestbook.save(new GuestbookEntry(name, text, color));
        return "redirect:/guestbook";
    }

    /**
     * Handles AJAX requests to create a new {@link GuestbookEntry}.
     *
     * @param name  the name of the person that made the entry
     * @param text  the actual text of the entry
     * @param model
     * @return
     * @see #addEntry(String, String, String)
     */
    @RequestMapping(value = "/guestbook", method = RequestMethod.POST, headers = IS_AJAX_HEADER)
    public String addEntry(@RequestParam("name") String name, @RequestParam("text") String text, @RequestParam("color") String color, Model model) {
        color = modifyColor(color);

        model.addAttribute("entry", guestbook.save(new GuestbookEntry(name, text, color)));
        model.addAttribute("index", guestbook.count());
        return "guestbook :: entry";
    }

    /**
     * Deletes a {@link GuestbookEntry}. Note how the path variable used in the {@link RequestMapping} annotation is bound
     * to the controller method using the {@link PathVariable} annotation.
     *
     * @param id the id of the {@link GuestbookEntry} to delete.
     * @return
     */
    @RequestMapping(value = "/guestbook/{id}", method = RequestMethod.DELETE)
    public String removeEntry(@PathVariable Long id) {
        guestbook.delete(id);
        return "redirect:/guestbook";
    }

    /**
     * Handles AJAX requests to delete {@link GuestbookEntry}s.
     *
     * @param id the id of the {@link GuestbookEntry} to delete.
     * @return
     */
    @RequestMapping(value = "/guestbook/{id}", method = RequestMethod.DELETE, headers = IS_AJAX_HEADER)
    public HttpEntity<?> removeEntryJS(@PathVariable Long id) {

        Optional<GuestbookEntry> entry = guestbook.findOne(id);

        if (!entry.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        guestbook.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);

        // Java 8 style use of Optional, less readable in this case.

        // return entry.map(e -> {
        // guestbook.delete(id);
        // return new ResponseEntity<>(HttpStatus.OK);
        // }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private String modifyColor(String color) {
        color = color.trim();
        if (color.matches("^#([0-9A-F]{3}|[0-9A-F]{6})$"))
            return color;
        else if (color.matches("^([0-9A-F]{3}|[0-9A-F]{6})$"))
            return "#" + color;
        else
            return "#FFFFFF";
    }
}
