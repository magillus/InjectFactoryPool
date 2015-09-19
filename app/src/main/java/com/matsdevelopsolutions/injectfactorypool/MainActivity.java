package com.matsdevelopsolutions.injectfactorypool;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class MyInflaterFactory implements LayoutInflater.Factory2 {

        private static final int VIEW_TAG_ID = 234;
        private final LayoutInflater.Factory2 fallbackInflaterFactory;
        // pool per type with list
        private final Map<String, List<View>> freeViewPool = new ConcurrentHashMap<>();
        private final Map<String, List<View>> inUseViewPool = new ConcurrentHashMap<>();

        public MyInflaterFactory(LayoutInflater.Factory2 fallbackInflater) {
            this.fallbackInflaterFactory = fallbackInflater;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return null;
        }

        private View fetchView(View parent, String name, Context context, AttributeSet attrs) {
            List<View> viewList = freeViewPool.get(name);
            if (viewList != null && viewList.size() > 0) {
                return freeViewPool.get(name).get(0);
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
            view.setTag(VIEW_TAG_ID, name);
            List<View> viewList = inUseViewPool.get(name);
            if (viewList!=null) {
                viewList.add(view);
            } else {
                viewList = new ArrayList<>();
                viewList.add(view);
                inUseViewPool.put(name, viewList);
            }
        }

        public void releaseView(View v) {
            String name = (String) v.getTag(VIEW_TAG_ID);
            if (name!=null) {
                List<View> viewList = inUseViewPool.get(name);
                if (viewList.contains(v)) {
                    viewList.remove(v);
                    List<View> poolList = freeViewPool.get(name);
                    if (poolList==null) {
                        poolList = new ArrayList<>();
                    }
                    poolList.add(v);

                    cleanUpView(v);
                }
            }
        }

        private void cleanUpView(View v) {
            // remove parent
            ViewParent parent = v.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(v);
            }
            // default styles
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
    }

    private MyInflaterFactory inflaterFactory;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inflaterFactory = new MyInflaterFactory(getLayoutInflater().getFactory2());
        // use introspection to allow a new Factory to be set
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(getLayoutInflater(), false);
            getLayoutInflater().setFactory2(new MyInflaterFactory(getLayoutInflater().getFactory2()));
        } catch (NoSuchFieldException e) {
            // ...
        } catch (IllegalArgumentException e) {
            // ...
        } catch (IllegalAccessException e) {
            // ...
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
