package com.sanchezparralabs.maze;

/* https://www.demo2s.com/java/javafx-mesh-tutorial-with-examples.html
* Copyright (c) 2016 theKidOfArcrania
        *
        * Permission is hereby granted, free of charge, to any person obtaining a copy
        * of this software and associated documentation files (the "Software"), to deal
        * in the Software without restriction, including without limitation the rights
        * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        * copies of the Software, and to permit persons to whom the Software is
        * furnished to do so, subject to the following conditions:
        *
        * The above copyright notice and this permission notice shall be included in all
        * copies or substantial portions of the Software.
        *
        * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        * SOFTWARE.
        */

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MazeWorld extends Application {
    private static final double WALK_SIN_STRETCH = .3;
    private static final double WALK_SIN_SHIFT = 1.2;
    private static final double TURN_VELOCITY = 90;
    private static final double MOVE_VELOCITY = 300;
    private static final double WALK_CYCLE = 5;
    private static final double WALK_LOWER = 20;
    private static final double WALK_UPPER = 0;

    private static final String textureLoc = "https://www.sketchuptextureclub.com/public/texture_f/slab-marble-emperador-cream-light-preview.jpg";
    private PhongMaterial texturedMaterial = new PhongMaterial();

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static int collidingState(double beforeMin, double beforeMax, double objMin, double objMax,
                                      double move) {
        double min = beforeMin + move;
        double max = beforeMax + move;

        if (max > objMin && objMax > min) //Intersecting
        {
            double maxOutside = max - objMax;
            double minOutside = objMin - min;

            if (move == 0 || maxOutside == minOutside || maxOutside <= 0 && minOutside <= 0)
                return 1; //Receding
            else
                return maxOutside < minOutside ^ move < 0 ? 2 /*Advancing*/ : 1 /*Receding*/;
        } else
            return 0; //No intersection
    }

    private static Box createBox(double x, double y, double z, double width, double height, double depth, Color c) {
        Box cube = new Box(width, height, depth);
        cube.setTranslateX(x + width / 2);
        cube.setTranslateY(y + height / 2);
        cube.setTranslateZ(z + depth / 2);
        cube.setMaterial(new PhongMaterial(c));
        return cube;
    }

    private static void rotate360(Rotate r, Duration cycleDuration) {
        Timeline rotating = new Timeline();
        rotating.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0), new KeyValue(r.angleProperty(), 0)),
                new KeyFrame(cycleDuration, new KeyValue(r.angleProperty(), 360)));
        rotating.setCycleCount(-1);
        rotating.playFromStart();
    }

    private boolean forward = false;
    private boolean backward = false;
    private boolean up = false;
    private boolean down = false;
    private boolean left = false;
    private boolean right = false;
    private double before = -1;
    private double walkFrame = 0;
    private final Sphere cameraBody = new Sphere(50);
    private final Rotate elevate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate heading = new Rotate(0, Rotate.Y_AXIS);
    private final Translate pos = new Translate(200, 50, 200);
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final ArrayList<Shape3D> solidBodies = new ArrayList<>();
    private Label hints;
    Bounds mazeBounds;
    int mazeWidth = 14;
    int mazeDepth = 14;
    int floorSize = 300;
    private char[][] mazeData;

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            Alert support = new Alert(AlertType.ERROR);
            support.setTitle("3D Test");
            support.setContentText("This computer does not support Scene3D.");
            support.showAndWait();
            System.exit(1);
        }

        primaryStage.setResizable(false);

        Image diff = new Image(new FileInputStream("diff.jpg"));
        Image spec = new Image(new FileInputStream("spec.jpg"));
        Image bump = new Image(new FileInputStream("norm.jpg"));
        texturedMaterial.setDiffuseMap(diff);
        texturedMaterial.setSpecularMap(spec);
        texturedMaterial.setBumpMap(bump);

        MazeMesh mesh = new MazeMesh(mazeWidth, mazeDepth, floorSize, 200);
        mazeData = mesh.getMaze();
        MeshView maze = new MeshView(mesh);
        mazeBounds = maze.localToScene(maze.getBoundsInLocal());
        pos.setX(mazeBounds.getMinX() + floorSize * 1.5);
        pos.setZ(mazeBounds.getMinZ() + floorSize * 1.5);
        maze.setMaterial(texturedMaterial);
        addSolidBodies(maze);

        MeshView crystal = new CrystalMesh(6, 50f, 100f, 50f);
        crystal.setTranslateX((mazeWidth / 2 - 1) * floorSize);
        crystal.setTranslateY(100);
        crystal.setTranslateZ((mazeDepth / 2 - 1) * floorSize);
        Rotate r = new Rotate();
        r.setAxis(Rotate.Y_AXIS);
        rotate360(r, Duration.seconds(2));
        crystal.getTransforms().add(r);
        addSolidBodies(crystal);

        PointLight light = new PointLight(Color.WHITE);
        Group cameraGroup = new Group();

        // Create and position camera
        camera.setFieldOfView(78);
        camera.setFarClip(10000);
        camera.setVerticalFieldOfView(false);
        cameraGroup.getChildren().addAll(camera, light, cameraBody);
        cameraGroup.getTransforms().addAll(pos, elevate, heading);
        addSolidBodies(cameraBody);

        hints = new Label("HInts show here");
        hints.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        hints.setTextFill(Color.LIGHTGREEN);


        // Build the Scene Graph
        // Add 2D content
        AnchorPane globalRoot = new AnchorPane();
        Scene scene = new Scene(globalRoot, 1024, 768, true);

        // Add 3D content
        Group root = new Group();
        root.getChildren().addAll(cameraGroup, maze, crystal);
        SubScene sub = new SubScene(root, 1024, 768, true, SceneAntialiasing.BALANCED);
        sub.setCamera(camera);
        sub.toBack();
        sub.setFill(Color.BLUE);

        globalRoot.getChildren().addAll(sub, hints);
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.setFullScreen(false);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            switch (evt.getCode()) {
                case W:
                    forward = true;
                    break;
                case A:
                    left = true;
                    break;
                case S:
                    backward = true;
                    break;
                case D:
                    right = true;
                    break;
                case Q:
                    down = true;
                    break;
                case E:
                    up = true;
                    break;
            }
        });
        primaryStage.addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            switch (evt.getCode()) {
                case W:
                    forward = false;
                    break;
                case A:
                    left = false;
                    break;
                case S:
                    backward = false;
                    break;
                case D:
                    right = false;
                    break;
                case Q:
                    down = false;
                    break;
                case E:
                    up = false;
                    break;
            }
        });
        AnimationTimer tick = new AnimationTimer() {

            @Override
            public void handle(long xx) {
                double now = System.currentTimeMillis() * .001;
                if (before != -1)
                    act(now - before);
                before = now;
            }

        };
        tick.start();
    }

    private void act(double time) {
        if (left ^ right) {
            double angle = heading.getAngle();
            double newAngle = angle + time * TURN_VELOCITY * (right ? 1 : -1);
            heading.setAngle(newAngle - Math.floor(newAngle / 360) * 360);
            elevate.setAxis(heading.transform(Rotate.X_AXIS));
        }

        if (forward ^ backward) {
            double sin = Math.sin(Math.toRadians(heading.getAngle()));
            double cos = Math.cos(Math.toRadians(heading.getAngle()));
            double walkAngle = walkFrame * Math.PI * 2;
            double dist = (Math.sin(walkAngle) * WALK_SIN_STRETCH + WALK_SIN_SHIFT) * MOVE_VELOCITY * time
                    * (forward ? 1 : -1);

            if (move(cameraBody, pos, new Point3D(sin * dist, 0, cos * dist))) {
                camera.setTranslateY((Math.sin(walkAngle) + 1.0) * (WALK_UPPER - WALK_LOWER) + WALK_LOWER);
                walkFrame += time * (forward ? 1 : -1);
                walkFrame -= Math.floor(walkFrame / WALK_CYCLE) * WALK_CYCLE;
            }
        }

        if (up ^ down) {
            double angle = elevate.getAngle();
            double newAngle = angle + time * TURN_VELOCITY * (up ? 1 : -1);
            elevate.setAngle(Math.min(90, Math.max(-90, newAngle)));
        }
    }

    private void addSolidBodies(Node body) {
        ArrayList<Node> tests = new ArrayList<>();
        tests.add(body);
        while (!tests.isEmpty()) {
            Node object = tests.remove(tests.size() - 1);
            if (object instanceof Shape3D)
                solidBodies.add((Shape3D) object);
            else if (object instanceof Parent)
                for (Node child : ((Parent) object).getChildrenUnmodifiable())
                    tests.add(child);
        }
    }

    private boolean move(Shape3D mover, Translate pos, Point3D vector) {
        double oldX = pos.getX(), oldY = pos.getY(), oldZ = pos.getZ();

        int cx = (int) Math.floor((oldX - mazeBounds.getMinX()) / floorSize);
        int cz = (int) Math.floor((oldZ - mazeBounds.getMinZ()) / floorSize);

        double dx = ((mazeWidth / 2 - 1) * floorSize) - oldX;
        double dz = ((mazeDepth / 2 - 1) * floorSize) - oldZ;
        double dist = Math.sqrt((dx * dx) + (dz * dz)) / 300;
        hints.setText(String.format("%3.1f %d %d", dist, cx, cz));

        List<Bounds> walls = new ArrayList<>(4);
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        for (int[] dir : dirs) {
            int x = dir[0];
            int z = dir[1];
            if (mazeData[cz + z][cx + x] == '#') {
                double x1 = (cx + x) * floorSize + mazeBounds.getMinX();
                double z1 = (cz + z) * floorSize + mazeBounds.getMinZ();

                Bounds b = new BoundingBox(x1,
                        0,
                        z1,
                        floorSize,
                        200,
                        floorSize
                );
                walls.add(b);
            }
        }
//
//        if (solidBodies.contains(mover)) {
        Bounds moverBounds = mover.localToScene(mover.getBoundsInLocal());
        for (Bounds otherBounds : walls) {
//            for (Shape3D other : solidBodies) {
//                if (other == mover)
//                    continue;
//                Bounds otherBounds = other.localToScene(other.getBoundsInLocal());
            int xColliding = collidingState(moverBounds.getMinX(), moverBounds.getMaxX(), otherBounds.getMinX(),
                    otherBounds.getMaxX(), vector.getX());
            int yColliding = collidingState(moverBounds.getMinY(), moverBounds.getMaxY(), otherBounds.getMinY(),
                    otherBounds.getMaxY(), vector.getY());
            int zColliding = collidingState(moverBounds.getMinZ(), moverBounds.getMaxZ(), otherBounds.getMinZ(),
                    otherBounds.getMaxZ(), vector.getZ());
//
            if (xColliding + yColliding + zColliding > 3) {
                pos.setX(oldX);
                pos.setY(oldY);
                pos.setZ(oldZ);
                return false;
            }
        }
//        }

        pos.setX(oldX + vector.getX());
        pos.setY(oldY + vector.getY());
        pos.setZ(oldZ + vector.getZ());
        return true;
    }
}