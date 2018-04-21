package me.dawars.szakkor5;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import processing.core.PMatrix3D;
import processing.opengl.PShader;


public class Shading extends PApplet {
    public static void main(String[] args) {
        PApplet.main(Shading.class);
    }

    @Override
    public void settings() {
        size(400, 400, P3D);
        //g = (PGraphicsOpenGL) createGraphics(400,400);
    }

    PShape triangle;
    PShape tetrahedron;
    PShape cylinder;
    PShape sphere;
    PShape paraboloid;
    PShape torus;
    float rotation = 0;

    PVector camera = new PVector(0,0,0);
    PVector cameraDir = new PVector(0,0,-1);
    PVector cameraUp = new PVector(0,1,0);
    final float step = 2f;
    final float rStep = TAU / 200f;
    PShader sh;
    PVector lightDir = new PVector(1,-1,-1);


    PMatrix3D baseTransform = new PMatrix3D(
            1,  0,  0,  200,
            0,  1,  0,  200,
            0,  0,  1,  0,
            0,  0,  0,    1
    );

    @Override
    public void setup() {
        sphere = createSphere(100);
        //cylinder = createCylinder(20,60);
        //paraboloid = createParaboloid();
        //torus = createTorus(200,80);

        //sh = loadShader("szakkor5/frag_lambert.glsl","szakkor5/vertex_lambert.glsl");
        sh = loadShader("szakkor5/frag_blinn.glsl","szakkor5/vertex_blinn.glsl");
    }


    @Override
    public void draw() {
        background(0);
        translate(width/2,height/2);
        lightDir = rotateAroundY(lightDir.x,lightDir.y,lightDir.z,TAU / 200);

        sh.set("lightDir",lightDir);
        shader(sh);

        shape(sphere);
    }

    private PShape createTorus(float innerRadius,float outerRadius) {
        PShape shape = createShape();
        shape.beginShape(TRIANGLE_STRIP);
        shape.noStroke();

        float innerStep = TAU / 20;
        float outerStep = TAU / 20;

        for(float innerAngle = 0;innerAngle <= TAU;innerAngle += innerStep) {
            PVector p0 = new PVector(innerRadius*cos(innerAngle),0,innerRadius*sin(innerAngle));
            PVector p1 = new PVector(innerRadius*cos(innerAngle + innerStep),0,innerRadius*sin(innerAngle + innerStep));

            for(float outerAngle=0;outerAngle <= (TAU + outerStep);outerAngle += outerStep) {
                PVector v0 = rotateAroundPointY(polarToDescartes(outerRadius,outerAngle,p0),p0,innerAngle);
                PVector v1 = rotateAroundPointY(polarToDescartes(outerRadius,outerAngle,p1),p1,innerAngle + innerStep);

                shape.fill(127,0,127);
                shape.vertex(v0.x,v0.y,v0.z);
                shape.fill(115,40,115);
                shape.vertex(v1.x,v1.y,v1.z);
            }
        }

        shape.endShape();
        return shape;
    }

    /**
     * Rotates a point around another point (Y)
     * @param p Point to rotate
     * @param ref Point to rotate around
     * @param angle Angle to rotate by (radians)
     */
    private PVector rotateAroundPointY(PVector p,PVector ref,float angle) {
        PVector res = new PVector();
        res.x = (p.x - ref.x) * cos(angle) - (p.z - ref.z) * sin(angle) + ref.x;
        res.y = p.y;
        res.z = (p.x - ref.x) * sin(angle) + (p.z - ref.z) * cos(angle) + ref.z;
        return res;
    }

    /**
     * Converts polar to descartes coords relative to a point
     * @param r Radius
     * @param angle Angle
     * @param center Point of reference
     */
    private PVector polarToDescartes(float r,float angle,PVector center) {
        PVector res = new PVector();
        res.x = r*cos(angle) + center.x;
        res.y = r*sin(angle) + center.y;
        res.z = center.z;
        return res;
    }

    private PShape createParaboloid(final int width, final float a, final float b,final int hyperbolic) {
        PShape shape = createShape();
        //Set this to 1 for an elliptic paraboloid or -1 for a hyperbolic one

        shape.beginShape(TRIANGLE_STRIP);
        shape.noStroke();

        float rotateStep = TAU / 100;

        float r = 0;
        float rStep = 1;
        for(float angle = 0;angle <= TAU;angle += rotateStep) {
            for(;(r <= width) && r >= 0;r += rStep) {
                float x0 = r * cos(angle);
                float y0 = r * sin(angle);
                float z0 = getParaboloidZ(x0,y0,a,b,hyperbolic);

                float x1 = (r + rStep) * cos(angle);
                float y1 = (r + rStep) * sin(angle);
                float z1 = getParaboloidZ(x1,y1,a,b,hyperbolic);

                float x2 = r * cos(angle + rotateStep);
                float y2 = r * sin(angle + rotateStep);
                float z2 = getParaboloidZ(x2,y2,a,b,hyperbolic);

                float x3 = (r + rStep) * cos(angle + rotateStep);
                float y3 = (r + rStep) * sin(angle + rotateStep);
                float z3 = getParaboloidZ(x3,y3,a,b,hyperbolic);

                shape.fill(127,0,127);
                shape.vertex(x0,y0,z0);
                shape.vertex(x1,y1,z1);
                shape.vertex(x2,y2,z2);
                shape.vertex(x3,y3,z3);
            }

            if(r > width)
                r = width;
            if(r < 0)
                r = 0;
            rStep *= -1;
        }

        shape.endShape();
        return shape;
    }

    private float getParaboloidZ(float x, float y,float a, float b,float hyperbolic) {
        return hyperbolic * (x*x)/(a*a) + (y*y)/(b*b);
    }

    private PShape createSphere(int radius) {
        PShape shape = createShape();
        int quality = 36;

        shape.beginShape(TRIANGLE_STRIP);
        shape.noStroke();

        float step = 360 / quality;
        for(float hAngle = 0;hAngle < 180;hAngle += step) {
            //Draw an arc of the sphere
            for(float vAngle = 0;vAngle <= 360;vAngle += step) {
                PVector v0 = rotateAroundY(
                        radius * cos(radians(vAngle)),
                        radius * sin(radians(vAngle)),
                        0,
                        radians(hAngle)
                );

                shape.fill(127,0,127);
                PVector n0 = v0.normalize(null);
                //shape.fill(n0.x * 255,n0.y * 255,n0.z * 255);
                shape.normal(n0.x,n0.y,n0.z);
                shape.vertex(v0.x,v0.y,v0.z);

                PVector v1 = rotateAroundY(
                        v0.x,
                        v0.y,
                        v0.z,
                        radians(step)
                );
                /*if(vAngle % 15 == 0) { //Stripes for visibility
                    shape.fill(115,40,115);
                }*/
                //shape.fill(115,40,115);
                PVector n1 = v1.normalize(null);
                shape.normal(n1.x,n1.y,n1.z);
                //shape.fill(n1.x * 255,n1.y * 255,n1.z * 255);
                shape.vertex(v1.x,v1.y,v1.z);
            }
        }

        shape.endShape();
        return shape;
    }

    /**
     * Rotates a coord around the Y axis
     *  @param angle Angle in radians
     */
    private PVector rotateAroundY(float x,float y,float z,float angle) {
        PVector res = new PVector();
        res.x = x * cos(angle) - z * sin(angle);
        res.y = y;
        res.z = x * sin(angle) + z * cos(angle);
        return res;
    }


    private PShape createCylinder(int radius,int height) {
        PShape shape = createShape();
        int quality = 36;

        shape.beginShape(TRIANGLE_STRIP);
        shape.noStroke();

        int step = 360 / quality;
        for(int angle = 0;angle <= 360;angle += step) {
            float x = radius * cos(radians(angle));
            float z = radius * sin(radians(angle));

            //shape.fill(100,0,100);
            shape.fill(77, 38, 0);//brown for tree
            shape.vertex(x,0,z);
            //shape.fill(0,100,0);
            shape.vertex(x,height,z);
        }

        shape.endShape();
        return shape;
    }

    private PShape createTetrahedron() {
        PShape shape = createShape();

        shape.beginShape(TRIANGLES);

        shape.fill(255,0,0);
        shape.vertex(0,0,100);
        shape.vertex(-50,0,0);
        shape.vertex(50,0,0);

        shape.fill(255,0,0);
        shape.vertex(0,0,100);
        shape.vertex(0,100,50);
        shape.vertex(-50,0,0);

        shape.fill(0,255,0);
        shape.vertex(0,0,100);
        shape.vertex(0,100,50);
        shape.vertex(50,0,0);

        shape.fill(0,0,255);
        shape.vertex(50,0,0);
        shape.vertex(-50,0,0);
        shape.vertex(0,100,50);

        shape.endShape();
        return shape;
    }

    private PShape createTriangle() {
        PShape shape = createShape();

        shape.beginShape(TRIANGLES);
        shape.fill(0, 0, 255);
        shape.vertex(0, -100);
        shape.fill(0, 255, 0);
        shape.vertex(100, 100);
        shape.fill(255, 0, 0);
        shape.vertex(-100, 100);
        shape.endShape();

        return shape;
    }
}
