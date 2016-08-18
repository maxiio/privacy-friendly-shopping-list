package privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.ui.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.R;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.framework.context.AbstractInstanceFactory;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.framework.context.InstanceFactory;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.framework.utils.CameraUtils;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.logic.product.business.ProductService;
import privacyfriendlyshoppinglist.secuso.org.privacyfriendlyshoppinglist.ui.products.ProductsActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: Grebiel Jose Ifill Brito
 * Created: 16.08.16 creation date
 */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String THUMBNAIL_KEY = "thumbnail";

    private static final int THUMBNAIL_SIZE = 200;
    public static final int FLASH_OPTIONS_AVAILABLE = 3;
    private Camera mCamera;
    private CameraPreview mPreview;
    private String productId;
    private int cameraOrientation;
    private FloatingActionButton flashButton;
    private FloatingActionButton captureButton;
    private FloatingActionButton retakeButton;
    private List<Integer> flashIcons;
    private int currentFlashIconIndex;
    private boolean PhotoCaptured;
    private Bitmap takenImageBitmap;
    private Bitmap rotatedThumbnailBitmap;
    private byte[] imageData;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);

        Bundle extras = getIntent().getExtras();
        productId = (String) extras.get(ProductsActivity.PRODUCT_ID_KEY);
        String productName = (String) extras.get(ProductsActivity.PRODUCT_NAME);
        setTitle(productName);
        setupFlashIcons();

        mCamera = getInitializedCamera();
        setupCameraPreview();

        captureButton = (FloatingActionButton) findViewById(R.id.button_capture);
        flashButton = (FloatingActionButton) findViewById(R.id.button_flash);
        retakeButton = (FloatingActionButton) findViewById(R.id.button_retake);

        captureButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        retakeButton.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch ( item.getItemId() )
        {
            // override back navigation behavior
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch ( id )
        {
            case R.id.button_capture:
                handleButtonCapture();
                break;
            case R.id.button_flash:
                setupFlash();
                break;
            case R.id.button_retake:
                handleButtonRetake();
        }
    }

    private void handleButtonCapture()
    {
        if ( !PhotoCaptured )
        {
            captureButton.setVisibility(View.GONE);
            PhotoCaptured = true;
            captureButton.setImageResource(R.drawable.ic_check_white_48dp);
            flashButton.animate().alpha(0.0f).setDuration(500L);
            mCamera.takePicture(null, null, mPicture);
        }
        else
        {
            String productImagePath = getImagePath();

            // set thumbnail bitmap using a background thread
            prepareBitmaps(imageData)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            bitmap ->
                            {
                                rotatedThumbnailBitmap = bitmap;
                                // save bitmap using a background thread
                                CameraUtils.saveBitmap(takenImageBitmap, productImagePath, cameraOrientation)
                                        .subscribeOn(Schedulers.newThread())
                                        .subscribe();
                            },
                            Throwable::printStackTrace,
                            () ->
                            {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(THUMBNAIL_KEY, rotatedThumbnailBitmap);
                                setResult(RESULT_OK, resultIntent);
                                mCamera.release();
                                finish();
                            }
                    );
        }
    }

    private void handleButtonRetake()
    {
        PhotoCaptured = false;
        retakeButton.setVisibility(View.GONE);
        flashButton.animate().alpha(1.0f).setDuration(500L);
        captureButton.setImageResource(R.drawable.ic_camera_alt_white_48dp);
        mCamera.startPreview();
    }

    private void setupCameraPreview()
    {
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    private void setupFlash()
    {
        currentFlashIconIndex = (++currentFlashIconIndex) % FLASH_OPTIONS_AVAILABLE;
        flashButton.setImageResource(flashIcons.get(currentFlashIconIndex));

        Camera.Parameters parameters = mCamera.getParameters();
        switch ( currentFlashIconIndex )
        {
            case 0:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case 1:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            default:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mCamera.setParameters(parameters);
    }

    private void setupFlashIcons()
    {
        currentFlashIconIndex = 0;
        flashIcons = new ArrayList<>();
        flashIcons.add(R.drawable.ic_flash_auto_white_48dp);
        flashIcons.add(R.drawable.ic_flash_on_white_48dp);
        flashIcons.add(R.drawable.ic_flash_off_white_48dp);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            imageData = data;
            mCamera.stopPreview();
            captureButton.setVisibility(View.VISIBLE);
            retakeButton.setVisibility(View.VISIBLE);
            retakeButton.animate().rotation(-360).alpha(1.0f).setDuration(500L);
        }
    };

    private Observable<Bitmap> prepareBitmaps(byte[] data)
    {
        Observable<Bitmap> observable = Observable
                .create(subscriber ->
                {
                    subscriber.onNext(prepareBitmapsSync(data));
                    subscriber.onCompleted();
                });
        return observable;
    }

    private Bitmap prepareBitmapsSync(byte[] data)
    {
        takenImageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(takenImageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true);
        return CameraUtils.getRotatedBitmap(thumbnailBitmap, cameraOrientation);
    }

    private String getImagePath()
    {
        AbstractInstanceFactory instanceFactory = new InstanceFactory(getApplicationContext());
        ProductService productService = (ProductService) instanceFactory.createInstance(ProductService.class);
        return productService.getProductImagePath(productId);
    }


    public Camera getInitializedCamera()
    {
        Camera camera = null;
        try
        {
            camera = Camera.open();
            cameraOrientation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = CameraUtils.getRotationAdjustment(cameraOrientation);
            camera.setDisplayOrientation(orientation);
            setAutoFocus(camera);
            PhotoCaptured = false;
        }
        catch ( Exception e )
        {
            // no camera available
        }
        return camera;
    }

    private void setAutoFocus(Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
    }
}
