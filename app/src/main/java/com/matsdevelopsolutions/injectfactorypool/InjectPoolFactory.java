package com.matsdevelopsolutions.injectfactorypool;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mateusz on 9/25/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InjectPoolFactory implements LayoutInflater.Factory2 {

    private final LayoutInflater.Factory2 fallbackInflaterFactory;
    // pool per type with list
    private final Map<String, List<ViewDetails>> freeViewPool = new ConcurrentHashMap<>();
    private final Map<String, List<ViewDetails>> inUseViewPool = new ConcurrentHashMap<>();

    public static class ViewDetails {
        public ViewDetails(String name, View view) {
            this.name = name;
            this.view = view;
        }
        public String name;
        public View view;
        public View.OnAttachStateChangeListener onAttachStateChangeListener;
    }
    public InjectPoolFactory(LayoutInflater.Factory2 fallbackInflater) {
        this.fallbackInflaterFactory = fallbackInflater;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View v = onCreateViewInternal(parent, name, context, attrs);
        if (v != null) {
            return v;
        }
        return fallbackInflaterFactory.onCreateView(parent, name, context, attrs);
    }
    public void releaseView(View v) {
//        if (inUseViewPool.values().contains(v)) {
//            for(Map.Entry<String, List<ViewDetails>> entry : inUseViewPool.entrySet()){
//                if (entry.getValue().contains(v).equals(v)) {
//                    releaseView(v);
//                }
//            }
//        }
    }
    public void releaseView(ViewDetails v) {
        String name = (String) v.name;
        if (name != null) {
            List<ViewDetails> viewList = inUseViewPool.get(name);
            // find View in list of view details
            if (viewList.contains(v)) {
                viewList.remove(v);
                List<ViewDetails> poolList = freeViewPool.get(name);
                if (poolList == null) {
                    poolList = new ArrayList<>();
                    freeViewPool.put(name, poolList);
                }
                poolList.add(v);

             //   cleanUpView(v);
            }
        }
    }

    private ViewDetails fetchView(View parent, String name, Context context, AttributeSet attrs) {
            List<ViewDetails> viewList = freeViewPool.get(name);
        if (viewList != null && viewList.size() > 0) {
            ViewDetails viewFromPool = freeViewPool.get(name).get(0);
            cleanUpView(viewFromPool);

            return viewFromPool;
        } else {
            ViewDetails vd = new ViewDetails(name, fallbackInflaterFactory.onCreateView(parent, name, context, attrs));
            return vd;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private View onCreateViewInternal(View parent, String name, Context context, AttributeSet attrs) {

        ViewDetails view = fetchView(parent, name, context, attrs);
        if (view != null) {
            if (view.onAttachStateChangeListener == null) {
                view.onAttachStateChangeListener = new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        releaseView(v);
                    }
                };
                view.view.addOnAttachStateChangeListener(view.onAttachStateChangeListener);
            }
            addToPool(name, view);
            return view.view;
        }
        return null;
    }

    private void addToPool(String name, ViewDetails view) {
        view.name = name;
        List<ViewDetails> viewList = inUseViewPool.get(name);
        if (viewList != null) {
            viewList.add(view);
        } else {
            viewList = new ArrayList<>();
            viewList.add(view);
            inUseViewPool.put(name, viewList);
        }
    }

    private void cleanUpView(ViewDetails v) {

        // remove parent
//        ViewParent parent = v.getParent();
//        if (parent != null && parent instanceof ViewGroup) {
//            ((ViewGroup) parent).removeView(v);
//        }
        // todo default styles reset
    }
}
