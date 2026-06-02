package Project1Game.component.npc;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

public class NPCAnimationComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel animDown, animRight, animLeft, animUp;

    public NPCAnimationComponent() {
        this("NPC/Guider/Guider.png");
    }

    public NPCAnimationComponent(String textureName) {
        // Giả sử sprite sheet có 4 khung hình mỗi hàng. 

        int framesPerRow = 4;
        
        animDown  = new AnimationChannel(FXGL.image(textureName), framesPerRow, 32, 64, Duration.seconds(0.8), 0, 3);
        animRight = new AnimationChannel(FXGL.image(textureName), framesPerRow, 32, 64, Duration.seconds(0.8), 4, 7);
        animLeft  = new AnimationChannel(FXGL.image(textureName), framesPerRow, 32, 64, Duration.seconds(0.8), 8, 11);
        animUp    = new AnimationChannel(FXGL.image(textureName), framesPerRow, 32, 64, Duration.seconds(0.8), 12, 15);

        texture = new AnimatedTexture(animDown);
    }

    @Override
    public void onAdded() {
        // Thêm texture vào view của Entity
        entity.getViewComponent().addChild(texture);
        texture.loop(); // Bắt đầu chạy vòng lặp hoạt ảnh
    }

    @Override
    public void onUpdate(double tpf) {

    }

    // Các phương thức hỗ trợ đổi hướng nhìn cho NPC
    public void faceDown() { texture.loopAnimationChannel(animDown); }
    public void faceRight() { texture.loopAnimationChannel(animRight); }
    public void faceLeft() { texture.loopAnimationChannel(animLeft); }
    public void faceUp() { texture.loopAnimationChannel(animUp); }
}
