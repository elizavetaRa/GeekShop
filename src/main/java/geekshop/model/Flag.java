package geekshop.model;

/**
 * Created by Lisa on 22.12.2014.
 */
public class Flag {


    private boolean reclaimModus;

    public Flag(){
        this.reclaimModus=true;

    }

    public boolean isReclaimModus() {
        return reclaimModus;
    }

    public void switchModus(){

        if (this.reclaimModus==true){this.reclaimModus=false;}
        else this.reclaimModus=true;

    }

}
