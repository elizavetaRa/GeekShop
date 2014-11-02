package Prototype.web;

import Prototype.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * Created by h4llow3En on 30/10/14.
 */
@Controller
public class ArticleRepositoryController {
    private static final String IS_AJAX_HEADER = "X-Requested-With=XMLHttpRequest";

    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleRepositoryController(ArticleRepository articleRepository) {

        Assert.notNull(articleRepository, "Has not to be null.");
        this.articleRepository = articleRepository;

    }

    @RequestMapping("/")
    public String index() {
        return "redirect:/repository";
    }

    @RequestMapping(value = "/repository", method = RequestMethod.GET)
    public String repository(Model model) {

        model.addAttribute("entries", articleRepository.findAll());
        return "repository";
    }

    @RequestMapping(value = "/repository", method = RequestMethod.POST)
    public String addEntry(@RequestParam("artName") String artName, @RequestParam("artPrice") String artPrice, @RequestParam("artDescription") String artDescription, @RequestParam("artQuantity") String artQuantity) {
        float price = Float.parseFloat(artPrice);
        long quantity = Long.parseLong(artQuantity, 10);
        articleRepository.save(new Article(artName, price, artDescription, quantity));
        return "redirect:/repository";
    }

    @RequestMapping(value = "/repository", method = RequestMethod.POST, headers = IS_AJAX_HEADER)
    public String addEntry(@RequestParam("artName") String artName, @RequestParam("artPrice") String artPrice, @RequestParam("artDescription") String artDescription, @RequestParam("artQuantity") String artQuantity, Model model) {

        float price = Float.parseFloat(artPrice);
        long quantity = Long.parseLong(artQuantity, 10);
        model.addAttribute("entry", articleRepository.save(new Article(artName, price, artDescription, quantity)));
        model.addAttribute("index", articleRepository.count());
        return "repository :: entry";
    }

    @RequestMapping(value = "/repository/{artId}", method = RequestMethod.DELETE)
    public String removeEntry(@PathVariable Long artId) {
        articleRepository.delete(artId);
        return "redirect:/repository";
    }

    @RequestMapping(value = "/guestbook/{artId}", method = RequestMethod.DELETE, headers = IS_AJAX_HEADER)
    public HttpEntity<?> removeEntryJS(@PathVariable Long artId) {

        Optional<Article> entry = articleRepository.findOne(artId);

        if (!entry.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        articleRepository.delete(artId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
