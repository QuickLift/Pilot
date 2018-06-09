package com.quickliftpilot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.quickliftpilot.DAO.DatabaseHelper;
import com.quickliftpilot.R;
import com.quickliftpilot.model.Driver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverRegistration extends AppCompatActivity {
    EditText name,email,phone,password,vehicle,vehicle_no;
    CircleImageView pic;
    ProgressDialog pdialog;
    private FirebaseAuth mAuth;
    String upload_img="";
    Uri selectedImage=null;
    private StorageReference mStorageRef;
    private static DatabaseHelper db;
    DatabaseReference user_db= FirebaseDatabase.getInstance().getReference("Drivers");

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setTitle("Driver Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pdialog=new ProgressDialog(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        vehicle=(EditText)findViewById(R.id.vehicle_type);
        vehicle_no=(EditText)findViewById(R.id.vehicle_no);
        password=(EditText)findViewById(R.id.password);
        pic=(CircleImageView) findViewById(R.id.image);
        db = new DatabaseHelper(this);

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photo = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                final int ACTIVITY_SELECT_IMAGE = 1234;
                startActivityForResult(photo, ACTIVITY_SELECT_IMAGE);
            }
        });
    }

    public void signup(View v){
        if (!validateForm()){

        }

        else {
            showProgressDialog();
            // [START create_user_with_email]
            String em=email.getText().toString();
            String pass=password.getText().toString();
            mAuth.createUserWithEmailAndPassword(em,pass )
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                sendEmailVerification();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(DriverRegistration.this, "Authentication failed. Please Try Again !", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }
                    });
            // [END create_user_with_email]
        }
    }

    private void updateUI(FirebaseUser user){ 
        hideProgressDialog();
        if (user != null) {
            Driver driver=new Driver();
            driver.setName(name.getText().toString());
            driver.setEmail(email.getText().toString());
            driver.setVeh_type(vehicle.getText().toString());
            driver.setVeh_num(vehicle_no.getText().toString());
            driver.setPhone(phone.getText().toString());
            driver.setThumb(upload_img);
            driver.setAddress("");

//            boolean status = db.insertProfileInfo(driver.getName(),driver.getEmail(),driver.getPhone(),password.getText().toString(),driver.getVeh_type(),driver.getVeh_num(),driver.getThumb(),null,null);
//            if (status){
//                Toast.makeText(this, "Successfully inserted", Toast.LENGTH_SHORT).show();
//            }else {
//                Toast.makeText(this, "Insertion Failed", Toast.LENGTH_SHORT).show();
//            }
//            finish();

            if (selectedImage!=null){
                StorageReference riversRef = mStorageRef.child("Drivers/"+user.getUid());

                UploadTask uploadTask = riversRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
//                        Toast.makeText(DriverRegistration.this, "Failed to upload image !", Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(DriverRegistration.this, "Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
//                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }

            user_db.child(user.getUid()).setValue(driver);
            mAuth.signOut();
            Toast.makeText(this, "Successfully Registered !", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Data not entered !", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        if (task.isSuccessful()) {
                            Toast.makeText(DriverRegistration.this, "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(DriverRegistration.this, "Failed to send verification email.",
//                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void hideProgressDialog() {
        pdialog.dismiss();
    }

    private void showProgressDialog() {
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(false);
        pdialog.setMessage("Please Wait  ...");
        pdialog.show();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required.");
            valid = false;
        } else {
            name.setError(null);
        }

        if (TextUtils.isEmpty(vehicle.getText().toString())) {
            vehicle.setError("Required.");
            valid = false;
        } else {
            vehicle.setError(null);
        }

        if (TextUtils.isEmpty(vehicle_no.getText().toString())) {
            vehicle_no.setError("Required.");
            valid = false;
        } else {
            vehicle_no.setError(null);
        }

        if (TextUtils.isEmpty(phone.getText().toString())) {
            phone.setError("Required.");
            valid = false;
        } else {
            phone.setError(null);
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Required.");
            valid = false;
        } else {
           password.setError(null);
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1234:
                if(resultCode == RESULT_OK){
                    showProgressDialog();

                    selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                    Bitmap imageThumbnail= ThumbnailUtils.extractThumbnail(yourSelectedImage,150,150);
                    upload_img = BitMapToString(imageThumbnail);
                    pic.setImageBitmap(yourSelectedImage);

                    hideProgressDialog();
                }
                break;
        }
    }


    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.NO_WRAP);
        return temp;
    }
}
