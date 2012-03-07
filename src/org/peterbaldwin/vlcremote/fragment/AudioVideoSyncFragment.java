
package org.peterbaldwin.vlcremote.fragment;

import org.peterbaldwin.client.android.vlcremote.R;
import org.peterbaldwin.vlcremote.intent.Intents;
import org.peterbaldwin.vlcremote.model.Status;
import org.peterbaldwin.vlcremote.net.MediaServer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DecimalFormat;


public final class AudioVideoSyncFragment extends DialogFragment implements View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {


    private ImageView mIcon;
    private SeekBar mSeekBar;
    private MediaServer mMediaServer;
    
    
    
    private Button mRateSlower;
    private Button mRateReset;
    private Button mRateFaster;
    private TextView mRateText;
    
    private RelativeLayout mAudioDelayTextContainer;
    private LinearLayout mAudioDelayButtons;
    private Button mAudioDelaySlower;
    private Button mAudioDelayReset;
    private Button mAudioDelayFaster;
    private SeekBar mAudioDelaySeeker;
    private TextView mAudioDelayText;
    
    private RelativeLayout mSubtitleDelayTextContainer;
    private LinearLayout mSubtitleDelayButtons;
    private Button mSubtitleDelaySlower;
    private Button mSubtitleDelayReset;
    private Button mSubtitleDelayFaster;
    private SeekBar mSubtitleDelaySeeker;
    private TextView mSubtitleDelayText;
    
    private double currentRate;
    private double currentAudioDelay;
    private double currentSubtitleDelay;

    private BroadcastReceiver mStatusReceiver;

    public void setMediaServer(MediaServer mediaServer) {
        mMediaServer = mediaServer;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    
    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.audiovideosync_fragment, root, false);
        View view = inflater.inflate(R.layout.navigation_fragment, root, false);
        
        
        
        //mSeekBar.setOnSeekBarChangeListener(this);
        return view;
    }*/
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.audiovideosync_fragment, null);
        
        System.out.println(R.id.rate_increase);
        System.out.println(R.id.audiodelay_text_container);
        System.out.println(R.id.subtitledelay_text_container);
        mRateSlower = (Button) view.findViewById(R.id.rate_decrease);
        mRateReset = (Button) view.findViewById(R.id.rate_reset);
        mRateFaster = (Button) view.findViewById(R.id.rate_increase);
        mRateText = (TextView) view.findViewById(R.id.rate_text);
        
        mRateSlower.setOnClickListener(this);
        mRateReset.setOnClickListener(this);
        mRateFaster.setOnClickListener(this);
        
        mSubtitleDelaySlower = (Button) view.findViewById(R.id.subtitledelay_decrease);
        mSubtitleDelayReset = (Button) view.findViewById(R.id.subtitledelay_reset);
        mSubtitleDelayFaster = (Button) view.findViewById(R.id.subtitledelay_increase);
        mSubtitleDelayButtons = (LinearLayout) view.findViewById(R.id.subtitledelay_buttons);
        mSubtitleDelayText = (TextView) view.findViewById(R.id.subtitledelay_text);
        mSubtitleDelayTextContainer = (RelativeLayout) view.findViewById(R.id.subtitledelay_text_container);
        mSubtitleDelaySeeker = (SeekBar) view.findViewById(R.id.subtitledelay_seekbar);
        
        mSubtitleDelayTextContainer.setOnLongClickListener(this);
        mSubtitleDelaySeeker.setOnSeekBarChangeListener(this);
        mSubtitleDelaySlower.setOnClickListener(this);
        mSubtitleDelayReset.setOnClickListener(this);
        mSubtitleDelayFaster.setOnClickListener(this);
        
        mAudioDelaySlower = (Button) view.findViewById(R.id.audiodelay_decrease);
        mAudioDelayReset = (Button) view.findViewById(R.id.audiodelay_reset);
        mAudioDelayFaster = (Button) view.findViewById(R.id.audiodelay_increase);
        mAudioDelayButtons = (LinearLayout) view.findViewById(R.id.audiodelay_buttons);
        mAudioDelayText = (TextView) view.findViewById(R.id.audiodelay_text);
        mAudioDelayTextContainer = (RelativeLayout) view.findViewById(R.id.audiodelay_text_container);
        mAudioDelaySeeker = (SeekBar) view.findViewById(R.id.audiodelay_seekbar);
        
        mAudioDelayTextContainer.setOnLongClickListener(this);
        mAudioDelayTextContainer.setOnClickListener(this);
        mAudioDelaySeeker.setOnSeekBarChangeListener(this);
        mAudioDelaySlower.setOnClickListener(this);
        mAudioDelayReset.setOnClickListener(this);
        mAudioDelayFaster.setOnClickListener(this);
        
        currentAudioDelay = 0;
        currentSubtitleDelay = 0;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Audio and Visual Sync");
        builder.setView(view);
        builder.setNeutralButton(R.string.close, null);

        AlertDialog dialog = builder.show();
        
        //dialog.setView(v);
        
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        final View v = inflater.inflate(R.layout.audiovideosync_fragment, null);
        
        // Handle ListView item clicks directly so that dialog is not dismissed

        return dialog;
    }


    @Override
    public void onResume() {
        super.onResume();
        mStatusReceiver = new StatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_STATUS);
        getActivity().registerReceiver(mStatusReceiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mStatusReceiver);
        mStatusReceiver = null;
        super.onPause();
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intents.ACTION_STATUS.equals(action)) {
                Status status = (Status) intent.getSerializableExtra(Intents.EXTRA_STATUS);
                
                DecimalFormat twoDForm = new DecimalFormat("##.###");
                currentRate = Double.valueOf(twoDForm.format(status.getRate()));
                currentAudioDelay = status.getAudioDelay();
                currentSubtitleDelay = status.getSubtitleDelay();
                
                mAudioDelaySeeker.setProgress((int)(currentAudioDelay * 50) + 50);
                mAudioDelayText.setText("" + currentAudioDelay);
                
                mSubtitleDelaySeeker.setProgress((int)(currentSubtitleDelay * 50) + 50);
                mSubtitleDelayText.setText("" + currentSubtitleDelay);
                
                
                mRateText.setText("" + currentRate);
            }
        }
    }

    private void setRate(double rate)
    {
        DecimalFormat twoDForm = new DecimalFormat("##.###");
        rate = Double.valueOf(twoDForm.format(rate));
        mMediaServer.status().command.rate(rate);
        
        currentRate = rate;
        
        mRateText.setText("" + rate);
    }
    
    private void setAudioDelay(double delay)
    {
        DecimalFormat twoDForm = new DecimalFormat("##.####");
        delay = Double.valueOf(twoDForm.format(delay));
        mMediaServer.status().command.audioDelay(delay);
        currentAudioDelay = delay;
        
        mAudioDelaySeeker.setProgress((int)(delay * 50) + 50);
        mAudioDelayText.setText("" + delay);
    }
    
    private void setSubtitleDelay(double delay)
    {
        DecimalFormat twoDForm = new DecimalFormat("##.####");
        delay = Double.valueOf(twoDForm.format(delay));
        mMediaServer.status().command.subtitleDelay(delay);
        
        currentAudioDelay = delay;
        
        mSubtitleDelayText.setText("" + delay);
    }
       
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
        if (v == mRateSlower || v == mRateReset || v== mRateFaster)
        {
            double[] rates = {.03, .06, .12, .25, .33, .50, .67, 1.00, 1.50, 2.00, 3.00, 4.00, 8.00, 16.0, 32.0, 64.0};
            int rateIndex = 7;
            
            for (int i = 0; i < rates.length; i++)
            {
                if (rates[i] <= currentRate)
                {
                    rateIndex = i;
                }
            }
            
               
            if (v == mRateSlower)
            {
                setRate(rates[Math.max(0, rateIndex - 1)]);
                
            }
            else if (v == mRateReset)
            {
                setRate(1);
            }
            else if (v == mRateFaster)
            {
                setRate(rates[Math.min(rates.length - 1, rateIndex + 1)]);
            }
        }
        else if (v == mAudioDelaySlower)
        {
            setAudioDelay(currentAudioDelay - 0.05);
        } 
        else if (v == mAudioDelayReset || (v == mAudioDelayTextContainer && mAudioDelaySeeker.getVisibility() == View.VISIBLE))
        {
            setAudioDelay(0);
        }
        else if (v == mAudioDelayFaster)
        {
            setAudioDelay(currentAudioDelay + 0.05);
        }
        else if (v == mSubtitleDelaySlower)
        {
            setSubtitleDelay(currentSubtitleDelay - 0.05);
        } 
        else if (v == mSubtitleDelayReset  || (v == mSubtitleDelayTextContainer && mSubtitleDelaySeeker.getVisibility() == View.VISIBLE))
        {
            setSubtitleDelay(0);
        }
        else if (v == mSubtitleDelayFaster)
        {
            setSubtitleDelay(currentSubtitleDelay + 0.05);
        }

        
        
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        
    }

    public boolean onLongClick(View v) {
        if (v == mAudioDelayTextContainer)
        {
            if (mAudioDelaySeeker.getVisibility() == View.GONE)
            {
                mAudioDelaySeeker.setVisibility(View.VISIBLE);
                mAudioDelayButtons.setVisibility(View.GONE);
            }
            else
            {
                mAudioDelaySeeker.setVisibility(View.GONE);
                mAudioDelayButtons.setVisibility(View.VISIBLE);
            }
        } 
        else if (v == mSubtitleDelayTextContainer)
        {
            if (mSubtitleDelaySeeker.getVisibility() == View.GONE)
            {
                mSubtitleDelaySeeker.setVisibility(View.VISIBLE);
                mSubtitleDelayButtons.setVisibility(View.GONE);
            }
            else
            {
                mSubtitleDelaySeeker.setVisibility(View.GONE);
                mSubtitleDelayButtons.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
        if (fromUser)
        {
            if (bar == mAudioDelaySeeker)
            {
                setAudioDelay((bar.getProgress() - 50.0) / 50.0);
            }
            else if (bar == mSubtitleDelaySeeker)
            {
                setSubtitleDelay((bar.getProgress() - 50) / 50);
            }
        }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        
    }

    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        
    }
}
