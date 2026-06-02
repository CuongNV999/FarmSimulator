package Project1Game.component.farming.monster;

/**
 * Specific AI behavior component for Fox entities.
 * Fox is a CARNIVORE with 220.0px flee radius.
 */
public class FoxComponent extends BaseMonsterComponent {

    public FoxComponent() {
        super(220.0, MonsterClassification.CARNIVORE);
    }
}
