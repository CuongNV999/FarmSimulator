package Project1Game.component.farming.monster;

/**
 * Specific AI behavior component for Deer entities.
 * Deer is a HERBIVORE with 250.0px flee radius.
 */
public class DeerComponent extends BaseMonsterComponent {

    public DeerComponent() {
        super(250.0, MonsterClassification.HERBIVORE);
    }
}
