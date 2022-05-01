package com.example.bchainprac.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.bchainprac.R;
import com.example.bchainprac.view.base.BaseActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultActivity extends BaseActivity {

    Button dis;
    Spinner s1,s2,s3,s4,s5,s6,s7;
    String d[] = new String[7];

    String diarrhoea[] = {"Stomach Ache","Nausea","Vomiting","Fever","Sudden Weight Loss"};
    String malaria[] = {"Fever","Vomiting","Sweating","Muscle And Body Pain","Headaches"};
    String typhoid[] = {"Fever","Headaches","Weakness/Fatigue","Abdominal Pain","Muscle Pain","Dry Cough","Diarrhoea/Constipation"};
    String diabetes[] = {"Frequent Urination","Hunger","Thirsty Than Usual","Sudden Weight Loss","Blurred Vision","Skin Itching"};
    String blood_pressure[] = {"Headaches","Blurred Vision","Chest Pain","Shortness in Breath","Dizziness","Nausea","Vomiting"};
    String cardio_disease[] = {"Shortness in Breath","Fast Heartbeat","Indigestion","Pressure Or Heaviness In Chest","Anxiety"};



    public ResultActivity() {
        super(R.layout.activity_result, true);
    }

    @Override
    protected void doOnCreate(Bundle bundle) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        toolbarTextView.setText(R.string.medical_cv);
        toolbarBackImageView.setVisibility(View.VISIBLE);

        dis= findViewById(R.id.disease);

        s1 = findViewById(R.id.syp1);
        s2 = findViewById(R.id.syp2);
        s3 = findViewById(R.id.syp3);
        s4 = findViewById(R.id.syp4);
        s5 = findViewById(R.id.syp5);
        s6 = findViewById(R.id.syp6);
        s7 = findViewById(R.id.syp7);
        final String name = getIntent().getStringExtra("name");
        final String gender = getIntent().getStringExtra("gender");

        dis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                int c[] = new int[6];
                d[0] = s1.getSelectedItem().toString();
                d[1] = s2.getSelectedItem().toString();
                d[2] = s3.getSelectedItem().toString();
                d[3] = s4.getSelectedItem().toString();
                d[4] = s5.getSelectedItem().toString();
                d[5] = s6.getSelectedItem().toString();
                d[6] = s7.getSelectedItem().toString();



                for( int i = 0 ; i < 7 ; i++)
                {
                    for( int j=1 ; j <= 6; j++)
                    {
                        switch (j)
                        {
                            case 1 : {
                                int l = 0;
                                l = diarrhoea.length;
                                for(int k=0 ; k<l ; k++ )
                                {
                                    if(d[i].equals(diarrhoea[k]) )
                                    {
                                        c[0]++;
                                    }
                                }
                                break; }

                            case 2: {
                                int l = 0;
                                l = malaria.length;
                                for (int k = 0; k < l; k++) {
                                    if (d[i].equals(malaria[k])) {
                                        c[1]++;
                                    }
                                }
                                break;
                            }

                            case 3: {
                                int l = 0;
                                l = typhoid.length;
                                for (int k = 0; k < l; k++) {
                                    if (d[i].equals(typhoid[k])) {
                                        c[2]++;
                                    }
                                }
                                break;
                            }

                            case 4: {
                                int l = 0;
                                l = diabetes.length;
                                for (int k = 0; k < l; k++) {
                                    if (d[i].equals(diabetes[k])) {
                                        c[3]++;
                                    }
                                }
                                break;
                            }

                            case 5: {
                                int l = 0;
                                l = blood_pressure.length;
                                for (int k = 0; k < l; k++) {
                                    if (d[i].equals(blood_pressure[k])) {
                                        c[4]++;
                                    }
                                }
                                break;
                            }

                            case 6: {
                                int l = 0;
                                l = cardio_disease.length;
                                for (int k = 0; k < l; k++) {
                                    if (d[i].equals(cardio_disease[k])) {
                                        c[5]++;
                                    }
                                }
                                break;
                            }



                        }
                    }
                }

                int max = c[0];
                for( int m=0; m<6 ; m++)
                {
                    if(c[m] > max)
                        max = c[m];
                }


                Intent dis_page = new Intent(ResultActivity.this,DiseaseActivity.class);
//                dis_page.putExtra("name",name);
//                dis_page.putExtra("gender",gender);
                dis_page.putExtra("max",max);
                dis_page.putExtra("c",c);
                startActivity(dis_page);

            }


        });

    }

//    private void showDialog(Set<String> result) {
//        String res="";
//        int i=1;
//        for(String str:result){
//            res+= String.valueOf(i)+": "+str+"\n";
//            i++;
//        }
//        new AlertDialog.Builder(ResultActivity.this)
//                .setMessage("Your Diagnosis report is here...")
//                .setTitle(res)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.i("Result","Success");
//                    }
//                })
//                .show();
//    }
//
//    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
//        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("");
//        FileInputStream inputStream = new  FileInputStream(fileDescriptor.getFileDescriptor());
//        FileChannel fileChannel = inputStream.getChannel();
//        long startOffset = fileDescriptor.getStartOffset();
//        long declaredLength = fileDescriptor.getDeclaredLength();
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
//    }
//
//
//    public Set<String> get_result(List<String> strings){
//        //TODO
//        if(tflite==null){
//            Log.d("message","null interpreter");
//        }
//
//        Set<String> result=new HashSet<>();
//        for(String s:strings){
//            float[][] input=new float[1][symptoms.size()];
//            for(int i=0;i<symptoms.size();i++){
//                if(symptoms.get(i).equals(s)){
//                    input[0][i]=1.0f;
//                }
//                else{
//                    input[0][i]=0.0f;
//                }
//            }
//            float[][] output=new float[1][diseases.size()];
//            tflite.run(input,output);
//            String[] mappings={"0", "1", "10", "11", "12", "13", "14", "15", "16", "17", "18",
//                    "19", "2", "20", "21", "22", "23", "24", "25", "26", "27", "28",
//                    "29", "3", "30", "31", "32", "33", "34", "35", "36", "37", "38",
//                    "39", "4", "40", "5", "6", "7", "8", "9"};
//            int index=0;
//            float max_val=0.0f;
//            int max_index=-1;
//            for(float val:output[0]){
//                Log.d("message",val+" ");
//                if(max_val<val){
//                    max_val=val;
//                    max_index=index;
//                }
//                index++;
//            }
//            int val=Integer.parseInt(mappings[max_index]);
//            result.add(diseases.get(val));
//        }
//
//        return result;
//    }
//
//    public class ImageGalleryAdapter extends RecyclerView.Adapter{
//        List<String> list;
//        Context context;
//
//        public ImageGalleryAdapter(List<String> list,
//                                   Context context)
//        {
//            this.list=new ArrayList<>();
//            for(String s:list){
//                this.list.add(String.join(" ",s.split("_")));
//            }
//            this.context = context;
//        }
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            Context context
//                    = parent.getContext();
//            LayoutInflater inflater
//                    = LayoutInflater.from(context);
//
//            // Inflate the layout
//
//            View photoView
//                    = inflater
//                    .inflate(R.layout.diagnosis_item,
//                            parent, false);
//
//            ImageGalleryAdapter.DiagnosisViewHolder viewHolder
//                    = new ImageGalleryAdapter.DiagnosisViewHolder(photoView);
//            return viewHolder;
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            DiagnosisViewHolder viewHolder=(DiagnosisViewHolder)holder;
//            viewHolder.txt.setText(list.get(position));
//        }
//
//        @Override
//        public int getItemCount() {
//            return list.size();
//        }
//
//        public class DiagnosisViewHolder extends RecyclerView.ViewHolder {
//
//
//            RelativeLayout layout;
//            TextView txt;
//            ImageView img;
//
//            public DiagnosisViewHolder(View itemView)
//            {
//                super(itemView);
//                layout=itemView.findViewById(R.id.image_btn);
//                txt=itemView.findViewById(R.id.txtSymptom);
//                img=itemView.findViewById(R.id.imageDiagnosis);
//
//                txt.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(img.getVisibility()==View.GONE){
//                            img.setVisibility(View.VISIBLE);
//                            selectedStrings.add(txt.getText().toString());
//                        }
//                        else{
//                            img.setVisibility(View.GONE);
//                            selectedStrings.remove(txt.getText().toString());
//                        }
//                    }
//                });
//            }
//
//        }
//    }
}
