package com.glofora.whatstustoolbox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.glofora.whatstustoolbox.R;
import com.glofora.whatstustoolbox.Utls.Common;
import com.glofora.whatstustoolbox.Utls.ThumbnailUtils;
import com.glofora.whatstustoolbox.adapter.FilesAdapter;
import com.glofora.whatstustoolbox.model.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import needle.Needle;
import needle.UiRelatedProgressTask;

import static com.glofora.whatstustoolbox.Utls.Common.APP_DIR;

public class SavedFilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Status> savedFilesList = new ArrayList<>();
    private Handler handler = new Handler();
    private FilesAdapter filesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView no_files_found;
    private View savedItemView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_saved_files,container,false);
        savedItemView=root;
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        APP_DIR= Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+this.getString(R.string.app_name)+ "/Whatstus Saved Statuses/";

        recyclerView = view.findViewById(R.id.recyclerViewFiles);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutFiles);
        progressBar = view.findViewById(R.id.progressBar);
        no_files_found = view.findViewById(R.id.no_files_found);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(),android.R.color.holo_orange_dark)
                ,ContextCompat.getColor(getActivity(),android.R.color.holo_green_dark),
                ContextCompat.getColor(getActivity(),R.color.colorPrimary),
                ContextCompat.getColor(getActivity(),android.R.color.holo_blue_dark));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFiles();
            }
        });
        getFiles();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
    }

    private void getFiles() {

        final File app_dir = new File(Common.APP_DIR);
       final   List<Status> savedFilesList_temp = new ArrayList<>();

        if (app_dir.exists()){

            no_files_found.setVisibility(View.GONE);

            Needle.onBackgroundThread().execute(new UiRelatedProgressTask<String, Integer>() {
                @Override
                protected String doWork() {
                    int result = 0;
                    File[] savedFiles = null;
                    savedFiles = app_dir.listFiles();

                    if (savedFiles!=null && savedFiles.length>0){

                        Arrays.sort(savedFiles);
                        for (File file : savedFiles){
                            Status status = new Status(file, file.getName(), file.getAbsolutePath());

                            if (status.isVideo())
                                status.setThumbnail(getThumbnail(status));

                            savedFilesList_temp.add(status);
                            result += 1;
                            publishProgress(result);
                        }

                    }else {
                        result += 1;
                        publishProgress(result);
                    }

                    return "The result is: " + result;
                }

                @Override
                protected void thenDoUiRelatedWork(String result) {
                    savedFilesList=savedFilesList_temp;
                    filesAdapter = new FilesAdapter(savedFilesList,savedItemView);
                    recyclerView.setAdapter(filesAdapter);
                    filesAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if(savedFilesList.size()==0)
                    {
                        no_files_found.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                protected void onProgressUpdate(Integer progress) {

                }
            });
        }else {
            savedFilesList.clear();
            filesAdapter = new FilesAdapter(savedFilesList,savedItemView);
            recyclerView.setAdapter(filesAdapter);
            filesAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            no_files_found.setVisibility(View.VISIBLE);
         
        }

    }

    private Bitmap getThumbnail(Status status) {
        return ThumbnailUtils.createVideoThumbnail(status.getFile().getAbsolutePath(),
                3);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null)  {
            if(resultCode == -1)
            {
                getFiles();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                getFiles();
            }
        }
    }

    @Override
    public void onResume() {
       getFiles();
        super.onResume();
    }


}
