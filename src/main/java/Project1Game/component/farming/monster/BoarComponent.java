package Project1Game.component.farming.monster;

/**
 * Specific AI behavior component for Boar entities.
 * Boar is a CARNIVORE with 180.0px flee radius.
 */
public class BoarComponent extends BaseMonsterComponent {

    public BoarComponent() {
        super(180.0, MonsterClassification.CARNIVORE);
    }
}
