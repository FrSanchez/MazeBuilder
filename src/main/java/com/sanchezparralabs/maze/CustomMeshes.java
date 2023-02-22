package com.sanchezparralabs.maze;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;

public class CustomMeshes extends TriangleMesh {

    public static Mesh createRectangle(float Width, float Height) {
        TriangleMesh mesh = new TriangleMesh();
        float[] points = {
                -Width/2,  Height/2, 0, // idx p0
                -Width/2, -Height/2, 0, // idx p1
                Width/2,  Height/2, 0, // idx p2
                Width/2, -Height/2, 0  // idx p3
        };
        float[] texCoords = {
                1, 1, // idx t0
                1, 0, // idx t1
                0, 1, // idx t2
                0, 0  // idx t3
        };
        /*
         * points:
         * 1      3
         *  -------   texture:
         *  |\    |  1,1    1,0
         *  | \   |    -------
         *  |  \  |    |     |
         *  |   \ |    |     |
         *  |    \|    -------
         *  -------  0,1    0,0
         * 0      2
         *
         * texture[3] 0,0 maps to vertex 2
         * texture[2] 0,1 maps to vertex 0
         * texture[0] 1,1 maps to vertex 1
         * texture[1] 1,0 maps to vertex 3
         *
         * Two triangles define rectangular faces:
         * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
         * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
         */
        int[] faces = {
                2, 2, 1, 1, 0, 0,
                2, 2, 3, 3, 1, 1
        };

        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);

        return mesh;
    }

    public static Mesh createCrystal(int latFaces, float side, float bodyHeight, float pyramidHeight) {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        double totalExterior = 2 * Math.PI;
        double[] regularPoints = new double[latFaces * 2];
        double x = 0;
        double y = 0;
        double angle = 0;
        for (int i = 0; i < latFaces; i++, angle += totalExterior / latFaces) {
            regularPoints[i * 2] = x;
            regularPoints[i * 2 + 1] = y;
            x += Math.cos(angle) * side;
            y += Math.sin(angle) * side;
        }

        double centerX, centerZ;
        if ((latFaces & 1) == 0) {
            centerX = regularPoints[latFaces];
            centerZ = regularPoints[latFaces + 1];
        } else {
            centerX = (regularPoints[latFaces - 1] + regularPoints[latFaces + 1]) / 2;
            centerZ = (regularPoints[latFaces] + regularPoints[latFaces + 2]) / 2;
        }
        centerX = (centerX + regularPoints[0]) / 2;
        centerZ = (centerZ + regularPoints[1]) / 2;

        // Add top pyramid.
        mesh.getPoints().addAll(0, -pyramidHeight - bodyHeight / 2, 0);
        for (int i = 0; i < latFaces; i++) {
            mesh.getPoints().addAll((float) (regularPoints[i * 2] - centerX), -bodyHeight / 2,
                    (float) (regularPoints[i * 2 + 1] - centerZ));
            if (i == 0)
                mesh.getFaces().addAll(0, 0, latFaces, 0, 1, 0);
            else
                mesh.getFaces().addAll(0, 0, i, 0, i + 1, 0);
        }

        // Add body prism
        for (int i = 0; i < latFaces; i++) {
            mesh.getPoints().addAll((float) (regularPoints[i * 2] - centerX), bodyHeight / 2,
                    (float) (regularPoints[i * 2 + 1] - centerZ));
            if (i == 0) {
                mesh.getFaces().addAll(latFaces, 0, latFaces * 2, 0, latFaces + 1, 0);
                mesh.getFaces().addAll(latFaces, 0, latFaces + 1, 0, 1, 0);
            } else {
                int across = i + latFaces;
                mesh.getFaces().addAll(i, 0, across, 0, across + 1, 0);
                mesh.getFaces().addAll(i, 0, across + 1, 0, i + 1, 0);
            }
        }

        // Add bottom pyramid
        int last = latFaces * 2 + 1;
        mesh.getPoints().addAll(0, pyramidHeight + bodyHeight / 2, 0);
        for (int i = 0; i < latFaces; i++)
            if (i == 0)
                mesh.getFaces().addAll(last, 0, latFaces + 1, 0, latFaces * 2, 0);
            else
                mesh.getFaces().addAll(last, 0, i + latFaces + 1, 0, i + latFaces, 0);

        for (int i = 0; i < mesh.getFaces().size(); i += 6)
            mesh.getFaceSmoothingGroups().addAll(0);
        return mesh;
    }
}