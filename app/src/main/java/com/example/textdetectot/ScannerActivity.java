package com.example.textdetectot;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    private ImageView captureIV;
    private TextView resTV;
    private Button snapBtn,detBtn;
    private Bitmap imgBitmap;
    static final int REQ_IMG_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        captureIV=findViewById(R.id.idIVCaptureImage);
        resTV=findViewById(R.id.idTVDetectedText);
        snapBtn=findViewById(R.id.idBtnSnap);
        detBtn=findViewById(R.id.idBtnDet);

        detBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detText();
            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(checkperm()){
                     captureImg();
                 }
                 else{
                     reqPerm();
                 }
            }
        });

    }
    private boolean checkperm(){
        int camoerm= ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return camoerm == PackageManager.PERMISSION_GRANTED;
    }
    private void reqPerm(){
        int PERM_CODE =200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERM_CODE);
    }

    private void captureImg(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePic,REQ_IMG_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean camPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(camPermission){
                Toast.makeText(this, "Permission Granted",Toast.LENGTH_SHORT).show();
                captureImg();
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_IMG_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imgBitmap=(Bitmap) extras.get("data");
            captureIV.setImageBitmap(imgBitmap);
        }
    }

    private void detText(){
          InputImage image = InputImage.fromBitmap(imgBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> res = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder res = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockTest = block.getText();
                    Point[] blockCorPoint = block.getCornerPoints();
                    Rect blockFrme = block.getBoundingBox();
                    for(Text.Line l: block.getLines()){
                        String lText = l.getText();
                        Point[] lcornerPoint = l.getCornerPoints();
                        Rect lRect =  l.getBoundingBox();
                        for(Text.Element elm: l.getElements()){
                            String elmText = elm.getText();
                            res.append(elmText);
                        }
                        resTV.setText(blockTest);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Fail to detect text from image"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}