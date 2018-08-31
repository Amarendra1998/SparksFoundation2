package com.example.admin.sparksfoundation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;


public class Main2Activity extends AppCompatActivity {
      private TextView first,last,mail;
      private ImageView imageView;
      private Button button;
      private ShareDialog shareDialog;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main2);
        mAuth = FirebaseAuth.getInstance();
        shareDialog = new ShareDialog(this);
        imageView=(ImageView)findViewById(R.id.imageView);
        first=(TextView)findViewById(R.id.textView2);
        button=(Button)findViewById(R.id.button2);
        final FirebaseUser firebaseUser = mAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
           //
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firebaseUser!=null){
                mAuth.getInstance().signOut();
                Intent mine=new Intent(Main2Activity.this,MainActivity.class);
                startActivity(mine);
                finish();
                }else {
                    LISessionManager.getInstance(getApplicationContext()).clearSession();
                    Intent mine=new Intent(Main2Activity.this,MainActivity.class);
                    startActivity(mine);
                    finish();
                }
            }
        });
        fetchmydata();
        Bundle bundle = getIntent().getExtras();
        String myname=bundle.get("name").toString();
        String mysurname=bundle.get("surname").toString();
        String myimageurl=bundle.get("imageUrl").toString();
        /*Bundle parameters = new Bundle();
        String name = bundle.get("name").toString();
        String surname = bundle.get("surname").toString();
        String imageUrl = bundle.get("imageUrl").toString();*/
        first.setText(""+myname+""+mysurname);
        new Main2Activity.DownloadImage(imageView).execute(myimageurl);
    }
    public class DownloadImage extends AsyncTask<String,Void,Bitmap>{
        ImageView imageView;
        public DownloadImage(ImageView imageView) {
            this.imageView=imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urldisplay = strings[0];
            Bitmap bitmap=null;
            try {
                InputStream inputStream = new URL(urldisplay).openStream();
                bitmap= BitmapFactory.decodeStream(inputStream);
            }catch (Exception e){
                Log.e("Error",e.getMessage());
                Toast.makeText(Main2Activity.this,"error",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap bitmap){
            imageView.setImageBitmap(bitmap);
        }
    }
    private void fetchmydata() {
        String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,public-profile-url,picture-url,email-address,picture-urls::(original))";

        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, url, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) throws JSONException {
                JSONObject jsonObject = apiResponse.getResponseDataAsJson();
           try {
               String firstname = jsonObject.getString("firstName");
               String lastname = jsonObject.getString("lastName");
               String pictureUrl = jsonObject.getString("pictureUrl");
               String emailAddress = jsonObject.getString("emailAddress");
               Picasso.with(getApplicationContext()).load(pictureUrl).into(imageView);
               StringBuilder stringBuilder = new StringBuilder();
               stringBuilder.append("First Name:"+firstname);
               stringBuilder.append("\n\n");
               stringBuilder.append("Last Name:"+lastname);
               stringBuilder.append("\n\n");
               stringBuilder.append("Email:"+emailAddress);
               first.setText(stringBuilder);
              }catch (JSONException e){
               e.printStackTrace();
           }// Success!
            }
            @Override
            public void onApiError(LIApiError liApiError) {
                // Error making GET request!
                Log.e("error",liApiError.getMessage());
            }
        });
    }

}
