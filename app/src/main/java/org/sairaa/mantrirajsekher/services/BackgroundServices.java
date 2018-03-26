package org.sairaa.mantrirajsekher.services;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.sairaa.mantrirajsekher.UtilCheckStatus;

import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

import static org.sairaa.mantrirajsekher.services.BackgroundUtil.showNotification;

/**
 * Created by praful on 9/9/2017.
 */

public class BackgroundServices extends JobService {

    public static final String LOG_TAGBACK = BackgroundServices.class.getName();

    public static final String INSERTURL = "http://sairaa.org/mantri_rajsekhar/get_json_details.php";
    public static final String UPDATEURL = "http://sairaa.org/mantri_rajsekhar/get_json_updated_database.php";
    public static final String DELETEURL = "http://sairaa.org/mantri_rajsekhar/get_json_of_delete_record.php";

    private static final String CHECK_URL = "http://sairaa.org/mantri_rajsekhar/check_updated_table.php";
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(LOG_TAGBACK,"TestB: onStartJob");
        //Toast.makeText(this, "On Start Job",Toast.LENGTH_SHORT).show();
        int i = 15;
        Log.i(LOG_TAGBACK,"Test : onStartJob()"+i);
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected){

            new backgroundTask(this).execute(params);
            return true;
        }else {
//            jobFinished(params,false);
            //return false;
            this.jobFinished(params,false);
        }

        //jobFinished(params, false);
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        //Toast.makeText(this,"OnStopJob",Toast.LENGTH_LONG).show();
        Log.i(LOG_TAGBACK, "TestBack : OnStopJob ");
        return true;
    }

    private static class backgroundTask extends AsyncTask<JobParameters, Void, JobParameters>{ // parameter, progress, result
        BackgroundServices myService;

        backgroundTask(BackgroundServices myService){
            this.myService = myService;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            Log.i(LOG_TAGBACK,"Test : onStart().doinBackground() is called");

            int UtilStatus = UtilCheckStatus.facthUpdates(CHECK_URL,myService);
            Log.i(LOG_TAGBACK,"Test : UtisStatus: "+UtilStatus);
            if(UtilStatus != 0){
                Log.i(LOG_TAGBACK,"Test : UtisStatus: "+UtilStatus);
                String notify = BackgroundUtil.doDatabaseUpdate(INSERTURL,UPDATEURL,DELETEURL,myService);
                //showNotification(myService,"Hello");
                if(notify != null){
                    showNotification(myService, notify);
                }
            }

            return params[0];
        }



        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            Log.i(LOG_TAGBACK,"TestB: OnPostExecute");

            myService.jobFinished(jobParameters, false);
        }
    }
}
