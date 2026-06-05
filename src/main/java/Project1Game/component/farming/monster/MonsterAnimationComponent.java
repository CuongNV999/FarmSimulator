package Project1Game.component.farming.monster;

import Project1Game.component.common.DirectionalAnimationComponent;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MonsterAnimationComponent extends DirectionalAnimationComponent {
    private PhysicsComponent physics;
    private AnimationChannel lastWalkAnim;

    private String monsterType;
    private boolean useFallback = false;

    public MonsterAnimationComponent(String monsterType, String walkTexturePath, String idleTexturePath) {
        this.monsterType = monsterType;
        Image walkImg = null;
        Image idleImg = null;

        try {
            walkImg = FXGL.image(walkTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load walk texture " + walkTexturePath + ", trying fallback...");
        }
        try {
            idleImg = FXGL.image(idleTexturePath);
        } catch (Exception e) {
            System.err.println("Failed to load idle texture " + idleTexturePath + ", trying fallback...");
        }

        if (walkImg == null) {
            try { walkImg = FXGL.image("monster/Fox/Fox_walk_with_shadow.png"); } catch (Exception ignored) {}
        }
        if (idleImg == null) {
            try { idleImg = FXGL.image("monster/Fox/Fox_Idle_with_shadow.png"); } catch (Exception ignored) {}
        }
        if (walkImg == null) {
            try { walkImg = FXGL.image("monster/Boar/Boar_Walk_with_shadow.png"); } catch (Exception ignored) {}
        }
        if (idleImg == null) {
            try { idleImg = FXGL.image("monster/Boar/Boar_Idle_with_shadow.png"); } catch (Exception ignored) {}
        }

        if (walkImg != null && idleImg != null) {
            int frameW = 32;
            int frameH = 32;
            int totalColumns = 6;

            setup(walkImg, idleImg, totalColumns, frameW, frameH, Duration.seconds(0.8), Duration.seconds(0.2),
                  0, 5,   // walkDown
                  6, 11,  // walkUp
                  12, 17, // walkLeft
                  18, 23, // walkRight
                  0, 0,   // idleDown
                  6, 6,   // idleUp
                  12, 12, // idleLeft
                  18, 18  // idleRight
            );
            lastWalkAnim = getAnimWalkDown();
        } else {
            useFallback = true;
        }
    }

    @Override
    public void onAdded() {
        physics = entity.getComponentOptional(PhysicsComponent.class).orElse(null);

        if (entity != null) {
            entity.getTransformComponent().setScaleOrigin(new javafx.geometry.Point2D(16.0, 16.0));
            entity.getTransformComponent().setRotationOrigin(new javafx.geometry.Point2D(16.0, 16.0));
            entity.setScaleX(1.8);
            entity.setScaleY(1.8);
        }

        if (useFallback) {
            Rectangle box = new Rectangle(32, 32);
            Color color;
            if ("Fox".equalsIgnoreCase(monsterType)) {
                color = Color.ORANGE;
            } else if ("Deer".equalsIgnoreCase(monsterType)) {
                color = Color.BROWN;
            } else if ("Hare".equalsIgnoreCase(monsterType)) {
                color = Color.LIGHTGRAY;
            } else {
                color = Color.RED;
            }
            box.setFill(color);
            box.setStroke(Color.BLACK);
            box.setStrokeWidth(1);
            entity.getViewComponent().addChild(box);
        } else {
            super.onAdded();
        }
    }

    @Override
    public void onUpdate(double tpf) {
        if (useFallback) {
            return;
        }
        double vx = 0;
        double vy = 0;
        if (physics != null) {
            vx = physics.getVelocityX();
            vy = physics.getVelocityY();
        }

        AnimationChannel targetAnim;
        if (vx == 0 && vy == 0) {
            if (lastWalkAnim == getAnimWalkDown()) targetAnim = getAnimIdleDown();
            else if (lastWalkAnim == getAnimWalkUp()) targetAnim = getAnimIdleUp();
            else if (lastWalkAnim == getAnimWalkLeft()) targetAnim = getAnimIdleLeft();
            else targetAnim = getAnimIdleRight();
        } else {
            if (Math.abs(vx) > Math.abs(vy)) {
                if (vx < 0) {
                    targetAnim = getAnimWalkLeft();
                } else {
                    targetAnim = getAnimWalkRight();
                }
            } else {
                if (vy > 0) {
                    targetAnim = getAnimWalkDown();
                } else {
                    targetAnim = getAnimWalkUp();
                }
            }
            lastWalkAnim = targetAnim;
        }

        setAnimationChannel(targetAnim);
    }
}