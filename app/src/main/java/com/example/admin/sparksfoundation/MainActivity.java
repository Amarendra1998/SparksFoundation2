package com.example.admin.sparksfoundation;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.pm.Signature;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
private CallbackManager mCallbackManager;
private static final String TAG = "FACELOG";
private ProfileTracker profileTracker;
    private FirebaseAuth mAuth;
    private AccessTokenTracker accessTokenTracker;
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        initializeControls();
        // Initialize Facebook Login button

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };
        profileTracker=new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                 nextactivity(currentProfile);
            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();
        LoginButton loginButton = findViewById(R.id.login_button);
        FacebookCallback<LoginResult>callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile =Profile.getCurrentProfile();
                nextactivity(profile);
                Toast.makeText(getApplicationContext(),"Logged in",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        };
        loginButton.setReadPermissions("user_friends");
        loginButton.registerCallback(mCallbackManager,callback);
            /* loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                if (profile != null){
                    nextactivity(profile);
                }

                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                 handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });*/
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.admin.sparksfoundation",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Profile profile=Profile.getCurrentProfile();
        nextactivity(profile);
    }

    private void nextactivity(Profile profile) {
        if (profile!=null){
            Intent mine = new Intent(MainActivity.this,Main2Activity.class);
            //Bundle parameters = new Bundle();
            mine.putExtra("name",profile.getFirstName());
            mine.putExtra("surname",profile.getLastName());
            mine.putExtra("imageUrl",profile.getProfilePictureUri(200,200).toString());

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
       // profileTracker.stopTracking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        accessTokenTracker.stopTracking();
    }

    private void initializeControls() {
        ImageView linkedin = (ImageView)findViewById(R.id.imageView2);
        linkedin.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            updateUI(currentUser);
        }

    }

    private void updateUI(FirebaseUser firebaseUser) {
        Toast.makeText(MainActivity.this,"You're logged in",Toast.LENGTH_SHORT).show();
        Intent mine = new Intent(MainActivity.this,Main2Activity.class);
        startActivity(mine);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
   /* private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

       AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        final com.google.android.gms.tasks.Task<AuthResult> authResultTask = mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }

                });
    }*/

    @Override
    public void onClick(View v) {
   switch (v.getId()){
       case R.id.imageView2:
           handlelogin();
           break;
   }
    }
    private void handlelogin(){
        LISessionManager.getInstance(getApplicationContext()).init(this,  buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                Intent mine = new Intent(MainActivity.this,Main2Activity.class);
                startActivity(mine);
                finish();
                // Authentication was successful.  You can now do
                // other calls with the SDK.
            }

            @Override
            public void onAuthError(LIAuthError error) {
                Log.e("wrong",error.toString());
                // Handle authentication errors
            }
        }, true);
    }

    private Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.W_SHARE,Scope.R_EMAILADDRESS);
    }
}
