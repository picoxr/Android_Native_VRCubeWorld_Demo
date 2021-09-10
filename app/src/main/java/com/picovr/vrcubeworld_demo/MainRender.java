package com.picovr.vrcubeworld_demo;

import static com.picovr.cvclient.ButtonNum.buttonLG;
import static com.picovr.cvclient.ButtonNum.buttonRG;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.MotionEvent;
import com.picovr.cvclient.CVController;
import com.picovr.vrlibs.renderer.VRRenderer;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;
import java.io.InputStream;
import java.util.Random;

public class MainRender extends VRRenderer {

    private final Random random = new Random();
    private Material mCubeMaterial;
    private CVController ctrRight, ctrLeft;

    public MainRender(Context context) {
        super(context);
    }

    @Override
    public void initScene() {
        @SuppressLint("ResourceType")
        InputStream is = getContext().getResources().openRawResource(R.drawable.right);
        @SuppressLint("ResourceType")
        InputStream is1 = getContext().getResources().openRawResource(R.drawable.left);
        @SuppressLint("ResourceType")
        InputStream is2 = getContext().getResources().openRawResource(R.drawable.top);
        @SuppressLint("ResourceType")
        InputStream is3 = getContext().getResources().openRawResource(R.drawable.bottom);
        @SuppressLint("ResourceType")
        InputStream is4 = getContext().getResources().openRawResource(R.drawable.back);
        @SuppressLint("ResourceType")
        InputStream is5 = getContext().getResources().openRawResource(R.drawable.front);
        Bitmap[] bitmaps = new Bitmap[6];
        bitmaps[0] = BitmapFactory.decodeStream(is);
        bitmaps[1] = BitmapFactory.decodeStream(is1);
        bitmaps[2] = BitmapFactory.decodeStream(is2);
        bitmaps[3] = BitmapFactory.decodeStream(is3);
        bitmaps[4] = BitmapFactory.decodeStream(is4);
        bitmaps[5] = BitmapFactory.decodeStream(is5);
        try {
            getCurrentScene().setSkybox(bitmaps);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        getCurrentCamera().setFarPlane(2000);

        initTextPlane("Please hold the trigger button to add cube", 1.2f, 1.8f, -3);
        initTextPlane("Please hold the grip button to remove cube", 1.2f, 1.5f, -3);

        mCubeMaterial = new Material();
        mCubeMaterial.enableLighting(true);
        mCubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        getCurrentCamera().setZ(10);

        for (int i = 0; i < 10; i++) {
            addNewCube();
        }
    }

    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        if (0 != ctrRight.getConnectState()) {
            if (ctrRight.getButtonState(buttonLG)) {
                removeRandomCube();
            }
            if (ctrRight.getTriggerNum() > 220) {
                addNewCube();
            }
        }

        if (0 != ctrLeft.getConnectState()) {
            if (ctrLeft.getButtonState(buttonRG)) {
                removeRandomCube();
            }
            if (ctrLeft.getTriggerNum() > 220) {
                addNewCube();
            }
        }
    }

    public void removeRandomCube() {
        final int count = getCurrentScene().getNumChildren();
        if (count > 0) {
            final int index = random.nextInt(count);
            final Object3D child = getCurrentScene().getChildrenCopy().get(index);
            if (child instanceof Cube) {
                getCurrentScene().removeChild(child);
                child.destroy();
            }
        }
    }

    private void initTextPlane(String text, float posX, float posY, float posZ) {
        Plane plane = new Plane(3f, 1.5f, 2, 1);
        plane.setTransparent(true);
        plane.setPosition(posX, posY, posZ);
        plane.setLookAt(0, 0, 0);
        plane.setVisible(true);

        Material material = new Material();
        material.setColorInfluence(0);
        Bitmap bitmap = createTextView(text, 80, 2000, 1000, Color.BLUE, 30);
        Texture texture = new Texture("textTexture", bitmap);
        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        plane.setMaterial(material);
        getCurrentScene().addChild(plane);
    }

    public void addNewCube() {
        Cube cube = new Cube(1);
        double x = random.nextDouble();
        double y = random.nextDouble();
        double z = random.nextDouble();
        int i = random.nextInt(20) + 30;
        cube.setPosition(x > 0.5f ? x * i : x * -i, y > 0.5f ? y * i : y * -i, z > 0.5f ? z * i : z * -i);
        cube.setMaterial(mCubeMaterial);
        cube.setColor(0x666666 + random.nextInt(0x999999));
        getCurrentScene().addChild(cube);

        Vector3 randomAxis = new Vector3(random.nextFloat(), random.nextFloat(), random.nextFloat());
        randomAxis.normalize();

        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis, 360);
        anim.setTransformable3D(cube);
        anim.setDurationMilliseconds(3000 + (int) (random.nextDouble() * 5000));
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();
    }

    private Bitmap createTextView(String title, float textsize, int width, int height, int color, int textAlign) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textsize);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setDither(false);
        paint.setFakeBoldText(true);
        paint.setLinearText(false);
        paint.setShadowLayer(0.05f, 0, 0, color);

        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);

        float textLength = paint.measureText(title);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = (float) (Math.ceil(fm.descent - fm.ascent));
        Canvas bitmapCanvas = new Canvas(bitmap);
        float x = 0;
        if (textAlign == 0) {
            x = 0;
        } else if (textAlign == 1) {
            x = (width - textLength) / 2;
        } else if (textAlign == 2) {
            x = width - textLength;
        }
        float y = (height - textHeight) / 2;
        bitmapCanvas.drawText(title, x, y, paint);
        return bitmap;
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    public void setController(CVController rightController, CVController leftController) {
        ctrRight = rightController;
        ctrLeft = leftController;
    }
}
