package wit.ie.invasivespeciestracker.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.MapFragment;

/**
 * Wrapper for {@link MapFragment} designed to embed into scrollable views.
 * {@link MapFragment} doesn't support by default touch interception.
 */
public class WorkaroundMapFragment extends MapFragment {
    private OnTouchListener mListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View layout = super.onCreateView(layoutInflater, viewGroup, bundle);

        // Interceptor view. It will catch events and pass it to listener and pass it to map.
        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());

        // Make interceptor view invisible.
        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Put interceptor view into fragment.
        ((ViewGroup) layout).addView(frameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return layout;
    }

    /**
     * Set listener to intercept touch events on map
     * @param listener
     */
    public void setListener(OnTouchListener listener) {
        mListener = listener;
    }

    public interface OnTouchListener {
        /**
         * Touch event on map.
         */
        void onTouch();
    }

    /**
     * Map is inside {@link FrameLayout} that intercepts and processes touch events.
     * Passes events to {@link OnTouchListener}
     * (Used to disable scrolling of parent view)
     */
    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mListener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    mListener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }
    }
}
