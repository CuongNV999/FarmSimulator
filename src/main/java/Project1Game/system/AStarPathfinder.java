package Project1Game.system;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import Project1Game.core.EntityType;
import javafx.geometry.Point2D;

import java.util.*;

public class AStarPathfinder {
    private static final double CELL_SIZE = 32.0;

    public static class Node implements Comparable<Node> {
        public int col, row;
        public double g; // path cost
        public double h; // heuristic cost
        public Node parent;

        public Node(int col, int row) {
            this.col = col;
            this.row = row;
        }

        public double getF() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.getF(), other.getF());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return col == node.col && row == node.row;
        }

        @Override
        public int hashCode() {
            return Objects.hash(col, row);
        }
    }

    public static List<Point2D> findPath(Point2D start, Point2D target, double mapWidth, double mapHeight) {
        return findPath(start, target, mapWidth, mapHeight, 64.0); // Default to human NPC height (32x64)
    }

    public static List<Point2D> findPath(Point2D start, Point2D target, double mapWidth, double mapHeight, double entityHeight) {
        int cols = (int) Math.ceil(mapWidth / CELL_SIZE);
        int rows = (int) Math.ceil(mapHeight / CELL_SIZE);
        boolean isTall = entityHeight > 32.0;

        boolean[][] blocked = new boolean[cols][rows];
        double[][] costGrid = new double[cols][rows];
        for (int c = 0; c < cols; c++) {
            Arrays.fill(costGrid[c], 1.0);
        }

        // Get all obstacle entities
        List<Entity> obstacles = FXGL.getGameWorld().getEntitiesByType(EntityType.WALL, EntityType.COLLISION);
        for (Entity obstacle : obstacles) {
            double minX = obstacle.getX();
            double minY = obstacle.getY();
            double maxX = obstacle.getRightX();
            double maxY = obstacle.getBottomY();

            int startCol = Math.max(0, (int) (minX / CELL_SIZE));
            int endCol = Math.min(cols - 1, (int) Math.ceil(maxX / CELL_SIZE) - 1);
            int startRow = Math.max(0, (int) (minY / CELL_SIZE));
            int endRow = Math.min(rows - 1, (int) Math.ceil(maxY / CELL_SIZE) - 1);

            for (int c = startCol; c <= endCol; c++) {
                for (int r = startRow; r <= endRow; r++) {
                    blocked[c][r] = true;
                }
            }
        }

        // Apply obstacle inflation for cost: cells 1 tile away from a blocked tile have cost 15, cells 2 tiles away have cost 5.
        // This keeps the NPC in the middle of roads and away from walls/obstacles.
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (blocked[c][r]) {
                    // Distance 1 neighbors
                    for (int dc = -1; dc <= 1; dc++) {
                        for (int dr = -1; dr <= 1; dr++) {
                            if (dc == 0 && dr == 0) continue;
                            int nc = c + dc;
                            int nr = r + dr;
                            if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                                if (!blocked[nc][nr]) {
                                    costGrid[nc][nr] = Math.max(costGrid[nc][nr], 15.0);
                                }
                            }
                        }
                    }
                    // Distance 2 neighbors
                    for (int dc = -2; dc <= 2; dc++) {
                        for (int dr = -2; dr <= 2; dr++) {
                            if (Math.abs(dc) <= 1 && Math.abs(dr) <= 1) continue; // already handled
                            int nc = c + dc;
                            int nr = r + dr;
                            if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                                if (!blocked[nc][nr]) {
                                    costGrid[nc][nr] = Math.max(costGrid[nc][nr], 5.0);
                                }
                            }
                        }
                    }
                }
            }
        }

        int startCol = Math.max(0, Math.min(cols - 1, (int) Math.round(start.getX() / CELL_SIZE)));
        int startRow = Math.max(0, Math.min(rows - 1, (int) Math.round(start.getY() / CELL_SIZE)));
        int targetCol = Math.max(0, Math.min(cols - 1, (int) Math.round(target.getX() / CELL_SIZE)));
        int targetRow = Math.max(0, Math.min(rows - 1, (int) Math.round(target.getY() / CELL_SIZE)));

        // Guarantee start and target are passable
        blocked[startCol][startRow] = false;
        blocked[targetCol][targetRow] = false;

        // Since the NPC is 32x64, it occupies two vertical cells. Let's make sure the cell below the start and target is also passable
        if (isTall) {
            if (startRow + 1 < rows) blocked[startCol][startRow + 1] = false;
            if (targetRow + 1 < rows) blocked[targetCol][targetRow + 1] = false;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(startCol, startRow);
        startNode.g = 0;
        startNode.h = Math.abs(startCol - targetCol) + Math.abs(startRow - targetRow); // Manhattan distance
        openSet.add(startNode);

        Map<String, Node> allNodes = new HashMap<>();
        allNodes.put(startCol + "," + startRow, startNode);

        Node targetNode = null;

        int[] dCol = {1, -1, 0, 0};
        int[] dRow = {0, 0, 1, -1};

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.col == targetCol && current.row == targetRow) {
                targetNode = current;
                break;
            }

            closedSet.add(current.col + "," + current.row);

            for (int i = 0; i < 4; i++) {
                int nCol = current.col + dCol[i];
                int nRow = current.row + dRow[i];

                if (nCol < 0 || nCol >= cols || nRow < 0 || nRow >= rows) continue;
                if (blocked[nCol][nRow]) continue;
                if (closedSet.contains(nCol + "," + nRow)) continue;

                // Since NPC is 32x64, check if the cell below is also blocked
                if (isTall && nRow + 1 < rows && blocked[nCol][nRow + 1]) continue;

                double movementCost = costGrid[nCol][nRow];
                double tentativeG = current.g + movementCost;

                String key = nCol + "," + nRow;
                Node neighbor = allNodes.get(key);
                if (neighbor == null) {
                    neighbor = new Node(nCol, nRow);
                    neighbor.g = Double.MAX_VALUE;
                    allNodes.put(key, neighbor);
                }

                if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.h = Math.abs(nCol - targetCol) + Math.abs(nRow - targetRow);
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        if (targetNode == null) {
            // No path found
            return Collections.emptyList();
        }

        // Reconstruct path
        List<Point2D> path = new ArrayList<>();
        Node curr = targetNode;
        while (curr != null) {
            path.add(0, new Point2D(curr.col * CELL_SIZE, curr.row * CELL_SIZE));
            curr = curr.parent;
        }

        // Replace the last waypoint with the exact target position
        if (!path.isEmpty()) {
            path.set(path.size() - 1, target);
        }

        return path;
    }
}
