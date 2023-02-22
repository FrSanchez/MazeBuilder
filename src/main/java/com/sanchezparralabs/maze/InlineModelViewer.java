package com.sanchezparralabs.maze;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class InlineModelViewer extends Application {

    private static final int VIEWPORT_SIZE = 800;

    private static final double MODEL_SCALE_FACTOR = 2;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 150;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE / 2;

    private static final String textureLoc = "https://www.sketchuptextureclub.com/public/texture_f/slab-marble-emperador-cream-light-preview.jpg";

    private Image texture;
    private PhongMaterial texturedMaterial = new PhongMaterial();

    private MeshView meshView = loadMeshView();

    private MeshView loadMeshView() {
//        float[] points = {
//                0, 0, 100,      //P0
//                100, 0, 100,    //P1
//                0, 100, 100,    //P2
//                100, 100, 100,  //P3
//                0, 0, 0,        //P4
//                100, 0, 0,      //P5
//                0, 100, 0,      //P6
//                100, 100, 0     //P7
//        };
//        float[] texCoords = {
//                0.25f, 0,       //T0
//                0.5f, 0,        //T1
//                0, 0.25f,       //T2
//                0.25f, 0.25f,   //T3
//                0.5f, 0.25f,    //T4
//                0.75f, 0.25f,   //T5
//                1, 0.25f,       //T6
//                0, 0.5f,        //T7
//                0.25f, 0.5f,    //T8
//                0.5f, 0.5f,     //T9
//                0.75f, 0.5f,    //T10
//                1, 0.5f,        //T11
//                0.25f, 0.75f,   //T12
//                0.5f, 0.75f     //T13
//        };
//        int[] faces = {
//                5,1,4,0,0,3     //P5,T1 ,P4,T0  ,P0,T3
//                ,5,1,0,3,1,4    //P5,T1 ,P0,T3  ,P1,T4
//                ,0,3,4,2,6,7    //P0,T3 ,P4,T2  ,P6,T7
//                ,0,3,6,7,2,8    //P0,T3 ,P6,T7  ,P2,T8
//                ,1,4,0,3,2,8    //P1,T4 ,P0,T3  ,P2,T8
//                ,1,4,2,8,3,9    //P1,T4 ,P2,T8  ,P3,T9
//                ,5,5,1,4,3,9    //P5,T5 ,P1,T4  ,P3,T9
//                ,5,5,3,9,7,10   //P5,T5 ,P3,T9  ,P7,T10
//                ,4,6,5,5,7,10   //P4,T6 ,P5,T5  ,P7,T10
//                ,4,6,7,10,6,11  //P4,T6 ,P7,T10 ,P6,T11
//                ,3,9,2,8,6,12   //P3,T9 ,P2,T8  ,P6,T12
//                ,3,9,6,12,7,13  //P3,T9 ,P6,T12 ,P7,T13
//        };
//
//        TriangleMesh mesh = new TriangleMesh();
//        mesh.getPoints().setAll(points);
//        mesh.getTexCoords().setAll(texCoords);
//        mesh.getFaces().setAll(faces);

//        return new MeshView(CustomMeshes.createRectangle(40f, 70f));
//        return new MeshView(CustomMeshes.createCrystal(12, 100f, 200f, 100f));
        return new MeshView(new MazeMesh(10, 10, 50, 50));
    }


    private Group buildScene() {
        meshView.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
        meshView.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
        meshView.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
        meshView.setScaleX(MODEL_SCALE_FACTOR);
        meshView.setScaleY(MODEL_SCALE_FACTOR);
        meshView.setScaleZ(MODEL_SCALE_FACTOR);

        return new Group(meshView);
    }

    @Override
    public void start(Stage stage) {
        texture = new Image(textureLoc);
        texturedMaterial.setDiffuseMap(texture);

        Group group = buildScene();

        RotateTransition rotate = rotate3dGroup(group);

        VBox layout = new VBox(
                createControls(rotate),
                createScene3D(group)
        );

        stage.setTitle("Model Viewer");

        Scene scene = new Scene(layout, Color.CORNSILK);
        stage.setScene(scene);
        stage.show();
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene3d.setFill(Color.rgb(10, 10, 40));
        scene3d.setCamera(new PerspectiveCamera());
        return scene3d;
    }

    private VBox createControls(RotateTransition rotateTransition) {
        CheckBox cull = new CheckBox("Cull Back");
        meshView.cullFaceProperty().bind(
                Bindings.when(
                                cull.selectedProperty())
                        .then(CullFace.BACK)
                        .otherwise(CullFace.NONE)
        );
        CheckBox wireframe = new CheckBox("Wireframe");
        meshView.drawModeProperty().bind(
                Bindings.when(
                                wireframe.selectedProperty())
                        .then(DrawMode.LINE)
                        .otherwise(DrawMode.FILL)
        );

        CheckBox rotate = new CheckBox("Rotate");
        rotate.selectedProperty().addListener(observable -> {
            if (rotate.isSelected()) {
                rotateTransition.play();
            } else {
                rotateTransition.pause();
            }
        });

        CheckBox texture = new CheckBox("Texture");
        meshView.materialProperty().bind(
                Bindings.when(
                                texture.selectedProperty())
                        .then(texturedMaterial)
                        .otherwise((PhongMaterial) null)
        );

        VBox controls = new VBox(10, rotate, texture, cull, wireframe);
        controls.setPadding(new Insets(10));
        return controls;
    }

    private RotateTransition rotate3dGroup(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}