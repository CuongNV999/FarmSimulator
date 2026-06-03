package Project1Game.component.farming.monster;

/**
 * Specific AI behavior component for Hare entities.
 * Hare is a HERBIVORE with 300.0px flee radius.
 */
public class HareComponent extends BaseMonsterComponent {

    public HareComponent() {
        super(300.0, MonsterClassification.HERBIVORE);
    }
}
