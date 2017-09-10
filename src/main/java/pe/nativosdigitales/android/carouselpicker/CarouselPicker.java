package pe.nativosdigitales.android.carouselpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos Aguinaga on 9/09/2017.
 */

public class CarouselPicker extends HorizontalScrollView {

    private static final String TAG = "CarouselPicker";

    private Context context;
    private Integer normal_size, active_size;
    private Integer device_width;
    private Integer item_width;

    private float dm_density;

    private LinearLayout scroll_container;

    private Integer last_scroll_position = -1;

    private Integer carousel_position = -1;

    private List<View> carousel_images = new ArrayList<>();
    private List<Drawable> carousel_drawables_on = new ArrayList<>();
    private List<Drawable> carousel_drawables_off = new ArrayList<>();

    public OnScrollChangedListener mOnScrollChangedListener;

    private OnScrollEndedListener on_scroll_ended_listener;

    public interface OnItemSelectedListener {

        void OnItemSelected();
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    public interface  OnScrollEndedListener {
        void OnScrollEnded();
    }

    public CarouselPicker(Context context) {
        this(context, null);
    }

    public CarouselPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize (Context context,
                            Integer normal_size, Integer active_size) {

        this.context = context;
        this.normal_size = normal_size;
        this.active_size = active_size;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        dm_density = displayMetrics.density;

        float dpWidth = displayMetrics.widthPixels / dm_density;

        device_width = Math.round(dpWidth);
        item_width = Math.round(this.device_width / 3);

        CarouselPicker.this.setOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {

                Integer dp_position = Math.round(l / dm_density);

                carousel_position = (dp_position + (item_width / 2)) / item_width;

                if (!last_scroll_position.equals(carousel_position)) {

                    last_scroll_position = carousel_position;
                    Log.e(TAG, "onScrollChanged Left: " + carousel_position);

                    carousel_images.get(carousel_position).performClick();

//                    setActive(carousel_images.get(carousel_position));
                }
            }
        });

        CarouselPicker.this.setOnScrollEndedListener(new OnScrollEndedListener() {
            @Override
            public void OnScrollEnded() {

                Log.e(TAG, "OnScrollEnded position: " + carousel_position);

                if (carousel_position >= 0) {

                    carousel_images.get(carousel_position).performClick();

                    centerActive(carousel_images.get(carousel_position));
                }
            }
        });

        CarouselPicker.this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    Log.e(TAG, "onTouch: endTouch" );

                    on_scroll_ended_listener.OnScrollEnded();

                    if (carousel_position >= 0) {

                        centerActive(carousel_images.get(carousel_position));
                    }
                }

                return false;
            }
        });

        createContainer();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        // do what you want
        return true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener){

        this.mOnScrollChangedListener = onScrollChangedListener;
    }

    public void setOnScrollEndedListener (OnScrollEndedListener on_scroll_ended_listener) {

        this.on_scroll_ended_listener = on_scroll_ended_listener;
    }

    private void createContainer() {

        scroll_container = new LinearLayout(context);
    }

    public void addItem (final Integer drawable_on_id, final Integer drawable_off_id,
                         final OnItemSelectedListener select_listener) {

        View carousel_item = LayoutInflater.from(context)
                .inflate(R.layout.carousel_item, null, false);

        ImageView imagen = carousel_item.findViewById(R.id.carousel_image);

        Drawable imagen_drawable_on = getResources().getDrawable(drawable_on_id);
        Drawable imagen_drawable_off = getResources().getDrawable(drawable_off_id);

        imagen.setImageDrawable(imagen_drawable_on);

        carousel_drawables_on.add(imagen_drawable_on);
        carousel_drawables_off.add(imagen_drawable_off);

        setSize(carousel_item, Math.round(device_width / 3), active_size);
        setSize(imagen, normal_size, normal_size);

        carousel_item.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                setActive(view);
                centerActive(view);

                select_listener.OnItemSelected();
            }
        });

        carousel_images.add(carousel_item);
    }

    private void setActive (View carousel_item) {

        Log.e(TAG, "setActive: " + carousel_item.toString());

        for (int i = 0; i < carousel_images.size(); i += 1) {

            setInactive(carousel_images.get(i));
        }

        ImageView image = carousel_item.findViewById(R.id.carousel_image);
        image.setImageDrawable(carousel_drawables_on.get(carousel_images.indexOf(carousel_item)));
        setSize(image, active_size, active_size);
    }

    private void setInactive (View carousel_image) {

        ImageView image = carousel_image.findViewById(R.id.carousel_image);
        image.setImageDrawable(carousel_drawables_off.get(carousel_images.indexOf(carousel_image)));
        setSize(image, normal_size, normal_size);
    }

    private void centerActive (View carousel_item) {

        Log.e(TAG, "centerActive: " + carousel_item.toString());

        Integer position = carousel_images.indexOf(carousel_item);

        Integer new_position = convertPXtoDP(Math.round(position * item_width));

        CarouselPicker.this.smoothScrollTo(new_position, 0);
    }

    public void addFakeItem () {

        View carousel_item = LayoutInflater.from(context)
                .inflate(R.layout.carousel_item, null, false);

        ImageView imagen = carousel_item.findViewById(R.id.carousel_image);

        setSize(carousel_item, Math.round(device_width / 3), active_size);
        setSize(imagen, normal_size, normal_size);

        scroll_container.addView(carousel_item);
    }

    public void showItems () {

        addFakeItem();

        for (int i = 0; i < carousel_images.size(); i += 1) {



            scroll_container.addView(carousel_images.get(i));
        }

        CarouselPicker.this.addView(scroll_container);

        addFakeItem();

        carousel_images.get(0).performClick();
    }

    private void setSize(View view, int width, int height){

        int new_width = convertPXtoDP(width);
        int new_height = convertPXtoDP(height);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new_width, new_height);
        view.setLayoutParams(layoutParams);
    }

    private Integer convertPXtoDP (Integer px) {

        Integer dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());;

        return dp;
    }
}
