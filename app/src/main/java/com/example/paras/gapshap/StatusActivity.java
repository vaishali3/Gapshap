package com.example.paras.gapshap;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mStatusDatabase;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar=(Toolbar)findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       String status_value= getIntent().getStringExtra("status_value");


        mStatus=(TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn=(Button)findViewById(R.id.status_save_btn);
        mStatus.getEditText().setText(status_value);

        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        String uid= mCurrentUser.getUid();
        mStatusDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //progress
                mProgress= new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("updating status");
                mProgress.setMessage("Please wait while status is updating");
                mProgress.setCanceledOnTouchOutside(true);
                mProgress.show();

                String status=mStatus.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(),"Status Updated",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"there was some error",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
