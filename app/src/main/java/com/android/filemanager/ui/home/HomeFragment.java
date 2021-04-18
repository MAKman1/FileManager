package com.android.filemanager.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.filemanager.MainActivity;
import com.android.filemanager.R;
import com.android.filemanager.pmTextEdit;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class HomeFragment extends Fragment {

    public Button backButton = null;
    public Button newButton = null;
    public RecyclerView recycler = null;
    public TextView notFound = null;

    public ArrayList<File> files = null;

    public ArrayList<File> fileTree = null;

    public HomeFragment(){}

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        backButton = root.findViewById(R.id.backButton);
        newButton = root.findViewById(R.id.newButton);
        recycler = root.findViewById(R.id.recycler);
        notFound = root.findViewById(R.id.notFound);

        ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                11);

        //Setting file tree
        fileTree = new ArrayList<>();

        //Get data in current directory
        populateDataInCurrentDirectory();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousDirectory();
            }
        });
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File dir;
                if( fileTree.size() == 0)
                    dir = new File( android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                else
                    dir = fileTree.get( fileTree.size() - 1);

                Intent intent = new Intent(getContext(), pmTextEdit.class);
                intent.putExtra("FILE_ABSOLUTE_PATH", dir.getAbsolutePath());
                startActivity(intent);
            }
        });

        return root;
    }

    public void populateDataInCurrentDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            notFound.setVisibility( View.GONE);
            files = new ArrayList<>();

            File dir = null;
            if( fileTree.size() == 0)
                dir = new File( android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
            else
                dir = fileTree.get( fileTree.size() - 1);

            File listFile[] = dir.listFiles();
            if (listFile != null) {
                for (int i = 0; i < listFile.length; i++) {
                    files.add( listFile[i]);
                }
            }

            recycler.setLayoutManager( new LinearLayoutManager( getContext()));
            recycler.setAdapter( new ContentRecyclerAdapter( getContext(), files));

            if( fileTree.size() == 0){
                backButton.setVisibility( View.GONE);
            } else {
                backButton.setVisibility( View.VISIBLE);
            }
        } else {
            notFound.setVisibility( View.VISIBLE);
        }
    }

    public void goToDirectory( File dir){
        fileTree.add( dir);
        populateDataInCurrentDirectory();
    }

    public void goToPreviousDirectory(){
        fileTree.remove( fileTree.size() - 1);
        populateDataInCurrentDirectory();
    }

    public String getFileMime(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public class ContentRecyclerAdapter extends RecyclerView.Adapter<ContentRecyclerAdapter.ViewHolder> {

        private ArrayList<File> files;
        private LayoutInflater mInflater;

        ContentRecyclerAdapter(Context context, ArrayList<File> files) {
            this.mInflater = LayoutInflater.from(context);
            this.files = files;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.file_view_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File current = files.get(position);
            holder.fileName.setText(current.getName());

            //Setting icon
            if( current.isFile()){
                holder.directoryIcon.setVisibility( View.GONE);
                holder.fileIcon.setVisibility( View.VISIBLE);
            } else {
                holder.directoryIcon.setVisibility( View.VISIBLE);
                holder.fileIcon.setVisibility( View.GONE);
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(current.isDirectory()){
                        goToDirectory( current);
                    } else {
                        //Open file
                        try {
                            String mime = getFileMime( current.getAbsolutePath());

                            if( mime.equals("text/plain")){
                                Intent intent = new Intent(getContext(), pmTextEdit.class);
                                intent.putExtra("FILE_ABSOLUTE_PATH", current.getAbsolutePath());
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile( current), mime);
                                startActivity(intent);
                            }

                        } catch (Exception e) {
                            Toast.makeText( getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView fileName;
            public View view;
            public ImageView directoryIcon;
            public ImageView fileIcon;

            ViewHolder(View itemView) {
                super(itemView);
                fileName = itemView.findViewById(R.id.fileName);
                view = itemView.findViewById( R.id.view);
                directoryIcon = itemView.findViewById( R.id.directoryIcon);
                fileIcon = itemView.findViewById( R.id.fileIcon);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 11: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateDataInCurrentDirectory();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        populateDataInCurrentDirectory();
    }
}