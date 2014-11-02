package Prototype;

import org.junit.Test;

/**
 * Created by h4llow3En on 31/10/14.
 */
public class RepositoryEntryTest {
    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyName() {
        new Article("",123.4f, "May the 4th be with you!", 50l);
    }

}
