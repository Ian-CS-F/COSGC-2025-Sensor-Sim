//import peasy.*;
//import processing.core.PApplet;
//
//import java.util.ArrayList;
//
//public class RobotVisual extends PApplet {
//
//    public static void main(String[] args) {
//        PApplet.main("RobotVisual");
//    }
//
//    PeasyCam cam;
//
//    // --- GRID / TERRAIN SETTINGS ---
//    int cols = 75;
//    int rows = 200;
//    float scale = 15;       // distance between pillars
//    float maxHeight = 200;  // max terrain height
//    float[][] heights;
//
//    // --- ROBOT SETTINGS ---
//    int tarX = cols/2>50?50:cols/2;
//    int tarY = rows/2>50?50:rows/2;
//    int direction = 1;             // 0=north,1=east,2=south,3=west
//    int[] robotCoords = {0, 0};
//    float robotHeight;
//
//    // --- SENSOR ARRAYS ---
//    int[][] sensedO, sensedD;
//
//    // --- PATHFINDING ---
//    ArrayList<Node> open = new ArrayList<>();
//    ArrayList<Node> closed = new ArrayList<>();
//    Node path = new Node(0, 0);
//
//    @Override
//    public void settings() {
//        size(displayWidth, displayHeight, P3D);
//    }
//
//    @Override
//    public void setup() {
//        println(System.getProperty("java.library.path"));
//
//        cam = new PeasyCam(this, 800);
//        cam.setMinimumDistance(200);
//        cam.setMaximumDistance(3000);
//
//        robotHeight = scale * 0.9f;
//
//        sensedO = new int[rows][cols];
//        sensedD = new int[rows][cols];
//
//        // Generate smooth terrain
//        heights = new float[rows][cols];
//        for (int x = 0; x < cols; x++) {
//            for (int y = 0; y < rows; y++) {
//                heights[y][x] = noise(x * 0.075f, y * 0.075f) * maxHeight;
//            }
//        }
//
//        approxSensor();
//    }
//
//    @Override
//    public void draw() {
//        background(135, 206, 235); // sky blue
//        lights();
//
//        pushMatrix();
//        // center terrain around origin
//        translate(-cols * scale, -rows * scale / 2, 0);
//
//        drawTerrain();
//        visualizeSensor();
//        followPathByParent();
//        drawRobot();
////        delay(100);
//        if(robotCoords[0] == tarX && robotCoords[1] == tarY) {
//            tarX=(int)random(cols);
//            tarY=(int)random(rows);
//            while(sqrt(pow(robotCoords[0]-tarX,2) +pow(robotCoords[1] -tarY,2))>75) {
//                tarX=(int)random(cols);
//                tarY=(int)random(rows);
//            }
//        }
//        popMatrix();
//    }
//
//    // -------------------- TERRAIN --------------------
//    void drawTerrain() {
//        for (int x = 0; x < cols; x++) {
//            for (int y = 0; y < rows; y++) {
//                float h = heights[y][x];
//                float o = sensedO[y][x];
//
//                pushMatrix();
//                translate(x * scale, y * scale, h / 2);
//
//                // terrain pillar
//                fill(map(h, 0, maxHeight, 50, 200), 100, 200);
//                box(scale * 0.9f, scale * 0.9f, h);
//
//                // sensor visualization
//                translate(cols * scale, 0, -h / 2 + o / 2);
//                fill(200, map(o, 0, maxHeight, 50, 200), 100);
//                box(scale * 0.9f, scale * 0.9f, o);
//
//                popMatrix();
//            }
//        }
//
//        // draw path
//        Node node = path;
//        while (node != null && node.parent != null) {
//            int px = node.getX();
//            int py = node.getY();
//
//            pushMatrix();
//            translate(px * scale, py * scale, heights[py][px]);
//            fill(100, 200, 100); // path color
//            box(scale * 0.9f, scale * 0.9f, 0);
//            popMatrix();
//
//            node = node.parent;
//        }
//    }
//
//    // -------------------- ROBOT --------------------
//    void drawRobot() {
//        pushMatrix();
//
//        int rx = robotCoords[0];
//        int ry = robotCoords[1];
//        float rh = heights[ry][rx];
//
//        translate(rx * scale, ry * scale, rh + robotHeight / 2);
//
//        // rotate robot to heading
//        rotateZ(-direction * HALF_PI);
//
//        // robot body
//        fill(0, 255, 0);
//        box(scale * 0.9f);
//
//        // forward indicator
//        fill(255, 0, 0);
//        translate(scale * 0.5f, 0, 0);
//        box(scale * 0.3f);
//
//        popMatrix();
//    }
//
//    // -------------------- SENSOR --------------------
//    void visualizeSensor() {
//        // reset sensor arrays
////        sensedO = new int[rows][cols];
//        sensedD = new int[rows][cols];
//
//        int rX = robotCoords[0];
//        int rY = robotCoords[1];
//        float robotZ = heights[rY][rX] + robotHeight;
//        float headingRad = direction * HALF_PI;
//
//        float[] verticalAngles = {radians(-20),radians(-15),radians(-10), radians(-5), 0, radians(5), radians(10), radians(15), radians(20)};
//        float maxDistance = 50;
//        int horizontalSteps = 180;
//        float stepSize = 0.5f;
//
//        for (float pitch : verticalAngles) {
//            boolean isUpward = true;
//            boolean isDownward = pitch < 0;
//
//            for (int s = 0; s < horizontalSteps; s++) {
//                float yaw = headingRad + map(s, 0, horizontalSteps - 1, -PI/2, PI/2);
//                boolean hit = false;
//
//                for (float d = 0; d < maxDistance; d += stepSize) {
//                    float checkX = rX + cos(yaw) * d;
//                    float checkY = rY - sin(yaw) * d;
//
//                    int gx = floor(checkX);
//                    int gy = floor(checkY);
//
//                    if (gx < 0 || gx >= cols || gy < 0 || gy >= rows) break;
//
//                    float terrainHeight = heights[gy][gx];
//                    float rayHeight = robotZ + tan(pitch) * d * scale;
//
//                    if (!hit) {
//                        if (isUpward && terrainHeight > rayHeight) hit = true;
//                        if (isDownward && terrainHeight < rayHeight) hit = true;
//                    }
//
//                    if (hit) {
//                        if (isUpward&&(int)rayHeight>sensedO[gy][gx]) {
//                            sensedO[gy][gx] = (int)rayHeight;
//                        }
//                        if (isDownward) sensedD[gy][gx] += 1;
//                        break;
//                    }
//                }
//            }
//        }
//
//        path = findPath();
//    }
//
//    void followPathByParent() {
//        if (path == null || path.parent == null) return; // reached goal or no path
//
//        // next node toward target
//        Node next = path;
//        while (next.parent.parent != null) {
//            next = next.parent;
//        }
//        int rx = robotCoords[0];
//        int ry = robotCoords[1];
//        int nx = next.getX();
//        int ny = next.getY();
//
//        // update direction
//        if (nx > rx) direction = 0;      // east
//        else if (nx < rx) direction = 2; // west
//        else if (ny > ry) direction = 3; // south
//        else if (ny < ry) direction = 1; // north
//
//        // move robot
//        robotCoords[0] = nx;
//        robotCoords[1] = ny;
//    }
//
//    // -------------------- SENSOR APPROXIMATION --------------------
//    void approxSensor() {
//        visualizeSensor();
//    }
//
//    // -------------------- ROBOT CONTROL --------------------
//    @Override
//    public void keyPressed() {
//        if (key == 'a') direction = (direction + 1) % 4; // turn left
//        if (key == 'd') direction = (direction + 3) % 4; // turn right
//        if (key == 'w') moveRobot(direction == 1 || direction == 3 ? -1 : 1);
//        if (key == 's') moveRobot(direction == 1 || direction == 3 ? 1 : -1);
//
//        approxSensor(); // update sensors after move
//    }
//
//    void moveRobot(int dir) {
//        float headingRad = direction * HALF_PI;
//        float newX = robotCoords[0] + cos(headingRad) * dir;
//        float newY = robotCoords[1] + sin(headingRad) * dir;
//
//        if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) return;
//
//        robotCoords[0] = (int)newX;
//        robotCoords[1] = (int)newY;
//    }
//
//    // -------------------- PATHFINDING --------------------
//    Node findPath() {
//        open.clear();
//        closed.clear();
//        open.add(new Node(robotCoords[0], robotCoords[1]));
//
//        while (!open.isEmpty()) {
//            Node q = open.remove(getLowestF(open));
//            ArrayList<Node> successors = genSuccessors(q);
//
//            for (Node succ : successors) {
//                if (succ.getX() == tarX && succ.getY() == tarY) return succ;
//
//                int x = succ.getX();
//                int y = succ.getY();
//                float currentHeight = heights[q.getY()][q.getX()];
//                float nextHeight = heights[y][x];
//                double heightCost = abs(nextHeight - currentHeight);
//                double g = q.getG() + 1+heightCost*3;//sensedO[y][x]
//                double h = dist(x, y, tarX, tarY); // Euclidean distance
//                double f = g + h;
//                succ.setFGH(f, g, h);
//
//                double fInOpen = lowestFAtCoords(x, y, open);
//                double fInClosed = lowestFAtCoords(x, y, closed);
//
//                if (f < fInOpen && f < fInClosed) open.add(succ);
//            }
//            closed.add(q);
//        }
//
//        return null;
//    }
//
//    int getLowestF(ArrayList<Node> list) {
//        double min = Double.MAX_VALUE;
//        int minIndex = 0;
//        for (int i = 0; i < list.size(); i++) {
//            if (list.get(i).getF() < min) {
//                min = list.get(i).getF();
//                minIndex = i;
//            }
//        }
//        return minIndex;
//    }
//
//    ArrayList<Node> genSuccessors(Node q) {
//        ArrayList<Node> successors = new ArrayList<>();
//        for (int dx = -1; dx <= 1; dx++) {
//            for (int dy = -1; dy <= 1; dy++) {
//                if (abs(dx) + abs(dy) != 1) continue; // only 4-neighbors
//                int nx = q.getX() + dx;
//                int ny = q.getY() + dy;
//                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && sensedO[ny][nx] != -1) {
//                    successors.add(new Node(nx, ny, q));
//                }
//            }
//        }
//        return successors;
//    }
//
//    double lowestFAtCoords(int x, int y, ArrayList<Node> list) {
//        double min = Double.MAX_VALUE;
//        for (Node n : list) {
//            if (n.getX() == x && n.getY() == y) min = n.getF()<min?n.getF():min;
//        }
//        return min;
//    }
//
//    // -------------------- NODE CLASS --------------------
//    public class Node {
//        int x, y;
//        double f, g, h;
//        Node parent;
//
//        Node(int x, int y) {
//            this(x, y, null);
//        }
//
//        Node(int x, int y, Node parent) {
//            this.x = x;
//            this.y = y;
//            this.parent = parent;
//        }
//
//        void setFGH(double f, double g, double h) {
//            this.f = f;
//            this.g = g;
//            this.h = h;
//        }
//
//        int getX() { return x; }
//        int getY() { return y; }
//        double getF() { return f; }
//        double getG() { return g; }
//        double getH() { return h; }
//        Node getParent() { return parent; }
//        void setParent(Node parent) { this.parent = parent; }
//    }
//
//}
import peasy.*;
import processing.core.PApplet;

import java.util.ArrayList;

public class RobotVisual extends PApplet {

    public static void main(String[] args) {
        PApplet.main("RobotVisual");
    }

    PeasyCam cam;
    boolean map,sensors,live;
    // --- GRID / TERRAIN SETTINGS ---
    int cols = 75;
    int rows = 75;
    int del =75;
    int count=0;
    float smoothing = 0.03f;
    float scale = 15;       // distance between pillars
    float maxHeight = 600;  // max terrain height
    float[][] heights;

    // --- ROBOT SETTINGS ---
    int tarX = cols/2>50?50:cols/2;
    int tarY = rows/2>50?50:rows/2;
    int direction = 1;             // 0=north,1=east,2=south,3=west
    int[] robotCoords = {0, 0};
    float robotHeight;

    // --- SENSOR ARRAYS ---
    int[][] sensedO, sensedD;

    // --- PATHFINDING ---
    ArrayList<Node> open = new ArrayList<>();
    ArrayList<Node> closed = new ArrayList<>();
    Node path = new Node(0, 0);

    @Override
    public void settings() {
        size(displayWidth, displayHeight, P3D);
    }

    @Override
    public void setup() {
        println(System.getProperty("java.library.path"));
//        size(800, 600, P3D);

        cam = new PeasyCam(this, 800);
        cam.setMinimumDistance(200);
        cam.setMaximumDistance(3000);

        robotHeight = scale * 0.9f;

        sensedO = new int[rows][cols];
        sensedD = new int[rows][cols];

        // Generate smooth terrain
        heights = new float[rows][cols];
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                heights[y][x] = noise(x * smoothing, y * smoothing) * maxHeight;
            }
        }

        approxSensor();
    }

    @Override
    public void draw() {
        background(135, 206, 235); // sky blue
        lights();

        pushMatrix();
        // center terrain around origin
        translate(-cols * scale/2, -rows * scale / 2, 0);
        drawTerrain();
        visualizeSensor();
        if(count%10==0)path=findPath();
        followPathByParent();
        drawRobot();
        delay(del);
        if(robotCoords[0] == tarX && robotCoords[1] == tarY) {
            tarX=(int)random(cols);
            tarY=(int)random(rows);
            while(sqrt(pow(robotCoords[0]-tarX,2) +pow(robotCoords[1] -tarY,2))>75) {
                tarX=(int)random(cols);
                tarY=(int)random(rows);
            }
        }
        popMatrix();
    }

    // -------------------- TERRAIN --------------------
    void drawTerrain() {
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                float h = heights[y][x];
                float o = sensedO[y][x];
                float d = sensedD[y][x];

                pushMatrix();
                translate(x * scale, y * scale, h / 2);

                // terrain pillar
                if(map) {
                    fill(map(h, 0, maxHeight, 50, 200), 100, 200,50);
                    box(scale * 0.9f, scale * 0.9f, h);
                }
                // sensor visualization
                translate(0, 0, -h / 2 + o / 2);//cols * scale
                if(sensors) {
                    fill(200, map(o, 0, maxHeight, 50, 200), 100);
                    box(scale * 0.9f, scale * 0.9f, o);
                }
                translate(0, 0, -o / 2 + d / 2);//-2 * cols * scale
                if(live) {
                    fill(200, 100, map(d, 0, maxHeight, 50, 200),125);
                    box(scale * 0.9f, scale * 0.9f, d);
                }

                popMatrix();
            }
        }

        // draw path
        Node node = path;
        while (node != null && node.parent != null) {
            int px = node.getX();
            int py = node.getY();

            pushMatrix();
            translate(px * scale, py * scale, heights[py][px]);
            fill(100, 200, 100); // path color
            box(scale * 0.9f, scale * 0.9f, 0);
            popMatrix();

            node = node.parent;
        }
    }

    // -------------------- ROBOT --------------------
    void drawRobot() {
        pushMatrix();

        int rx = robotCoords[0];
        int ry = robotCoords[1];
        float rh = heights[ry][rx];

        translate(rx * scale, ry * scale, rh + robotHeight / 2);

        // rotate robot to heading
        rotateZ(-direction * HALF_PI);

        // robot body
        fill(0, 255, 0);
        box(scale * 0.9f);

        // forward indicator
        fill(255, 0, 0);
        translate(scale * 0.5f, 0, 0);
        box(scale * 0.3f);

        popMatrix();
    }

    // -------------------- SENSOR --------------------
    void visualizeSensor() {
        // reset sensor arrays
//        sensedO = new int[rows][cols];
        sensedD = new int[rows][cols];

        int rX = robotCoords[0];
        int rY = robotCoords[1];
        float robotZ = heights[rY][rX] + robotHeight;
        float headingRad = direction * HALF_PI;

        float[] verticalAngles = {radians(-20),radians(-15),radians(-10), radians(-5), 0, radians(5), radians(10), radians(15), radians(20)};
        float maxDistance = 50;
        int horizontalSteps = 180;
        float stepSize = 0.5f;

        for (float pitch : verticalAngles) {
            boolean isUpward = true;
            boolean isDownward = pitch < 0;

            for (int s = 0; s < horizontalSteps; s++) {
                float yaw = headingRad + map(s, 0, horizontalSteps - 1, -PI/2, PI/2);
                boolean hit = false;
                for (float d = 0; d < maxDistance; d += stepSize) {
                    float checkX = rX + cos(yaw) * d;
                    float checkY = rY - sin(yaw) * d;

                    int gx = floor(checkX);
                    int gy = floor(checkY);

                    if (gx < 0 || gx >= cols || gy < 0 || gy >= rows) break;

                    float terrainHeight = heights[gy][gx];
                    float rayHeight = robotZ + tan(pitch) * d * scale;

                    if (!hit) {
                        if (isUpward && terrainHeight > rayHeight) hit = true;
                        if (isDownward && terrainHeight < rayHeight) hit = true;
                    }

                    if (hit) {
                        if (isUpward&&(int)rayHeight>sensedO[gy][gx]) {
                            sensedO[gy][gx] = (int)rayHeight;
                        }
                        break;
                    }
                }
            }
        }
        for (float pitch : verticalAngles) {
            boolean isUpward = true;
            boolean isDownward = pitch < 0;

            for (int s = 0; s < horizontalSteps; s++) {
                float yaw = headingRad + map(s, 0, horizontalSteps - 1, -PI/2, PI/2);
                boolean hit = false;
                for (float d = 0; d < maxDistance; d += stepSize) {
                    float checkX = rX + cos(yaw) * d;
                    float checkY = rY - sin(yaw) * d;

                    int gx = floor(checkX);
                    int gy = floor(checkY);

                    if (gx < 0 || gx >= cols || gy < 0 || gy >= rows) break;

                    float terrainHeight = heights[gy][gx];
                    float rayHeight = robotZ + tan(pitch) * d * scale;

                    if (!hit) {
                        if (isUpward && terrainHeight > rayHeight) hit = true;
                        if (isDownward && terrainHeight < rayHeight) hit = true;
                    }

                    if (hit) {
                        if (isUpward&&(int)rayHeight<heights[gy][gx]) {
                            sensedD[gy][gx] = (int)rayHeight;
                        }
                    }
                }
            }
        }

//        path = findPath();
    }

    void followPathByParent() {
        if (path == null || path.parent == null) return; // reached goal or no path

        // next node toward target
        Node next = path;
        while (next.parent.parent != null) {
            next = next.parent;
        }
        int rx = robotCoords[0];
        int ry = robotCoords[1];
        int nx = next.getX();
        int ny = next.getY();

        // update direction
        if (nx > rx) direction = 0;      // east
        else if (nx < rx) direction = 2; // west
        else if (ny > ry) direction = 3; // south
        else if (ny < ry) direction = 1; // north

        // move robot
        robotCoords[0] = nx;
        robotCoords[1] = ny;
        next.parent = null;
        if(path==null||path.parent==null) path=findPath();
        count++;
    }

    // -------------------- SENSOR APPROXIMATION --------------------
    void approxSensor() {
        visualizeSensor();
    }

    // -------------------- ROBOT CONTROL --------------------
    @Override
    public void keyPressed() {
        if (key == 'a') direction = (direction + 1) % 4; // turn left
        if (key == 'd') direction = (direction + 3) % 4; // turn right
        if (key == 'w') moveRobot(direction == 1 || direction == 3 ? -1 : 1);
        if (key == 's') moveRobot(direction == 1 || direction == 3 ? 1 : -1);
        if(key == 'm')map=!map;
        if(key=='l')live=!live;
        if(key=='r')sensors=!sensors;

        approxSensor(); // update sensors after move
    }

    void moveRobot(int dir) {
        float headingRad = direction * HALF_PI;
        float newX = robotCoords[0] + cos(headingRad) * dir;
        float newY = robotCoords[1] + sin(headingRad) * dir;

        if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) return;

        robotCoords[0] = (int)newX;
        robotCoords[1] = (int)newY;
    }

    // -------------------- PATHFINDING --------------------
    Node findPath() {
        open.clear();
        closed.clear();
        open.add(new Node(robotCoords[0], robotCoords[1]));

        while (!open.isEmpty()) {
            Node q = open.remove(getLowestF(open));
            ArrayList<Node> successors = genSuccessors(q);

            for (Node succ : successors) {
                if (succ.getX() == tarX && succ.getY() == tarY) return succ;

                int x = succ.getX();
                int y = succ.getY();
                float currentHeight = heights[q.getY()][q.getX()];
                float nextHeight = heights[y][x];
                double heightCost = abs(nextHeight - currentHeight);
                double g = q.getG() + 1+heightCost*3;//sensedO[y][x]
                double h = dist(x, y, tarX, tarY); // Euclidean distance
                double f = g + h;
                succ.setFGH(f, g, h);

                double fInOpen = lowestFAtCoords(x, y, open);
                double fInClosed = lowestFAtCoords(x, y, closed);

                if (f < fInOpen && f < fInClosed) open.add(succ);
            }
            closed.add(q);
        }

        return null;
    }

    int getLowestF(ArrayList<Node> list) {
        double min = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getF() < min) {
                min = list.get(i).getF();
                minIndex = i;
            }
        }
        return minIndex;
    }

    ArrayList<Node> genSuccessors(Node q) {
        ArrayList<Node> successors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (abs(dx) + abs(dy) != 1) continue; // only 4-neighbors
                int nx = q.getX() + dx;
                int ny = q.getY() + dy;
                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && sensedO[ny][nx] != -1) {
                    successors.add(new Node(nx, ny, q));
                }
            }
        }
        return successors;
    }

    double lowestFAtCoords(int x, int y, ArrayList<Node> list) {
        double min = Double.MAX_VALUE;
        for (Node n : list) {
            if (n.getX() == x && n.getY() == y) min = n.getF()<min?n.getF():min;
        }
        return min;
    }

    // -------------------- NODE CLASS --------------------
    public class Node {
        int x, y;
        double f, g, h;
        Node parent;

        Node(int x, int y) {
            this(x, y, null);
        }

        Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }

        void setFGH(double f, double g, double h) {
            this.f = f;
            this.g = g;
            this.h = h;
        }

        int getX() { return x; }
        int getY() { return y; }
        double getF() { return f; }
        double getG() { return g; }
        double getH() { return h; }
        Node getParent() { return parent; }
        void setParent(Node parent) { this.parent = parent; }
    }

}
