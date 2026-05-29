package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.core.EntityType;
import javafx.geometry.Point2D;
import java.util.*;

public class AStarPathfinder {

    private static final int TILE_SIZE = 32;

    public static List<Point2D> findPath(Point2D start, Point2D target, double mapW, double mapH) {
        int cols = (int) Math.ceil(mapW / TILE_SIZE);
        int rows = (int) Math.ceil(mapH / TILE_SIZE);

        int startX = (int) (start.getX() / TILE_SIZE);
        int startY = (int) (start.getY() / TILE_SIZE);
        int targetX = (int) (target.getX() / TILE_SIZE);
        int targetY = (int) (target.getY() / TILE_SIZE);

        // Clamp boundaries
        startX = Math.max(0, Math.min(cols - 1, startX));
        startY = Math.max(0, Math.min(rows - 1, startY));
        targetX = Math.max(0, Math.min(cols - 1, targetX));
        targetY = Math.max(0, Math.min(rows - 1, targetY));

        if (startX == targetX && startY == targetY) {
            return Collections.emptyList();
        }

        // Identify obstacles
        boolean[][] obstacles = new boolean[cols][rows];
        List<Entity> collisionEntities = FXGL.getGameWorld().getEntitiesByType(
            EntityType.WALL, EntityType.COLLISION
        );

        for (Entity obstacle : collisionEntities) {
            double minX = obstacle.getX();
            double minY = obstacle.getY();
            double maxX = obstacle.getRightX();
            double maxY = obstacle.getBottomY();

            int startCol = (int) (minX / TILE_SIZE);
            int endCol = (int) Math.ceil(maxX / TILE_SIZE) - 1;
            int startRow = (int) (minY / TILE_SIZE);
            int endRow = (int) Math.ceil(maxY / TILE_SIZE) - 1;

            for (int c = startCol; c <= endCol; c++) {
                for (int r = startRow; r <= endRow; r++) {
                    if (c >= 0 && c < cols && r >= 0 && r < rows) {
                        obstacles[c][r] = true;
                    }
                }
            }
        }

        // Let start and target remain clear
        obstacles[startX][startY] = false;
        obstacles[targetX][targetY] = false;

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node(startX, startY);
        startNode.g = 0;
        startNode.h = heuristic(startX, startY, targetX, targetY);
        startNode.f = startNode.g + startNode.h;
        openSet.add(startNode);
        allNodes.put(startNode.key(), startNode);

        Node targetNode = null;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.closed) continue;
            current.closed = true;

            if (current.x == targetX && current.y == targetY) {
                targetNode = current;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !obstacles[nx][ny]) {
                    String nKey = nx + "," + ny;
                    Node neighbor = allNodes.get(nKey);
                    if (neighbor == null) {
                        neighbor = new Node(nx, ny);
                        allNodes.put(nKey, neighbor);
                    }

                    if (neighbor.closed) continue;

                    double tentativeG = current.g + 1;
                    if (tentativeG < neighbor.g) {
                        neighbor.parent = current;
                        neighbor.g = tentativeG;
                        neighbor.h = heuristic(nx, ny, targetX, targetY);
                        neighbor.f = neighbor.g + neighbor.h;
                        openSet.add(neighbor);
                    }
                }
            }
        }

        if (targetNode == null) {
            return Collections.emptyList();
        }

        List<Point2D> path = new ArrayList<>();
        Node curr = targetNode;
        while (curr != null) {
            path.add(new Point2D(curr.x * TILE_SIZE, curr.y * TILE_SIZE));
            curr = curr.parent;
        }

        Collections.reverse(path);
        return path;
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static class Node {
        final int x, y;
        double g = Double.POSITIVE_INFINITY;
        double h = 0;
        double f = Double.POSITIVE_INFINITY;
        boolean closed = false;
        Node parent = null;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        String key() {
            return x + "," + y;
        }
    }
}
