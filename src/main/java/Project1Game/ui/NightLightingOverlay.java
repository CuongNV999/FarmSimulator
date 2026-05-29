package Project1Game.ui;

import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;

/**
 * Custom canvas overlay for dynamic line-of-sight night lighting.
 * It uses the MULTIPLY blend mode on the Node itself to overlay a dark tint,
 * while allowing white/warm light sources to shine through at full brightness.
 */
public class NightLightingOverlay extends Canvas {

    private boolean enabled = true;

    public NightLightingOverlay(double width, double height) {
        super(width, height);
        // Ensure it doesn't block player inputs or click events
        setMouseTransparent(true);
        // Set the blend mode of the node to MULTIPLY to multiply the game colors below
        setBlendMode(BlendMode.MULTIPLY);
        // Bind the visibility of this node to whether it actually has opacity
        visibleProperty().bind(opacityProperty().greaterThan(0.01));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            visibleProperty().unbind();
            setVisible(false);
        } else {
            visibleProperty().bind(opacityProperty().greaterThan(0.01));
        }
    }

    /**
     * Updates the lighting overlay drawing.
     * @param playerCenter World coordinates of the player center.
     * @param direction Player's facing/movement direction vector.
     */
    public void update(Point2D playerCenter, Point2D direction) {
        if (!enabled || !isVisible()) {
            return;
        }

        double w = getWidth();
        double h = getHeight();
        GraphicsContext gc = getGraphicsContext2D();

        // 1. Clear the canvas
        gc.clearRect(0, 0, w, h);

        // 2. Convert player's world position to screen/viewport coordinates
        double viewportX = FXGL.getGameScene().getViewport().getX();
        double viewportY = FXGL.getGameScene().getViewport().getY();
        double cx = playerCenter.getX() - viewportX;
        double cy = playerCenter.getY() - viewportY;

        // 3. Fill the entire canvas with the dark night overlay color (pitch black)
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);

        // 4. Draw the light sources using default blending (SRC_OVER) to overlay light onto the dark background
        
        // --- A. Draw Ambient Circle ---
        double ambientRadius = 100.0;
        RadialGradient ambientGrad = new RadialGradient(
                0, 0,
                cx, cy,
                ambientRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(1.0, 0.96, 0.88, 1.0)),
                new Stop(0.4, Color.color(1.0, 0.96, 0.88, 0.65)),
                new Stop(1.0, Color.color(1.0, 0.96, 0.88, 0.0))
        );
        gc.setFill(ambientGrad);
        gc.fillOval(cx - ambientRadius, cy - ambientRadius, ambientRadius * 2, ambientRadius * 2);

        // --- B. Draw Flashlight Cone ---
        if (direction == null || (direction.getX() == 0 && direction.getY() == 0)) {
            direction = new Point2D(0, 1); // Default to pointing down
        }

        // Invert Y coordinate because JavaFX Y-axis is inverted (increases downwards)
        double centerAngle = Math.toDegrees(Math.atan2(-direction.getY(), direction.getX()));


        // Layer 1: Outer cone (Wider, shorter range, softer transition for light scattering)
        double outerConeAngle = 70.0;
        double outerRadius = 220.0;
        RadialGradient outerFlashlightGrad = new RadialGradient(
                0, 0,
                cx, cy,
                outerRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(1.0, 0.98, 0.9, 0.45)),
                new Stop(0.5, Color.color(1.0, 0.98, 0.9, 0.25)),
                new Stop(1.0, Color.color(1.0, 0.98, 0.9, 0.0))
        );
        gc.setFill(outerFlashlightGrad);
        gc.fillArc(cx - outerRadius, cy - outerRadius, outerRadius * 2, outerRadius * 2,
                centerAngle - outerConeAngle / 2.0, outerConeAngle, ArcType.ROUND);

        // Layer 2: Inner cone (Narrower, longer range, brighter core flashlight beam)
        double innerConeAngle = 38.0;
        double innerRadius = 340.0;
        RadialGradient innerFlashlightGrad = new RadialGradient(
                0, 0,
                cx, cy,
                innerRadius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(1.0, 0.98, 0.9, 1.0)),
                new Stop(0.6, Color.color(1.0, 0.98, 0.9, 0.5)),
                new Stop(1.0, Color.color(1.0, 0.98, 0.9, 0.0))
        );
        gc.setFill(innerFlashlightGrad);
        gc.fillArc(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2,
                centerAngle - innerConeAngle / 2.0, innerConeAngle, ArcType.ROUND);
    }
}
