package com.sanchezparralabs.maze;

import java.util.*;

public class MazeBuilder {

    public void print(char[][] maze) {
        for(char[] row : maze) {
            for(char c : row) {
                System.out.printf(" %c", c);
            }
            System.out.println();
        }
    }
    Random rnd = new Random();
    public char[][] build(int width, int height) {
        width = width + 1;
        height = height + 1;
        char[][] maze = new char[height][width];
        for (int y = 0; y < height; y++) {
            Arrays.fill(maze[y], '#');
        }
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        int sx = 1;
        int sy = 1;

        Stack<Integer> stx = new Stack<>();
        Stack<Integer> sty = new Stack<>();
        stx.add(sx);
        sty.add(sy);
        while (!stx.isEmpty()) {
            int x = stx.pop();
            int y = sty.pop();
            maze[y][x] = '.';
            List<int[]> valid = new ArrayList<>(4);
            for(int[] dir : dirs) {
                int nx = x + dir[0] * 2;
                int ny = y + dir[1] * 2;
                if (nx > 0 && ny > 0 && nx < width - 1 && ny < height - 1 && maze[ny][nx] == '#') {
                    valid.add(dir);
                }
            }
            if(valid.size() > 0) {
                int[] dir = valid.get(rnd.nextInt(valid.size()));
                int nx = x + dir[0];
                int ny = y + dir[1];
                stx.add(x);
                sty.add(y);
                maze[ny][nx] = '.';
                nx += dir[0];
                ny += dir[1];
                stx.add(nx);
                sty.add(ny);
            }
        }

        return maze;
    }
}
