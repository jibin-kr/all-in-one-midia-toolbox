package com.glofora.whatstus.fragments.bwa;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.glofora.whatstus.R;
import com.glofora.whatstus.Utls.Utilities;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Objects;

import needle.Needle;
import needle.UiRelatedProgressTask;

import static android.app.Activity.RESULT_OK;
import static com.glofora.whatstus.Utls.Utilities.CAMERA_REQUEST_VIDEO_TRIMMER;
import static com.glofora.whatstus.Utls.Utilities.REQUEST_STORAGE_READ_ACCESS_PERMISSION;
import static com.glofora.whatstus.Utls.Utilities.REQUEST_VIDEO_TRIMMER;


/**
 * A simple {@link Fragment} subclass.
 */
public class BWASplitterFragment extends Fragment {
View splitterView;
    private NumberPicker numberPicker;
    public static int app = 99;
    //whatsapp for business 99
    //whatsapp  100
    //Others   101
    public static int interval = 30;
    // 60 -instagram
    private ProgressDialog mProgressDialog;
    private  RadioButton rdBtnW;
    private  RadioButton rdBtnC;
    public BWASplitterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getContext());

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.delete_prgs_msg));
        setHasOptionsMenu(true);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        splitterView=inflater.inflate(R.layout.fragment_wa_and_bwa_splitter, container, false);
        Button galleryButton = splitterView.findViewById(R.id.galleryButton);
        if (galleryButton != null) {
            galleryButton.setOnClickListener(v -> pickFromGallery());
        }


        numberPicker = splitterView.findViewById(R.id.number_picker);

        numberPicker.setMaxValue(59);
        numberPicker.setMinValue(10);
        numberPicker.setValue(30);
        // Set fading edge enabled
        numberPicker.setFadingEdgeEnabled(true);

// Set scroller enabled
        numberPicker.setScrollerEnabled(true);

// Set wrap selector wheel
        numberPicker.setWrapSelectorWheel(true);

// Set accessibility description enabled
        numberPicker.setAccessibilityDescriptionEnabled(true);


// OnClickListener
        numberPicker.setOnClickListener(view -> {
            interval = numberPicker.getValue();
        });

// OnValueChangeListener
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> interval = newVal);

// OnScrollListener
        numberPicker.setOnScrollListener((picker, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                interval = picker.getValue();
            }
        });
        Button recordButton = splitterView.findViewById(R.id.cameraButton);
        if (recordButton != null) {
            recordButton.setOnClickListener(v -> openVideoCapture());
        }
        rdBtnW =splitterView.findViewById(R.id.radio_Whatsapp);
        rdBtnW.setText("Business Whatsapp");

        rdBtnC =splitterView.findViewById(R.id.radio_Instagram);
        if(!Utilities.checkInstallation("com.whatsapp.w4b",getContext())){
            rdBtnC.setChecked(true);
            app = 101;

            numberPicker.setValue(30);
            interval = numberPicker.getValue();

        }
        rdBtnW.setOnClickListener(v -> {
            if(Utilities.checkInstallation("com.whatsapp.w4b",getContext())) {

                onRadioButtonClicked(v);
            }else {
                Toast.makeText(getContext(), "Business WhatsApp Not Found on this Phone :(", Toast.LENGTH_SHORT).show();
                rdBtnC.setChecked(true);
                app = 101;

                numberPicker.setValue(30);
                interval = numberPicker.getValue();
            }
        });
        rdBtnC.setOnClickListener(v -> {
            onRadioButtonClicked(v);
        });


        return splitterView;
    }
    private void pickFromGallery() {

        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Utilities.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getString(R.string.permission_read_storage_rationale), REQUEST_STORAGE_READ_ACCESS_PERMISSION,getContext());
        } else {
            Intent intent = new Intent();
            intent.setTypeAndNormalize("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);


                startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_video)), REQUEST_VIDEO_TRIMMER);

        }
    }
    private void openVideoCapture() {
//        Intent videoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        startActivityForResult(videoCapture, REQUEST_VIDEO_TRIMMER);

        final int durationLimit = 59;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, durationLimit);

            startActivityForResult(intent, CAMERA_REQUEST_VIDEO_TRIMMER);

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_Whatsapp:
                if (checked)


                    numberPicker.setValue(30);

                //whatsapp for business
                app = 99;

                numberPicker.setValue(30);
                interval = numberPicker.getValue();
                break;
            case R.id.radio_Instagram:
                if (checked)
                    //Others
                    app = 101;

                numberPicker.setValue(30);
                interval = numberPicker.getValue();
                break;
        }
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_splitter_fragment, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /**
             * Camera Capture result.
             */
            if (requestCode == CAMERA_REQUEST_VIDEO_TRIMMER) {
                final Uri selectedUri = data.getData();

                openTrimmerActivity(selectedUri);

            }
            /**
             *Phone's Gallery Activity result.
             */
            if (requestCode == REQUEST_VIDEO_TRIMMER) {
                final Uri selectedUri = data.getData();

                openTrimmerActivity(selectedUri);

            }

        }
    }

    public void openTrimmerActivity(Uri selectedUri)
    {
        if (selectedUri != null) {
            try {


                Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
                    @Override
                    protected String doWork() {
                        int result = 0;

                        result += 1;
                        publishProgress(result);
                        Utilities.startTrimActivity(selectedUri, getContext(), app, interval);

                        return "success";


                    }

                    @Override
                    protected void thenDoUiRelatedWork(String result) {
                        mProgressDialog.cancel();



                    }

                    @Override
                    protected void onProgressUpdate(Integer progress) {
                        mProgressDialog.show();
                        mProgressDialog.setMessage("Loading.........");
                    }
                });



            } catch (Exception ex) {
                Toast.makeText(getContext(), "Large File not supported", Toast.LENGTH_SHORT).show();

            }

        } else {
            Toast.makeText(getContext(), R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show();
        }
    }

}
