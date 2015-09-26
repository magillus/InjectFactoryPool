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
    private final Map<String, List<View>> freeViewPool = new ConcurrentHashMap<>();
    private final Map<String, List<View>> inUseViewPool = new ConcurrentHashMap<>();

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
        String name = (String) v.getTag(R.id.inject_tag);
        if (name != null) {
            List<View> viewList = inUseViewPool.get(name);
            if (viewList.contains(v)) {
                viewList.remove(v);
                List<View> poolList = freeViewPool.get(name);
                if (poolList == null) {
                    poolList = new ArrayList<>();
                }
                poolList.add(v);

                //cleanUpView(v);
            }
        }
    }

    private View fetchView(View parent, String name, Context context, AttributeSet attrs) {
        List<View> viewList = freeViewPool.get(name);
        if (viewList != null && viewList.size() > 0) {
            View viewFromPool = freeViewPool.get(name).get(0);
            cleanUpView(viewFromPool);

            return viewFromPool;
        } else {
            return fallbackInflaterFactory.onCreateView(parent, name, context, attrs);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private View onCreateViewInternal(View parent, String name, Context context, AttributeSet attrs) {

        View view = fetchView(parent, name, context, attrs);
        if (view != null) {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {

                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    releaseView(v);
                }
            });
            addToPool(name, view);
            return view;
        }
        return null;
    }

    private void addToPool(String name, View view) {
        view.setTag(R.id.inject_tag, name);
        List<View> viewList = inUseViewPool.get(name);
        if (viewList != null) {
            viewList.add(view);
        } else {
            viewList = new ArrayList<>();
            viewList.add(view);
            inUseViewPool.put(name, viewList);
        }
    }

    private void cleanUpView(View v) {
        // remove parent
        ViewParent parent = v.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(v);
        }
        // todo default styles reset
    }
}
