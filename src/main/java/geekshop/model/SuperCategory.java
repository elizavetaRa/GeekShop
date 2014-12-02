package geekshop.model;

/**
 * Created by Marc on 02.12.2014.
 */
public class SuperCategory {

    private String name;

    @Deprecated
    protected SuperCategory() {
    }

    public SuperCategory(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
