package com.picovr.vrlibs.renderer;

import android.content.Context;
import android.util.Log;
import com.google.vrtoolkit.cardboard.Viewport;
import com.picovr.vractivity.Eye;
import com.picovr.vractivity.HmdState;
import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

public abstract class VRRenderer extends Renderer {
    private static final float MAX_LOOKAT_ANGLE = 10;
    private static final String TAG = "VRRender";

    protected Matrix4 mCurrentEyeMatrix;
    protected Matrix4 mHeadViewMatrix;
    protected Quaternion mCurrentEyeOrientation;
    protected Quaternion mHeadViewQuaternion;
    protected Vector3 mCameraPosition;
    private Vector3 mForwardVec;
    private Vector3 mHeadTranslation;

    public long mFrameCount=0;

    private Matrix4 mLookingAtMatrix;
    private float[] mHeadView;

    private float mFOV;
    private float mIPD;

    private Vector3 hmdVec;
    private Quaternion hmdQuat;

	public VRRenderer(Context context) {
		super(context);
        Log.d(TAG,"VRRenderer - VRRenderer");
        mCurrentEyeMatrix = new Matrix4();
        mHeadViewMatrix = new Matrix4();
        mLookingAtMatrix = new Matrix4();
        mCurrentEyeOrientation = new Quaternion();
        mHeadViewQuaternion = new Quaternion();
        mHeadView = new float[16];
        mCameraPosition = new Vector3();
        mForwardVec = new Vector3();
        mHeadTranslation = new Vector3();
        hmdVec = new Vector3();
        hmdQuat = new Quaternion();
	}

    public void onNewFrame(HmdState hmdState) {
        mFOV = hmdState.getFov();
        mIPD = hmdState.getIpd();
        float[] orientation = new float[4];
        hmdState.getOrientation(orientation,0);

        float[] position = new float[3];
        hmdState.getPos(position,0);

        hmdVec.x = position[0];
        hmdVec.y = position[1];
        hmdVec.z = position[2];
        hmdQuat.w = orientation[3];
        hmdQuat.x = -orientation[0];
        hmdQuat.y = -orientation[1];
        hmdQuat.z = -orientation[2];

        mFrameCount++;
    }

    public void onDrawEye(Eye eye) {
        getCurrentCamera().updatePerspective(mFOV,mFOV,mFOV,mFOV);
        getCurrentCamera().setNearPlane(0.1);
        getCurrentCamera().setOrientation(hmdQuat);
        getCurrentCamera().setPosition(hmdVec);

        super.onRenderFrame(null);

    }

    public void onFinishFrame(Viewport viewport) {

    }

    public boolean isLookingAtObject(Object3D target) {
        Log.d(TAG,"VRRenderer - isLookingAtObject(target)");
        return this.isLookingAtObject(target, MAX_LOOKAT_ANGLE);
    }

    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        Log.d(TAG,"VRRenderer - isLookingAtObject(target,maxAngle)");
        mHeadViewQuaternion.fromMatrix(mHeadViewMatrix);
        mHeadViewQuaternion.inverse();
        mForwardVec.setAll(0, 0, 1);
        mForwardVec.rotateBy(mHeadViewQuaternion);

        mHeadTranslation.setAll(mHeadViewMatrix.getTranslation());
        mHeadTranslation.subtract(target.getPosition());
        mHeadTranslation.normalize();

        return mHeadTranslation.angle(mForwardVec) < maxAngle;
    }
}
