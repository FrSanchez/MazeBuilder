package com.sanchezparralabs.maze;

import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.List;

public class MazeMesh extends TriangleMesh {
    private int width;
    private int height;
    private int floorSize;
    private int wallHeight;
    private MazeBuilder builder = new MazeBuilder();

    public char[][] getMaze() {
        return maze;
    }

    private char[][] maze;

    public MazeMesh(int width, int height, int floorSize, int wallHeight) {
        this.width = width;
        this.height = height;
        this.floorSize = floorSize;
        this.wallHeight = wallHeight;
        buildMesh();
    }

    private void buildMesh() {
        maze = builder.build(width, height);
        builder.print(maze);
        int meshWidth = (maze[0].length + 1) * 2;
        int xm = maze[0].length * floorSize / 2;
        int zm = maze.length * floorSize / 2;

        float[] texCoords = {
                1, 0.33f, // idx t0
                1, 0, // idx t1
                0, 0.33f, // idx t2
                0, 0,  // idx t3

                1, 0.66f, // idx t4
                1, 0.34f, // idx t5
                0, 0.66f, // idx t6
                0, 0.34f,  // idx t7

                1, 1, // idx t8
                1, 0.67f, // idx t9
                0, 1, // idx t10
                0, 0.67f  // idx t11

        };
        this.getTexCoords().setAll(texCoords);

        for (int y = 0; y <= maze.length; y++) {
            for (int x = 0; x <= maze[0].length; x++) {
                float px = x * floorSize - xm;
                float pz = y * floorSize - zm;
                this.getPoints().addAll(px, wallHeight, pz, px, 0f, pz);
                if (y < maze.length && x < maze[0].length) {
                    if (maze[y][x] == '#') {
                        this.getFaces().addAll(
                                (y * meshWidth) + (x + 1) * 2 + 1, 10,
                                ((y + 1) * meshWidth) + (x * 2) + 1, 9,
                                (y * meshWidth) + x * 2 + 1, 8,
                                (y * meshWidth) + (x + 1) * 2 + 1, 10,
                                ((y + 1) * meshWidth) + (x + 1) * 2 + 1, 11,
                                ((y + 1) * meshWidth) + (x * 2) + 1, 9
                        );
                    } else {
                        if (maze[y - 1][x] == '#') {
                            this.getFaces().addAll(
                                    (y * meshWidth) + (x * 2) + 1, 6,
                                    (y * meshWidth) + (x + 1) * 2 + 1, 4,
                                    (y * meshWidth) + (x + 1) * 2, 5,
                                    (y * meshWidth) + (x * 2) + 1, 6,
                                    (y * meshWidth) + (x + 1) * 2, 5,
                                    (y * meshWidth) + (x * 2), 7
                            );
                        }
                        if (maze[y + 1][x] == '#') {
                            this.getFaces().addAll(
                                    ((y+1) * meshWidth) + (x + 1) * 2 + 1, 4,
                                    ((y+1) * meshWidth) + (x * 2) + 1, 6,
                                    ((y+1) * meshWidth) + (x * 2), 7,
                                    ((y+1) * meshWidth) + (x + 1) * 2 + 1, 4,
                                    ((y+1) * meshWidth) + (x * 2), 7,
                                    ((y+1) * meshWidth) + (x + 1) * 2, 5
                            );
                        }
                        if (maze[y][x - 1] == '#') {
                            this.getFaces().addAll(
                                    ((y + 1) * meshWidth) + (x * 2) + 1, 4,
                                    (y * meshWidth) + (x * 2) + 1, 6,
                                    (y * meshWidth) + (x * 2), 7,
                                    ((y + 1) * meshWidth) + (x * 2) + 1, 4,
                                    (y * meshWidth) + (x * 2), 7,
                                    ((y + 1) * meshWidth) + (x * 2), 5
                            );
                        }
                        if (maze[y][x + 1] == '#') {
                            this.getFaces().addAll(
                                    (y * meshWidth) + ((x + 1) * 2) + 1, 4,
                                    ((y + 1) * meshWidth) + ((x + 1) * 2) + 1, 6,
                                    ((y + 1) * meshWidth) + ((x + 1) * 2), 7,
                                    (y * meshWidth) + ((x + 1) * 2) + 1, 4,
                                    ((y + 1) * meshWidth) + ((x + 1) * 2), 7,
                                    (y * meshWidth) + ((x + 1) * 2), 5
                            );
                        }
                        this.getFaces().addAll(
                                (y * meshWidth) + (x + 1) * 2, 2,
                                ((y + 1) * meshWidth) + (x * 2), 1,
                                (y * meshWidth) + x * 2, 0,
                                (y * meshWidth) + (x + 1) * 2, 2,
                                ((y + 1) * meshWidth) + (x + 1) * 2, 3,
                                ((y + 1) * meshWidth) + (x * 2), 1
                        );
                    }
                }
            }
        }

    }
}
