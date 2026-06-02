package Project1Game.interaction;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;

public class InteractableComponent extends Component {
    private final Interactable interactable;

    public InteractableComponent(Interactable interactable) {
        this.interactable = interactable;
    }

    public void interact(Entity player) {
        interactable.interact(player, entity);
    }
}
