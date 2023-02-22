package com.sanchezparralabs.maze;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class WriteImageType {
    static public void main(String args[]) throws Exception {
        try {
            int width = 100, height = 50;
            String fileName = "maze.png";
            int pxWidth = 10;
            int pxHeight = 10;
            MazeBuilder builder = new MazeBuilder();
            char[][] maze = builder.build(width, height);
            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
            // into integer pixels
            BufferedImage bi = new BufferedImage((width+1) * pxWidth, (height+1) * pxHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D ig2 = bi.createGraphics();

            for(int y = 0; y < maze.length; y++) {
                for (int x = 0; x < maze[0].length; x++) {
                    char c =maze[y][x];
                    Shape shape = new Rectangle(x * 10, y * 10, 10, 10);

                    ig2.setColor(c == '.' ? Color.BLACK : Color.WHITE);
                    ig2.fill(shape);
                }
            }

            Font font = new Font("TimesRoman", Font.BOLD, 20);
            ig2.setFont(font);
            String message = "www.java2s.com!";
            FontMetrics fontMetrics = ig2.getFontMetrics();
            int stringWidth = fontMetrics.stringWidth(message);
            int stringHeight = fontMetrics.getAscent();
            ig2.setPaint(Color.black);
//            ig2.drawString(message, (width - stringWidth) / 2, height / 2 + stringHeight / 4);

//            ImageIO.write(bi, "PNG", new File("c:\\yourImageName.PNG"));
//            ImageIO.write(bi, "JPEG", new File("c:\\yourImageName.JPG"));
//            ImageIO.write(bi, "gif", new File("c:\\yourImageName.GIF"));
//            ImageIO.write(bi, "BMP", new File("c:\\yourImageName.BMP"));
            ImageIO.write(bi, "PNG", new File(fileName));
            System.out.println("Wrote " + fileName + " file");
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }
}
