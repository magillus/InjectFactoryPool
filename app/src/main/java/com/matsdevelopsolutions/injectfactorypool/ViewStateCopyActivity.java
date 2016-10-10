package com.matsdevelopsolutions.injectfactorypool;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Test activity for save instant coping.
 */
public class ViewStateCopyActivity extends AppCompatActivity {

    @BindView(R.id.txt_left)
    TextView leftTextView;

    @BindView(R.id.txt_right)
    TextView rightTextView;

    @BindView(R.id.btn_copy)
    Button btnCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_state_copy);
        ButterKnife.bind(this);


        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyState(v);
            }
        });
    }

    public void copyState(View v) {

        Parcelable state = leftTextView.onSaveInstanceState();

        rightTextView.onRestoreInstanceState(state);

        rightTextView.requestLayout();
    }


}
