package Project1Game.system;

import Project1Game.core.EntityType;
import Project1Game.ui.DialogView;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.entity.Entity;

public class PhysicsSystem {

    // Interface hoặc Callback để báo về Main khi cần đổi tên NPC
    public interface NPCListener {
        void onNPCNear(Entity npc); // Thay đổi từ String name thành Entity npc
        void onNPCAway();
    }

    public static void init(NPCListener listener, DialogView dialogView) {
        FXGL.getPhysicsWorld().setGravity(0, 0);

        // Va chạm cản địa hình
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COLLISION) {
            @Override protected void onCollisionBegin(Entity p, Entity c) {
                System.out.println("Vướng vật cản!");
            }
        });

        // Va chạm NPC Guider
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.GUIDER) {
            @Override protected void onCollisionBegin(Entity p, Entity g) {
                listener.onNPCNear(g); // Truyền Entity của Guider
            }
            @Override protected void onCollisionEnd(Entity p, Entity g) {
                listener.onNPCAway();
                if (dialogView != null) dialogView.hide();
            }
        });

        // THÊM: Va chạm NPC Trader
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.TRADER) {
            @Override protected void onCollisionBegin(Entity p, Entity t) {
                listener.onNPCNear(t); // Truyền Entity của Trader
            }
            @Override protected void onCollisionEnd(Entity p, Entity t) {
                listener.onNPCAway();
                if (dialogView != null) dialogView.hide();
            }
        });

        // Bạn có thể thêm các Handler khác vào đây (Water, v.v.)
    }
}