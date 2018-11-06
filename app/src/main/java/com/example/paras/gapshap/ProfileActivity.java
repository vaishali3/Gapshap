package com.example.paras.gapshap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;
    private String mcurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage =(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_name);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView)findViewById(R.id.profile_friends);
        mProfileSendReqBtn=(Button) findViewById(R.id.profile_request_btn);
        mProfileDeclineBtn=(Button)findViewById(R.id.profile_decline_btn);
        mProfileDeclineBtn.setEnabled(false);
        mProfileDeclineBtn.setVisibility(View.GONE);

        mcurrent_state="not friends";

        mProgress=new ProgressDialog(this);
        mProgress.setTitle("loading user data");
        mProgress.setMessage("Please wait");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();



        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.ic_launcher_background).into(mProfileImage);

                //----------FRIENDS LIST/ REQUESTS FEATURE-------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                                mcurrent_state="req_received";
                                mProfileSendReqBtn.setText("accept friend request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);

                            }
                            if(req_type.equals("sent")){

                                mcurrent_state="req_sent";
                                mProfileSendReqBtn.setText("cancel friend request");
                            }
                        }
                        mProgress.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mProfileSendReqBtn.setEnabled(false);
               if(mcurrent_state.equals("not friends")){

                mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mcurrent_state="req_sent";
                                    mProfileSendReqBtn.setText("cancel friend request");

                                    Toast.makeText(ProfileActivity.this, "request sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "failed sending request", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

            if(mcurrent_state.equals("req_sent"))
            {

                mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mProfileSendReqBtn.setEnabled(true);
                                mcurrent_state="not friends";
                                mProfileSendReqBtn.setText("send friend request");
                            }
                        });
                    }
                });


            }

           }
       });

    }
}
