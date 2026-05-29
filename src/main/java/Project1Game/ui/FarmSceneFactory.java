package Project1Game.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;

/**
 * Custom scene factory to hook in our FarmMenu.
 */
public class FarmSceneFactory extends SceneFactory {
    @Override
    public FXGLMenu newMainMenu() {
        return new FarmMenu(MenuType.MAIN_MENU);
    }

    @Override
    public FXGLMenu newGameMenu() {
        return new FarmMenu(MenuType.GAME_MENU);
    }
}
