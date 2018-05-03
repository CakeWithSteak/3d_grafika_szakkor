package me.dawars.szakkor7;

import processing.core.*;
import processing.opengl.PShader;

public class ModelRender extends PApplet {
    private PShape radioShape;
    private PImage radioTexture;
    private PShader shader;
    private float angle;
    private PImage floor, wall;

    PVector camera = new PVector(0,-100,-300);
    PVector cameraDir = new PVector(0,0,1);
    PVector cameraUp = new PVector(0,1,0);
    final float step = 2f;
    final float rStep = TAU / 200f;

    public static void main(String[] args) {
        PApplet.main(ModelRender.class);
    }

    @Override
    public void settings() {
        size(720, 720, P3D);
        //pixelDensity(2);
    }

    @Override
    public void setup() {
        textureMode(NORMAL);
        radioShape = loadShape("models/radio/radio.obj");
        radioTexture = loadImage("models/radio/radio.png");
        floor = loadImage("models/floor.png");
        wall = loadImage("models/wall.png");
        shader = loadShader("szakkor7/frag.glsl","szakkor7/vert.glsl");

        PImage radioExponent = loadImage("models/radio/radio_exponent.png");
        shader.set("exponent",radioExponent);
        shader.set("repeat",1);
        shader.set("hasExponent",false);
    }

    private void processInput() {
        if(keyPressed) {
            if(key != CODED) {
                switch (key) {
                    case 'W':
                    case 'w':
                        camera.add(PVector.mult(cameraDir,step));
                        break;
                    case 'S':
                    case 's':
                        camera.add(PVector.mult(cameraDir,-step));
                        break;
                    case 'D':
                    case 'd':
                        camera.add(cameraDir.cross(cameraUp).mult(step));
                        break;
                    case 'A':
                    case 'a':
                        camera.add(cameraDir.cross(cameraUp).mult(-step));
                        break;
                    case 'E':
                    case 'e':
                        camera.add(PVector.mult(cameraUp,-step));
                        break;
                    case 'Q':
                    case 'q':
                        camera.add(PVector.mult(cameraUp,step));
                        break;

                }
            } else {
                PMatrix3D rotation;
                switch(keyCode) {
                    case LEFT://Positive Y rotation
                        rotation = rotateAroundAxis(cameraUp,rStep);
                        break;
                    case RIGHT://Negative Y
                        rotation = rotateAroundAxis(cameraUp,-rStep);
                        break;
                    case UP://Positive X
                        rotation = rotateAroundAxis(cameraUp.cross(cameraDir),rStep);
                        break;
                    case DOWN://Negative X
                        rotation = rotateAroundAxis(cameraUp.cross(cameraDir),-rStep);
                        break;
                    case CONTROL://Negative Z
                        rotation = rotateAroundAxis(cameraDir,rStep);
                        break;
                    case ALT://Positive Z
                        rotation = rotateAroundAxis(cameraDir,-rStep);
                        break;
                    default:
                        rotation = new PMatrix3D();
                }


                rotation.mult(cameraDir, cameraDir);
                rotation.mult(cameraUp,cameraUp);
            }
        }
    }


    @Override
    public void draw() {
        background(0);
        resetShader();
        processInput();

        PVector center = PVector.add(camera,cameraDir);
        camera(camera.x,camera.y,camera.z,center.x,center.y,center.z,cameraUp.x,cameraUp.y,cameraUp.z);

        scale(1, -1, 1); // flip coordinate system (+Y up)


        //rotateY(angle);

        // light
        pushMatrix();
        PVector light = new PVector(100, 80, -100);
        float x = light.x * cos(angle);
        float y = light.y * sin(angle);
        float z = light.z * sin(angle);

        pointLight(255, 0, 0, x, y+300, z);
        translate(x, y+300, z);
        sphere(1);
        popMatrix();

        pushMatrix();
        pointLight(0,255,0,x,y-400,z+10);
        translate(x,y-400,z+10);
        sphere(1);
        popMatrix();

        pushMatrix();
        pointLight(0, 0, 255, x+100, y+45, z);
        translate(x+100, y+45, z);
        sphere(1);
        popMatrix();

        pushMatrix();
        pointLight(0, 0, 255, x+200,y-400,z+10);
        translate(x+200,y-400,z+10);
        sphere(1);
        popMatrix();

        renderAxis();

        scale(2);

        shader(shader); // set active shader
        shader.set("hasExponent",true);
        shader.set("repeat",1);
        radioShape.setTexture(radioTexture); // set texture for model
        shape(radioShape); //  render model
        pushMatrix();
        translate(0,-100,-20);
        shape(radioShape);
        popMatrix();
        renderWalls();

        angle += 0.01f;
    }

    private void renderWalls() {
        //Floor and ceiling
        float side = 500;
        float hs = side / 2;

        shader.set("hasExponent",false);
        shader.set("repeat",5);
        for(float y = hs;y >= -hs;y -= side) {
            beginShape(TRIANGLE_STRIP);
            texture(floor);
            textureMode(NORMAL);
            textureWrap(REPEAT);
            normal(0,-y/hs,0);
            vertex(-hs, y, -hs, 0, 0);
            vertex(hs, y, -hs, 1, 0);
            vertex(-hs, y, hs, 0, 1);
            vertex(hs, y, hs, 1, 1);
            endShape();
        }
        for(float i = hs;i >= -hs;i -= side) {
            beginShape(TRIANGLE_STRIP);
            texture(wall);
            textureMode(NORMAL);
            normal(-i/hs,0,0);
            vertex(i,hs,-hs, 0, 0);
            vertex(i,hs,hs, 1, 0);
            vertex(i,-hs,-hs, 0, 1);
            vertex(i,-hs,hs, 1, 1);
            endShape();

            beginShape(TRIANGLE_STRIP);
            texture(wall);
            textureMode(NORMAL);
            normal(0,0,-i/hs);
            vertex(-hs,hs,i, 0,0);
            vertex(hs,hs,i, 1,0);
            vertex(-hs,-hs,i, 0,1);
            vertex(hs,-hs,i, 1,1);
            endShape();
        }
    }

    /**
     * Returns a matrix thet rotates space around a given axis
     * https://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle
     *
     * @param u A vector pointing in the direction of the axis
     * @param angle The angle to rotate by
     */
    PMatrix3D rotateAroundAxis(PVector u,float angle) {
        u.normalize();
        float a = 1 - cos(angle);
        return new PMatrix3D(
                cos(angle) + u.x*u.x*a,u.x*u.y*a - u.z*sin(angle),u.x*u.z*a + u.y*sin(angle),0,
                u.y*u.x*a + u.z*sin(angle),cos(angle) + u.y*u.y*a,u.y*u.z*a - u.x*sin(angle),0,
                u.z*u.x*a - u.y*sin(angle),u.z*u.y*a+u.x*sin(angle),cos(angle)+u.z*u.z*a,0,
                0,0,0,1
        );
    }

    /**
     * Render coordinate axis
     */
    private void renderAxis() {
        strokeWeight(2);
        stroke(255, 0, 0);
        line(0, 0, 0, 100, 0, 0);
        stroke(0, 255, 0);
        line(0, 0, 0, 0, 100, 0);
        stroke(0, 0, 255);
        line(0, 0, 0, 0, 0, 100);
        noStroke();
    }
}