package geekshop.model;

/**
 * Created by Marc on 02.12.2014.
 */
public class SubCategory {

    private String name;
    private SuperCategory superCategory;

    @Deprecated
    protected SubCategory() {
    }

    public SubCategory(String name, SuperCategory supc) {
        this.name = name;
        this.superCategory = supc;
    }

    public String getName(){
        return name;
    }

    public SuperCategory getSuperCategory(){
        return superCategory;
    }
}
